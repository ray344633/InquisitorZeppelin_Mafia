package com.scir4y.zeppelinmurdermod.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class MovingElevatorEntity extends Entity {
    private static final net.minecraft.network.syncher.EntityDataAccessor<CompoundTag> DATA_BLOCKS = SynchedEntityData.defineId(MovingElevatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.COMPOUND_TAG);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Float> DATA_TARGET_Y = SynchedEntityData.defineId(MovingElevatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.FLOAT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> DATA_IS_MOVING = SynchedEntityData.defineId(MovingElevatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

    private Map<BlockPos, BlockState> blocks = new HashMap<>();
    
    private double targetY;
    private boolean isMoving = false;
    private net.minecraft.world.phys.AABB elevatorBounds = new net.minecraft.world.phys.AABB(-0.5, 0, -0.5, 0.5, 1, 0.5);

    public MovingElevatorEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
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
        
        int minX = 0, minY = 0, minZ = 0;
        int maxX = 0, maxY = 0, maxZ = 0;
        boolean first = true;
        for(BlockPos p : blocks.keySet()) {
            if(first) {
                minX = maxX = p.getX();
                minY = maxY = p.getY();
                minZ = maxZ = p.getZ();
                first = false;
            } else {
                minX = Math.min(minX, p.getX());
                minY = Math.min(minY, p.getY());
                minZ = Math.min(minZ, p.getZ());
                maxX = Math.max(maxX, p.getX());
                maxY = Math.max(maxY, p.getY());
                maxZ = Math.max(maxZ, p.getZ());
            }
        }
        this.elevatorBounds = new net.minecraft.world.phys.AABB(minX - 0.5, minY, minZ - 0.5, maxX + 0.5, maxY + 1.0, maxZ + 0.5);
        this.setBoundingBox(this.elevatorBounds.move(this.position()));
    }

    public Map<BlockPos, BlockState> getBlocks() {
        return blocks;
    }
    
    @Override
    public void tick() {
        super.tick();
        
        this.setBoundingBox(this.elevatorBounds.move(this.position()));
        
        if (isMoving) {
            double speed = 0.1;
            double nextY = this.getY();
            
            if (this.getY() < targetY) {
                nextY = Math.min(this.getY() + speed, targetY);
            } else if (this.getY() > targetY) {
                nextY = Math.max(this.getY() - speed, targetY);
            }
            
            double deltaY = nextY - this.getY();
            this.setPos(this.getX(), nextY, this.getZ());
            
            // Push entities in the direction of movement (up or down)
            if (deltaY != 0) {
                double padding = 0.15;
                net.minecraft.world.phys.AABB pushBox = this.getBoundingBox().inflate(padding, 0.0, padding);
                java.util.List<Entity> passengers = this.level().getEntities(this, pushBox, e ->
                    e instanceof net.minecraft.world.entity.LivingEntity || e instanceof net.minecraft.world.entity.item.ItemEntity);
                for (Entity e : passengers) {
                    // Entity is ON TOP of the platform (its feet are at or above the platform floor)
                    double platformTop = this.getBoundingBox().maxY;
                    double platformFloor = this.getBoundingBox().minY;
                    boolean isOnPlatform = e.getBoundingBox().minY >= platformFloor - 0.5
                                        && e.getBoundingBox().minY <= platformTop + 0.5;
                    if (isOnPlatform) {
                        e.move(net.minecraft.world.entity.MoverType.SHULKER_BOX, new net.minecraft.world.phys.Vec3(0, deltaY, 0));
                        e.resetFallDistance();
                        if (deltaY > 0) e.setOnGround(true);
                    }
                }
            }
            
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
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos worldPos = center.offset(entry.getKey());
            this.level().setBlock(worldPos, entry.getValue(), 3);
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
    public void onSyncedDataUpdated(net.minecraft.network.syncher.EntityDataAccessor<?> key) {
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
