package com.scir4y.zeppelinmurdermod.elevator;

import com.scir4y.zeppelinmurdermod.entity.MODENTITIES;
import com.scir4y.zeppelinmurdermod.entity.custom.MovingElevatorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

import static com.scir4y.zeppelinmurdermod.block.MODBLOCKS.ELEVATOR_CONTROLLER_BLOCK;
import static com.scir4y.zeppelinmurdermod.block.MODBLOCKS.POLISHED_BRASS_BLOCK;

public class ElevatorAssembler {

    /**
     * Собирает платформу лифта (блоки внутри Pos1-Pos2) в летающую сущность
     * и отправляет её к targetY.
     *
     * @param targetFloorIndex индекс этажа, на который едем (нужен, чтобы по прибытии
     *                         контроллер знал, где он оказался)
     * @param targetY          мировая координата Y, на которой должен оказаться контроллер
     * @param controllerRideData снимок данных контроллера (этажи, очередь и т.п.),
     *                            который "путешествует" вместе с сущностью, пока блок разобран
     */
    public static boolean assembleAndSpawn(Level level, BlockPos controllerPos, BlockPos pos1, BlockPos pos2,
                                            int targetFloorIndex, int targetY, CompoundTag controllerRideData) {
        // 1. Проверяем, находится ли контроллер внутри зоны Pos1 - Pos2
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        boolean isInside = controllerPos.getX() >= minX && controllerPos.getX() <= maxX &&
                controllerPos.getY() >= minY && controllerPos.getY() <= maxY &&
                controllerPos.getZ() >= minZ && controllerPos.getZ() <= maxZ;

        if (!isInside) {
            return false; // Контроллер не находится в зоне лифта
        }

        Map<BlockPos, BlockState> blocks = new HashMap<>();

        // 2. Сканируем всю область между Pos1 и Pos2
        for (BlockPos current : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            BlockState state = level.getBlockState(current);

            // Проверяем разрешенные блоки
            if (state.is(POLISHED_BRASS_BLOCK.get()) || state.is(ELEVATOR_CONTROLLER_BLOCK.get())) {
                // Относительная позиция от контроллера
                blocks.put(current.immutable().subtract(controllerPos), state);
            }
        }

        if (blocks.isEmpty()) {
            return false;
        }

        // 3. Удаляем блоки из мира
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos worldPos = controllerPos.offset(entry.getKey());
            level.removeBlock(worldPos, false);
        }

        // 4. Спавним энтити
        MovingElevatorEntity entity = MODENTITIES.MOVING_ELEVATOR.get().create(level);
        if (entity != null) {
            entity.setPos(controllerPos.getX() + 0.5, controllerPos.getY(), controllerPos.getZ() + 0.5);
            entity.setBlocks(blocks);
            entity.setRideMetadata(targetFloorIndex, controllerRideData);
            entity.setTargetY(targetY);
            level.addFreshEntity(entity);
        }

        return true;
    }
}
