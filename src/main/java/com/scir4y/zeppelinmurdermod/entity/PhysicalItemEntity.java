package com.scir4y.zeppelinmurdermod.entity;

import com.mojang.logging.LogUtils;
import com.scir4y.zeppelinmurdermod.client.util.ItemModelUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

/**
 * A drop-in replacement for vanilla ItemEntity whose hitbox size reflects
 * the actual item/block model instead of the vanilla flat 0.25x0.25 box.
 *
 * DEBUG LOGGING: this version logs every getDimensions() call and every
 * setItem() call so we can see in the log whether:
 *   (a) this class is actually being used at all (i.e. the swap worked),
 *   (b) ItemModelUtils is returning something other than the 0.25x0.25
 *       fallback, and
 *   (c) refreshDimensions() is actually changing entity.getBoundingBox().
 * Once confirmed working, strip the LOGGER lines back out -- this will spam
 * the log heavily with many items on the ground.
 */
public class PhysicalItemEntity extends ItemEntity {

    private static final Logger LOGGER = LogUtils.getLogger();

    public PhysicalItemEntity(EntityType<PhysicalItemEntity> type, Level level) {
        super(type, level);
        LOGGER.info("[PhysicalItemEntity] constructed via (EntityType, Level) ctor, id={}", this.getId());
    }

    public PhysicalItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        this(MODENTITIES.PHYSICAL_ITEM.get(), level);
        this.setPos(x, y, z);
        this.setItem(stack);
    }

    public PhysicalItemEntity(Level level, double x, double y, double z,
                              ItemStack stack, double dx, double dy, double dz) {
        this(level, x, y, z, stack);
        this.setDeltaMovement(dx, dy, dz);
    }

    @Override
    public void setItem(ItemStack stack) {
        super.setItem(stack);
        LOGGER.info("[PhysicalItemEntity] setItem called: item={}, clientSide={}, id={}",
                stack.isEmpty() ? "EMPTY" : stack.getItem(), this.level().isClientSide(), this.getId());
        this.refreshDimensions();
        LOGGER.info("[PhysicalItemEntity] after refreshDimensions: boundingBox={}", this.getBoundingBox());
    }

    /**
     * Called on the client when a synced-data value (incl. the item stack,
     * which ItemEntity syncs via a private EntityDataAccessor rather than
     * through setItem()) is updated from a server packet. setItem() is NOT
     * invoked on that path, so this is the actual hook we need to refresh
     * the cached hitbox dimensions on the client.
     */
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        LOGGER.info("[PhysicalItemEntity] onSyncedDataUpdated: key={}, clientSide={}, id={}",
                key, this.level().isClientSide(), this.getId());
        this.refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (this.level().isClientSide()) {
            ItemStack stack = this.getItem();
            if (!stack.isEmpty()) {
                EntityDimensions dims = ItemModelUtils.getOrComputeDimensions(stack);
                LOGGER.info("[PhysicalItemEntity] getDimensions() -> item={}, width={}, height={}",
                        stack.getItem(), dims.width(), dims.height());
                return dims;
            }
        }
        return super.getDimensions(pose);
    }
}