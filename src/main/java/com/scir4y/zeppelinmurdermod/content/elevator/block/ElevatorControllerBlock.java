package com.scir4y.zeppelinmurdermod.content.elevator.block;

import com.scir4y.zeppelinmurdermod.content.elevator.block.entity.ElevatorControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import static com.scir4y.zeppelinmurdermod.registry.ModItems.ELEVATOR_GLUE;
import com.scir4y.zeppelinmurdermod.registry.ModItems;

public class ElevatorControllerBlock extends Block implements EntityBlock {
    public ElevatorControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();

            if (!stack.isEmpty() && stack.getItem() == ELEVATOR_GLUE.get()) {
                if (tag.contains("Pos1") && tag.contains("Pos2")) {
                    if (level.getBlockEntity(pos) instanceof ElevatorControllerBlockEntity controllerBE) {
                        BlockPos pos1 = BlockPos.of(tag.getLong("Pos1"));
                        BlockPos pos2 = BlockPos.of(tag.getLong("Pos2"));

                        // Записываем данные в BlockEntity
                        controllerBE.setElevatorData(pos1, pos2);

                        player.displayClientMessage(Component.literal("Data was loaded, Point 1: " + pos1 + " Point 2: " + pos2), true);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElevatorControllerBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof ElevatorControllerBlockEntity controllerBE) {
                // Проверяем, задана ли форма платформы (Elevator Glue)
                if (!controllerBE.hasValidData()) {
                    player.displayClientMessage(Component.literal("§cElevator data is missing! Use Elevator Glue first."), true);
                    return InteractionResult.FAIL;
                }

                // Нужно минимум 2 зарегистрированных этажа, иначе ехать некуда
                if (controllerBE.getFloorCount() < 2) {
                    player.displayClientMessage(Component.literal("§cAdd at least 2 floor points using the Elevator Floor Point item!"), true);
                    return InteractionResult.FAIL;
                }

                // Ставим в очередь поездку на следующий этаж (диспетчер сам решит,
                // ехать сразу или подождать 6 сек, если лифт только что приехал)
                controllerBE.requestManualNext();
                player.displayClientMessage(Component.literal("§aElevator called."), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) return null;
        return (lvl, blockPos, blockState, blockEntity) -> {
            if (blockEntity instanceof ElevatorControllerBlockEntity controllerBE) {
                controllerBE.serverTick(lvl, blockPos, blockState);
            }
        };
    }
}
