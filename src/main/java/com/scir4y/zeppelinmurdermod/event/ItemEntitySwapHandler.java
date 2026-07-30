package com.scir4y.zeppelinmurdermod.event;

import com.mojang.logging.LogUtils;
import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.entity.MODENTITIES;
import com.scir4y.zeppelinmurdermod.entity.PhysicalItemEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.slf4j.Logger;

/**
 * Transparently replaces vanilla ItemEntity instances with PhysicalItemEntity
 * as they join the level, so the model-based hitbox applies no matter where
 * the drop came from (block break, mob drop, player Q, dispenser, /summon, etc.)
 * without patching every one of those call sites individually.
 *
 * Runs server-side only: the server is authoritative over which entity type
 * gets spawned, and it replicates the correct EntityType to the client via
 * the normal spawn packet -- no separate client-side swap is needed.
 *
 * STATE TRANSFER: vanilla ItemEntity has no public getters for pickup delay,
 * thrower/owner UUID, or age (setters only), so those can't be read field by
 * field. Instead we save the *entire* entity to NBT (saveWithoutId) and load
 * it back into the replacement (load) -- this is the same mechanism vanilla
 * uses for entities crossing dimensions, and it carries over pickup delay,
 * thrower, age, item stack and position/motion all at once. Without this,
 * the replacement previously started with 0 pickup delay, which meant a
 * player's own thrown item could be picked back up on the very next tick --
 * looking exactly like the item "disappearing and coming back".
 */
@EventBusSubscriber(modid = ZeppelinMurderMod.MODID)
public class ItemEntitySwapHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }

        // Already our type, or not an ItemEntity at all -- nothing to do.
        if (!(event.getEntity() instanceof ItemEntity itemEntity)
                || itemEntity instanceof PhysicalItemEntity) {
            return;
        }

        LOGGER.info("[ItemEntitySwapHandler] swapping vanilla ItemEntity (item={}) for PhysicalItemEntity",
                itemEntity.getItem().getItem());

        event.setCanceled(true);

        CompoundTag savedState = itemEntity.saveWithoutId(new CompoundTag());

        PhysicalItemEntity replacement = new PhysicalItemEntity(MODENTITIES.PHYSICAL_ITEM.get(), level);
        replacement.load(savedState);

        level.addFreshEntity(replacement);

        // Discard the original so it doesn't linger as a duplicate.
        itemEntity.discard();
    }
}