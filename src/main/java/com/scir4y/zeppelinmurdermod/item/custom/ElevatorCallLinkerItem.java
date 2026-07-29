package com.scir4y.zeppelinmurdermod.item.custom;

import com.scir4y.zeppelinmurdermod.block.MODBLOCKS;
import com.scir4y.zeppelinmurdermod.block.entity.custom.ElevatorCallerBlockEntity;
import com.scir4y.zeppelinmurdermod.block.entity.custom.ElevatorControllerBlockEntity;
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

import java.util.UUID;

/**
 * Инструмент для соединения кнопки вызывателя с этажом конкретного контроллера.
 *
 * 1. Правый клик по Elevator Controller — привязывает инструмент к контроллеру
 *    и выбирает этаж #1. Повторный клик по ТОМУ ЖЕ контроллеру циклически
 *    переключает выбранный этаж (1 -> 2 -> ... -> N -> 1).
 * 2. Правый клик по Elevator Caller Block — привязывает эту кнопку к выбранному
 *    контроллеру и этажу.
 * 3. Shift + правый клик — сбрасывает инструмент.
 */
public class ElevatorCallLinkerItem extends Item {

    public ElevatorCallLinkerItem(Properties properties) {
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
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()));
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("Linker reset.").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        BlockState clickedState = level.getBlockState(clickedPos);

        // 1. Клик по контроллеру — привязка/переключение выбранного этажа
        if (clickedState.is(MODBLOCKS.ELEVATOR_CONTROLLER_BLOCK.get())) {
            if (!level.isClientSide()) {
                if (!(level.getBlockEntity(clickedPos) instanceof ElevatorControllerBlockEntity controllerBE)) {
                    return InteractionResult.FAIL;
                }

                int floorCount = controllerBE.getFloorCount();
                if (floorCount == 0) {
                    player.displayClientMessage(Component.literal("This controller has no floor points yet.").withStyle(ChatFormatting.RED), true);
                    return InteractionResult.FAIL;
                }

                boolean sameController = tag.hasUUID("ShaftId") && tag.getUUID("ShaftId").equals(controllerBE.getShaftId());
                int selected = sameController ? (tag.getInt("FloorIndex") + 1) % floorCount : 0;

                tag.putUUID("ShaftId", controllerBE.getShaftId());
                tag.putInt("FloorIndex", selected);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                int floorY = controllerBE.getFloors().get(selected).getY();
                player.displayClientMessage(Component.literal("Selected floor #" + (selected + 1) + "/" + floorCount + " (Y=" + floorY + ")").withStyle(ChatFormatting.AQUA), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        // 2. Клик по кнопке вызывателя — применяем привязку
        if (level.getBlockEntity(clickedPos) instanceof ElevatorCallerBlockEntity callerBE) {
            if (!tag.hasUUID("ShaftId")) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.literal("Bind a controller and select a floor first (right-click the controller).").withStyle(ChatFormatting.RED), true);
                }
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide()) {
                UUID shaftId = tag.getUUID("ShaftId");
                int floorIndex = tag.getInt("FloorIndex");
                callerBE.link(shaftId, floorIndex);
                player.displayClientMessage(Component.literal("Call button linked to floor #" + (floorIndex + 1)).withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }
}
