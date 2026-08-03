package com.scir4y.zeppelinmurdermod.content.elevator.item;

import com.scir4y.zeppelinmurdermod.registry.ModBlocks;
import com.scir4y.zeppelinmurdermod.content.elevator.block.entity.ElevatorControllerBlockEntity;
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
import net.minecraft.world.level.block.state.BlockState;

/**
 * Инструмент для создания точек этажей.
 *
 * 1. Правый клик по блоку Elevator Controller — привязывает инструмент к этому контроллеру.
 * 2. Правый клик по любому другому блоку — добавляет его позицию (точнее, её Y)
 *    как точку остановки этажа для привязанного контроллера.
 * 3. Shift + правый клик — сбрасывает привязку.
 */
public class ElevatorFloorPointItem extends Item {

    public ElevatorFloorPointItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();

        if (player.isSecondaryUseActive()) {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();
            tag.remove("ControllerPos");
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("Controller binding cleared.").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        BlockState clickedState = level.getBlockState(clickedPos);

        // Клик по контроллеру — привязываемся к нему
        if (clickedState.is(ModBlocks.ELEVATOR_CONTROLLER_BLOCK.get())) {
            tag.putLong("ControllerPos", clickedPos.asLong());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("Bound to elevator controller at " + clickedPos.toShortString()).withStyle(ChatFormatting.AQUA), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        // Иначе — добавляем точку этажа к уже привязанному контроллеру
        if (!tag.contains("ControllerPos")) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("Bind to an Elevator Controller first (right-click it).").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            BlockPos controllerPos = BlockPos.of(tag.getLong("ControllerPos"));
            if (level.getBlockEntity(controllerPos) instanceof ElevatorControllerBlockEntity controllerBE) {
                int index = controllerBE.addFloorPoint(clickedPos);
                player.displayClientMessage(Component.literal("Floor point #" + (index + 1) + " set at Y=" + clickedPos.getY()).withStyle(ChatFormatting.GREEN), true);
            } else {
                player.displayClientMessage(Component.literal("Bound controller not found, rebind it.").withStyle(ChatFormatting.RED), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
