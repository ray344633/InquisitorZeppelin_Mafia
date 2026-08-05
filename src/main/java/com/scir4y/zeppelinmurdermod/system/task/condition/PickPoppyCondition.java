package com.scir4y.zeppelinmurdermod.system.task.condition;

import com.scir4y.zeppelinmurdermod.system.task.TaskCondition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Triggers when a poppy (Items.POPPY) is in the player's inventory.
 */

public class PickPoppyCondition implements TaskCondition {

    @Override
    public boolean check(ServerPlayer player, ServerLevel level) {
        // check, has player a poppy or not
        if (player.getInventory().countItem(Items.POPPY) > 0) {

            // check all slots
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);

                // if there is a poppy remove 1
                if (stack.is(Items.POPPY)) {
                    stack.shrink(1);
                    return true;
                }
            }
        }

        // if player has nit a poppy returns false
        return false;
    }
}
