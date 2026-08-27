package com.scir4y.zeppelinmurdermod.content.footstool.entity;

import com.scir4y.zeppelinmurdermod.registry.ModEntities;
import com.scir4y.zeppelinmurdermod.content.footstool.block.FootstoolBlock;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
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
        Vec3 forward = Vec3.directionFromRotation(0, livingEntity.getYRot());
        return new Vec3(this.getX() + forward.x * 0.75, this.getY(), this.getZ() + forward.z * 0.75);
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
