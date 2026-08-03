package com.scir4y.zeppelinmurdermod.content.elevator.block;

import com.scir4y.zeppelinmurdermod.content.elevator.block.entity.ElevatorCallerBlockEntity;
import com.scir4y.zeppelinmurdermod.content.elevator.block.entity.ElevatorControllerBlockEntity;
import com.scir4y.zeppelinmurdermod.content.elevator.util.ElevatorShaftRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Кнопка вызова лифта. Ставится на любом этаже, привязывается к контроллеру
 * и конкретному этажу через предмет Elevator Call Linker. Клик пустой рукой
 * ставит этот этаж в очередь вызовов контроллера.
 *
 * Специально НЕ переопределяет useItemOn — это позволяет предмету
 * ElevatorCallLinkerItem обрабатывать клики по этому блоку через свой
 * собственный useOn(), как это уже устроено для Elevator Glue.
 */
public class ElevatorCallerBlock extends Block implements EntityBlock {

    public ElevatorCallerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElevatorCallerBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof ElevatorCallerBlockEntity callerBE) {
                if (!callerBE.isLinked()) {
                    player.displayClientMessage(Component.literal("§cThis button isn't linked to any elevator. Use Elevator Call Linker."), true);
                    return InteractionResult.FAIL;
                }

                UUID shaftId = callerBE.getShaftId();
                BlockPos controllerPos = (shaftId != null && level instanceof ServerLevel serverLevel)
                        ? ElevatorShaftRegistry.get(serverLevel, shaftId)
                        : null;

                if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof ElevatorControllerBlockEntity controllerBE) {
                    boolean queued = controllerBE.requestFloor(callerBE.getFloorIndex());
                    if (queued) {
                        player.displayClientMessage(Component.literal("§aCalling the elevator..."), true);
                    } else {
                        player.displayClientMessage(Component.literal("The elevator is already there or on its way."), true);
                    }
                } else {
                    // Контроллер этой шахты сейчас нигде не зарегистрирован —
                    // либо лифт реально в пути (блок временно разобран), либо
                    // его чанк ещё не подгружен.
                    player.displayClientMessage(Component.literal("§cThe elevator is currently moving, try again in a moment."), true);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
