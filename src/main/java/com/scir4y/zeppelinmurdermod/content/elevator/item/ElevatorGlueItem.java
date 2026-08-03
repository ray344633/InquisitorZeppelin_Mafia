package com.scir4y.zeppelinmurdermod.content.elevator.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ElevatorGlueItem extends Item {

    public ElevatorGlueItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        // Shift + Right-Click to reset selection
        if (player.isSecondaryUseActive()) {
            tag.remove("Pos1");
            tag.remove("Pos2");
            // Update data on the item stack
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("Selection cleared.").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        BlockPos pos = context.getClickedPos();

        // If Pos1 is not set yet
        if (!tag.contains("Pos1")) {
            tag.putLong("Pos1", pos.asLong());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag)); // Save on both client and server

            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("Point 1: " + pos.toShortString()).withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        // If Pos1 exists, but Pos2 does not
        else if (!tag.contains("Pos2")) {
            tag.putLong("Pos2", pos.asLong());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("Point 2: " + pos.toShortString()).withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }
}