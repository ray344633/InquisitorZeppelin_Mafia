package com.scir4y.zeppelinmurdermod.content.footstool.entity;

import com.scir4y.zeppelinmurdermod.registry.ModEntities;
import com.scir4y.zeppelinmurdermod.content.footstool.block.FootstoolBlock;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

public class FootstoolEntity extends Entity implements IEntityWithComplexSpawn {

    public FootstoolEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public FootstoolEntity(Level level) {
        this(ModEntities.FOOTSTOOL_SEAT.get(), level);
    }

    public static EntityType.Builder<FootstoolEntity> build(EntityType.Builder<FootstoolEntity> builder) {
        return builder.sized(1f, 0.31f);
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        AABB bb = getBoundingBox();
        Vec3 diff = new Vec3(x, y+0.28f, z).subtract(bb.getCenter());
        setBoundingBox(bb.move(diff));
    }

    @Override
    protected void positionRider(Entity entity, MoveFunction callback) {
        if (!this.hasPassenger(entity))
            return;
        double heightOffset = this.getPassengerRidingPosition(entity).y - entity.getVehicleAttachmentPoint(this).y + 0.2f;
        callback.accept(entity, this.getX(), heightOffset, this.getZ());
    }

    @Override
    public void onPassengerTurned(Entity entity) {
        entity.setYHeadRot(entity.getYRot());
    }

    @Override
    public void setDeltaMovement(Vec3 vec) {
        // seats never move
    }

    @Override
    public void tick() {
        if (level().isClientSide)
            return;

        boolean footstoolStillThere = level().getBlockState(blockPosition()).getBlock() instanceof FootstoolBlock;
        if (isVehicle() && footstoolStillThere)
            return;

        this.discard();
    }

    @Override
    protected boolean canRide(Entity entity) {
        // Fake players (dispensers/automation) shouldn't be able to sit down
        return !(entity instanceof FakePlayer);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Level level = this.level();
        BlockPos center = this.blockPosition();
        float yaw = livingEntity.getYRot();

        int[] angleOffsets = {0, 45, -45, 90};

        for (int offset : angleOffsets) {
            Vec3 dir = Vec3.directionFromRotation(0, yaw + offset);
            int dx = Math.round((float) dir.x);
            int dz = Math.round((float) dir.z);

            if (dx == 0 && dz == 0) {
                continue;
            }

            BlockPos candidate = center.offset(dx, 0, dz);
            if (hasAirSpace(level, candidate)) {
                return new Vec3(candidate.getX() + 0.5, candidate.getY(), candidate.getZ() + 0.5);
            }
        }

        return new Vec3(this.getX(), this.getY() + 0.7, this.getZ());
    }

    private boolean hasAirSpace(Level level, BlockPos pos) {
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());
        return feet.getCollisionShape(level, pos).isEmpty()
                && head.getCollisionShape(level, pos.above()).isEmpty();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // no synced data needed
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
    }

    public static class Render extends EntityRenderer<FootstoolEntity> {

        public Render(EntityRendererProvider.Context context) {
            super(context);
            this.shadowRadius = 0f;
        }

        @Override
        public boolean shouldRender(FootstoolEntity entity, Frustum frustum, double x, double y, double z) {
            return false;
        }

        @Override
        public ResourceLocation getTextureLocation(FootstoolEntity entity) {
            return null;
        }
    }
}
