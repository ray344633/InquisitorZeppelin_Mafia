package com.scir4y.zeppelinmurdermod.item.custom;

import com.scir4y.zeppelinmurdermod.client.render.gui.NoteEditScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WritableNoteItem extends Item {
    public WritableNoteItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // do not use unless main hand
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        // get item in hand
        ItemStack itemstack = player.getItemInHand(hand);

        // if it is client side open NoteEditScreen
        if (level.isClientSide()) {
            Minecraft.getInstance().setScreen(new NoteEditScreen(player, itemstack, hand));
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}