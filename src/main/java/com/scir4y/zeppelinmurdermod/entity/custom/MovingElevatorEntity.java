package com.scir4y.zeppelinmurdermod.entity.custom;

import com.scir4y.zeppelinmurdermod.block.MODBLOCKS;
import com.scir4y.zeppelinmurdermod.block.entity.custom.ElevatorControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

public class MovingElevatorEntity extends Entity {
    private static final EntityDataAccessor<CompoundTag> DATA_BLOCKS = SynchedEntityData.defineId(MovingElevatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Float> DATA_TARGET_Y = SynchedEntityData.defineId(MovingElevatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_IS_MOVING = SynchedEntityData.defineId(MovingElevatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

    private Map<BlockPos, BlockState> blocks = new HashMap<>();
    private double targetY;
    private boolean isMoving = false;
    private AABB elevatorBounds = new AABB(-0.5, 0, -0.5, 0.5, 1, 0.5);

    // Данные контроллера, "путешествующие" вместе с сущностью, пока сам блок
    // контроллера физически разобран. Восстанавливаются в disassemble().
    // Существуют только на сервере, синхронизация клиенту не требуется.
    private int rideTargetFloorIndex = -1;
    @Nullable
    private CompoundTag controllerRideData = null;

    public void setRideMetadata(int targetFloorIndex, @Nullable CompoundTag controllerRideData) {
        this.rideTargetFloorIndex = targetFloorIndex;
        this.controllerRideData = controllerRideData;
    }

    public MovingElevatorEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    @Override
    protected AABB makeBoundingBox() {
        if (this.elevatorBounds != null) {
            return this.elevatorBounds.move(this.position());
        }
        return super.makeBoundingBox();
    }

    public void setTargetY(double y) {
        this.targetY = y;
        this.isMoving = true;
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_TARGET_Y, (float) y);
            this.entityData.set(DATA_IS_MOVING, true);
        }
    }

    public void setBlocks(Map<BlockPos, BlockState> blocks) {
        this.blocks = blocks;
        recalculateBounds();

        if (!this.level().isClientSide()) {
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
                CompoundTag blockTag = new CompoundTag();
                blockTag.putInt("X", entry.getKey().getX());
                blockTag.putInt("Y", entry.getKey().getY());
                blockTag.putInt("Z", entry.getKey().getZ());
                blockTag.putInt("StateId", Block.getId(entry.getValue()));
                list.add(blockTag);
            }
            tag.put("ElevatorBlocks", list);
            this.entityData.set(DATA_BLOCKS, tag);
        }
    }

    private void recalculateBounds() {
        if (this.blocks.isEmpty()) return;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockPos p : blocks.keySet()) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());
        }

        this.elevatorBounds = new AABB(minX - 0.5, minY, minZ - 0.5, maxX + 0.5, maxY + 1.0, maxZ + 0.5);
        this.refreshDimensions();
        this.setBoundingBox(this.makeBoundingBox());
    }

    public Map<BlockPos, BlockState> getBlocks() {
        return blocks;
    }

    @Override
    public void tick() {
        super.tick();

        if (isMoving) {
            double speed = 0.2;
            double nextY = this.getY();

            if (this.getY() < targetY) {
                nextY = Math.min(this.getY() + speed, targetY);
            } else if (this.getY() > targetY) {
                nextY = Math.max(this.getY() - speed, targetY);
            }

            double deltaY = nextY - this.getY();
            Vec3 motion = new Vec3(0, deltaY, 0);

            // Викликаємо наш адаптований обробник колізій ДО оновлення позиції
            ElevatorCollisionHandler.handleCollisions(this, motion);

            this.setPos(this.getX(), nextY, this.getZ());

            if (nextY == targetY) {
                isMoving = false;
                if (!this.level().isClientSide()) {
                    this.entityData.set(DATA_IS_MOVING, false);
                    disassemble();
                }
            }
        }
    }

    private void disassemble() {
        BlockPos center = this.blockPosition();
        BlockPos controllerRelPos = null;

        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos worldPos = center.offset(entry.getKey());
            this.level().setBlock(worldPos, entry.getValue(), 3);

            if (entry.getValue().is(MODBLOCKS.ELEVATOR_CONTROLLER_BLOCK.get())) {
                controllerRelPos = entry.getKey();
            }
        }

        // Возвращаем контроллеру его "память" (этажи, очередь вызовов, форму платформы),
        // которую он передал нам перед тем, как его блок был разобран.
        if (controllerRelPos != null && controllerRideData != null && !this.level().isClientSide()) {
            BlockPos controllerWorldPos = center.offset(controllerRelPos);
            if (this.level().getBlockEntity(controllerWorldPos) instanceof ElevatorControllerBlockEntity controllerBE) {
                controllerBE.restoreAfterRide(controllerRideData, rideTargetFloorIndex, this.level().getGameTime());
            }
        }

        this.discard();
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BLOCKS, new CompoundTag());
        builder.define(DATA_TARGET_Y, 0.0f);
        builder.define(DATA_IS_MOVING, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_BLOCKS.equals(key) && this.level().isClientSide()) {
            CompoundTag tag = this.entityData.get(DATA_BLOCKS);
            blocks.clear();
            if (tag.contains("ElevatorBlocks", Tag.TAG_LIST)) {
                ListTag list = tag.getList("ElevatorBlocks", Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag blockTag = list.getCompound(i);
                    BlockPos pos = new BlockPos(blockTag.getInt("X"), blockTag.getInt("Y"), blockTag.getInt("Z"));
                    BlockState state = Block.stateById(blockTag.getInt("StateId"));
                    blocks.put(pos, state);
                }
            }
            recalculateBounds();
        } else if (DATA_TARGET_Y.equals(key) && this.level().isClientSide()) {
            this.targetY = this.entityData.get(DATA_TARGET_Y);
        } else if (DATA_IS_MOVING.equals(key) && this.level().isClientSide()) {
            this.isMoving = this.entityData.get(DATA_IS_MOVING);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        blocks.clear();
        if (tag.contains("ElevatorBlocks", Tag.TAG_LIST)) {
            ListTag list = tag.getList("ElevatorBlocks", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag blockTag = list.getCompound(i);
                BlockPos pos = new BlockPos(blockTag.getInt("X"), blockTag.getInt("Y"), blockTag.getInt("Z"));
                BlockState state = Block.stateById(blockTag.getInt("StateId"));
                blocks.put(pos, state);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            CompoundTag blockTag = new CompoundTag();
            blockTag.putInt("X", entry.getKey().getX());
            blockTag.putInt("Y", entry.getKey().getY());
            blockTag.putInt("Z", entry.getKey().getZ());
            blockTag.putInt("StateId", Block.getId(entry.getValue()));
            list.add(blockTag);
        }
        tag.put("ElevatorBlocks", list);
    }
}