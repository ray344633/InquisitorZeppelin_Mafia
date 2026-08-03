package com.scir4y.zeppelinmurdermod.content.elevator.block.entity;

import com.scir4y.zeppelinmurdermod.registry.ModBlockEntities;
import com.scir4y.zeppelinmurdermod.content.elevator.util.ElevatorAssembler;
import com.scir4y.zeppelinmurdermod.content.elevator.util.ElevatorShaftRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Мозг лифта.
 * Хранит:
 *  - Pos1/Pos2 — форму платформы лифта (задаётся Elevator Glue)
 *  - floors — список этажей (мировые координаты точки остановки), отсортированный по Y
 *  - callQueue — очередь этажей, на которые лифт должен приехать (от вызывателей / рычага)
 *  - currentFloorIndex — на каком этаже лифт сейчас стоит (-1 если неизвестно)
 *  - lastArrivalTime — игровое время последнего прибытия (для паузы дверей в 6 сек)
 *  - shaftId — стабильный идентификатор ЭТОЙ шахты, не зависящий от текущего
 *    этажа контроллера (в отличие от его BlockPos, который меняется при каждом
 *    приезде). Кнопки вызова привязываются именно к нему, а актуальный BlockPos
 *    контроллера ищут через ElevatorShaftRegistry.
 *
 * Пока лифт едет — этот BlockEntity физически не существует (блок разобран и
 * является частью MovingElevatorEntity). Все данные "переезжают" вместе с лифтом
 * через captureRideData()/restoreAfterRide() и восстанавливаются в НОВОМ BlockEntity,
 * созданном на месте прибытия — включая shaftId, иначе новый BlockEntity получил
 * бы новый случайный идентификатор и все кнопки отвязались бы заново.
 */
public class ElevatorControllerBlockEntity extends BlockEntity {

    // 6 секунд = 120 тиков. Минимальное время, которое лифт "стоит с открытыми
    // дверями" после прибытия/вызова, прежде чем поедет дальше.
    public static final int DOOR_DELAY_TICKS = 120;

    // Генерируется один раз при создании этого объекта в памяти. Для только что
    // размещённого контроллера это и есть его постоянный ID шахты. Для
    // контроллера, загруженного из NBT или восстановленного после поездки,
    // это значение будет тут же перезаписано настоящим (старым) shaftId.
    private UUID shaftId = UUID.randomUUID();

    private BlockPos pos1;
    private BlockPos pos2;

    private final List<BlockPos> floors = new ArrayList<>();
    private final List<Integer> callQueue = new ArrayList<>();

    private int currentFloorIndex = -1;
    private long lastArrivalTime = 0L;
    private boolean hasArrived = false;

    public ElevatorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELEVATOR_CONTROLLER_BE.get(), pos, state);
    }

    public UUID getShaftId() {
        return shaftId;
    }

    /**
     * Вызывается движком, когда этот BlockEntity подгружается в мир — как при
     * обычной загрузке чанка, так и сразу после того, как level.setBlock()
     * пересобрал контроллер на новом этаже. Это единственное надёжное место,
     * где мы точно знаем актуальный BlockPos контроллера, поэтому здесь и
     * обновляем реестр шахт.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            ElevatorShaftRegistry.register(serverLevel, shaftId, worldPosition);
        }
    }

    // ---------------------------------------------------------------------
    // Форма платформы (Elevator Glue)
    // ---------------------------------------------------------------------

    public void setElevatorData(BlockPos pos1, BlockPos pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        setChanged();
    }

    public BlockPos getPos1() { return pos1; }
    public BlockPos getPos2() { return pos2; }

    public boolean hasValidData() {
        return this.pos1 != null && this.pos2 != null;
    }

    // ---------------------------------------------------------------------
    // Этажи (Elevator Floor Point item)
    // ---------------------------------------------------------------------

    /**
     * Добавляет точку этажа (по её Y). Список всегда остаётся отсортированным
     * снизу вверх. Возвращает индекс добавленного (или уже существующего) этажа.
     */
    public int addFloorPoint(BlockPos pos) {
        BlockPos immutable = pos.immutable();

        for (int i = 0; i < floors.size(); i++) {
            if (floors.get(i).getY() == immutable.getY()) {
                floors.set(i, immutable);
                setChanged();
                return i;
            }
        }

        int insertIndex = 0;
        while (insertIndex < floors.size() && floors.get(insertIndex).getY() < immutable.getY()) {
            insertIndex++;
        }
        floors.add(insertIndex, immutable);

        // Индексы в очереди вызовов и текущий этаж сдвигаются, если вставили новый этаж ниже них
        for (int i = 0; i < callQueue.size(); i++) {
            if (callQueue.get(i) >= insertIndex) {
                callQueue.set(i, callQueue.get(i) + 1);
            }
        }
        if (currentFloorIndex >= insertIndex) {
            currentFloorIndex++;
        }

        // Если контроллер физически стоит на этой же высоте — считаем, что мы уже "на этом этаже"
        if (currentFloorIndex == -1 && this.getBlockPos().getY() == immutable.getY()) {
            currentFloorIndex = insertIndex;
            hasArrived = true;
        }

        setChanged();
        return insertIndex;
    }

    public List<BlockPos> getFloors() {
        return Collections.unmodifiableList(floors);
    }

    public int getFloorCount() {
        return floors.size();
    }

    public int getCurrentFloorIndex() {
        return currentFloorIndex;
    }

    // ---------------------------------------------------------------------
    // Вызовы (кнопка вызывателя / рычаг на контроллере)
    // ---------------------------------------------------------------------

    /**
     * Ставит этаж в очередь вызовов. Возвращает false, если этаж некорректный
     * либо уже стоит в очереди/лифт уже там и ничего не ожидает.
     */
    public boolean requestFloor(int floorIndex) {
        if (floorIndex < 0 || floorIndex >= floors.size()) return false;
        if (floorIndex == currentFloorIndex && callQueue.isEmpty()) return false;
        if (callQueue.contains(floorIndex)) return false;

        callQueue.add(floorIndex);
        setChanged();
        return true;
    }

    /**
     * Ручное управление с самого контроллера: едем на следующий этаж выше,
     * а если мы уже на самом верхнем — едем в самый нижний.
     */
    public void requestManualNext() {
        if (floors.size() < 2) return;

        int next = (currentFloorIndex < 0) ? 0 : (currentFloorIndex + 1) % floors.size();

        // Ручной вызов имеет приоритет — ставим его в начало очереди
        callQueue.remove(Integer.valueOf(next));
        callQueue.add(0, next);
        setChanged();
    }

    // ---------------------------------------------------------------------
    // Тик — диспетчер лифта
    // ---------------------------------------------------------------------

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        if (callQueue.isEmpty()) return;
        if (!hasValidData() || floors.isEmpty()) return;

        long now = level.getGameTime();
        if (hasArrived && (now - lastArrivalTime) < DOOR_DELAY_TICKS) {
            return; // двери ещё "закрываются" — рано ехать
        }

        int targetFloor = callQueue.remove(0);
        setChanged();

        if (targetFloor == currentFloorIndex) {
            // Уже на месте — просто сбрасываем таймер, как будто открыли/закрыли двери
            lastArrivalTime = now;
            hasArrived = true;
            return;
        }

        if (targetFloor < 0 || targetFloor >= floors.size()) return;

        BlockPos targetPos = floors.get(targetFloor);

        // Pos1/Pos2 — абсолютные координаты; сдвигаем их на ту же дельту по Y,
        // на которую поедет сам контроллер, чтобы после прибытия платформа
        // осталась в правильном месте относительно новой высоты контроллера.
        int deltaY = targetPos.getY() - pos.getY();
        BlockPos shiftedPos1 = pos1.offset(0, deltaY, 0);
        BlockPos shiftedPos2 = pos2.offset(0, deltaY, 0);

        ElevatorAssembler.assembleAndSpawn(level, pos, pos1, pos2, targetFloor, targetPos.getY(),
                captureRideData(shiftedPos1, shiftedPos2));
    }

    // ---------------------------------------------------------------------
    // Передача данных через поездку (пока блок разобран)
    // ---------------------------------------------------------------------

    public CompoundTag captureRideData() {
        return captureRideData(this.pos1, this.pos2);
    }

    /**
     * То же самое, но форма платформы (Pos1/Pos2) передаётся отдельно — уже
     * сдвинутая на дельту предстоящей поездки. Pos1/Pos2 хранятся как
     * АБСОЛЮТНЫЕ мировые координаты, поэтому при переезде контроллера на
     * другую высоту их нужно сдвигать вместе с ним; иначе на новом этаже
     * isInside-проверка в ElevatorAssembler начнёт сравнивать текущий Y
     * контроллера со старым диапазоном Y и решит, что контроллер вне
     * платформы — следующая поездка тихо не запустится (assembleAndSpawn
     * просто вернёт false, без исключений).
     */
    public CompoundTag captureRideData(BlockPos pos1ForArrival, BlockPos pos2ForArrival) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("ShaftId", shaftId);

        ListTag floorList = new ListTag();
        for (BlockPos f : floors) floorList.add(LongTag.valueOf(f.asLong()));
        tag.put("Floors", floorList);

        ListTag queueList = new ListTag();
        for (int q : callQueue) queueList.add(IntTag.valueOf(q));
        tag.put("CallQueue", queueList);

        if (pos1ForArrival != null) tag.putLong("Pos1", pos1ForArrival.asLong());
        if (pos2ForArrival != null) tag.putLong("Pos2", pos2ForArrival.asLong());

        return tag;
    }

    /**
     * Вызывается у НОВОГО BlockEntity, созданного на этаже прибытия, чтобы вернуть
     * ему все данные лифта (этажи, очередь и т.п.), которые "путешествовали" внутри
     * MovingElevatorEntity, пока блока физически не существовало.
     */
    public void restoreAfterRide(CompoundTag tag, int arrivedFloorIndex, long arrivalGameTime) {
        if (tag.hasUUID("ShaftId")) {
            this.shaftId = tag.getUUID("ShaftId");
        }

        floors.clear();
        if (tag.contains("Floors")) {
            ListTag list = tag.getList("Floors", Tag.TAG_LONG);
            for (int i = 0; i < list.size(); i++) {
                floors.add(BlockPos.of(((LongTag) list.get(i)).getAsLong()));
            }
        }

        callQueue.clear();
        if (tag.contains("CallQueue")) {
            ListTag list = tag.getList("CallQueue", Tag.TAG_INT);
            for (int i = 0; i < list.size(); i++) {
                callQueue.add(((IntTag) list.get(i)).getAsInt());
            }
        }

        if (tag.contains("Pos1")) this.pos1 = BlockPos.of(tag.getLong("Pos1"));
        if (tag.contains("Pos2")) this.pos2 = BlockPos.of(tag.getLong("Pos2"));

        this.currentFloorIndex = arrivedFloorIndex;
        this.lastArrivalTime = arrivalGameTime;
        this.hasArrived = true;

        if (level instanceof ServerLevel serverLevel) {
            ElevatorShaftRegistry.register(serverLevel, shaftId, worldPosition);
        }

        setChanged();
    }

    // ---------------------------------------------------------------------
    // NBT (сохранение мира)
    // ---------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putUUID("ShaftId", this.shaftId);
        if (this.pos1 != null) tag.putLong("Pos1", this.pos1.asLong());
        if (this.pos2 != null) tag.putLong("Pos2", this.pos2.asLong());

        ListTag floorList = new ListTag();
        for (BlockPos f : floors) floorList.add(LongTag.valueOf(f.asLong()));
        tag.put("Floors", floorList);

        ListTag queueList = new ListTag();
        for (int q : callQueue) queueList.add(IntTag.valueOf(q));
        tag.put("CallQueue", queueList);

        tag.putInt("CurrentFloorIndex", currentFloorIndex);
        tag.putLong("LastArrivalTime", lastArrivalTime);
        tag.putBoolean("HasArrived", hasArrived);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // Старые сохранения (до этого фикса) тега не содержат — тогда просто
        // генерируем новый ID. Раньше линковка всё равно ломалась при первом же
        // переезде лифта, так что все существующие кнопки нужно перепривязать.
        this.shaftId = tag.hasUUID("ShaftId") ? tag.getUUID("ShaftId") : UUID.randomUUID();
        if (tag.contains("Pos1")) this.pos1 = BlockPos.of(tag.getLong("Pos1"));
        if (tag.contains("Pos2")) this.pos2 = BlockPos.of(tag.getLong("Pos2"));

        floors.clear();
        if (tag.contains("Floors")) {
            ListTag list = tag.getList("Floors", Tag.TAG_LONG);
            for (int i = 0; i < list.size(); i++) {
                floors.add(BlockPos.of(((LongTag) list.get(i)).getAsLong()));
            }
        }

        callQueue.clear();
        if (tag.contains("CallQueue")) {
            ListTag list = tag.getList("CallQueue", Tag.TAG_INT);
            for (int i = 0; i < list.size(); i++) {
                callQueue.add(((IntTag) list.get(i)).getAsInt());
            }
        }

        this.currentFloorIndex = tag.contains("CurrentFloorIndex") ? tag.getInt("CurrentFloorIndex") : -1;
        this.lastArrivalTime = tag.getLong("LastArrivalTime");
        this.hasArrived = tag.getBoolean("HasArrived");
    }
}
