package com.scir4y.zeppelinmurdermod.content.elevator.block;

import com.scir4y.zeppelinmurdermod.content.elevator.block.entity.ElevatorCallerBlockEntity;
import com.scir4y.zeppelinmurdermod.content.elevator.block.entity.ElevatorControllerBlockEntity;
import com.scir4y.zeppelinmurdermod.content.elevator.item.ElevatorCallLinkerItem;
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
 * Elevator call button. Places on any floor; links to a
 * controller and specific floor via {@link ElevatorCallLinkerItem}.
 * Right-clicking with an empty hand enqueues this floor in
 * the controller's call queue
 *
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
        // if it is server side
        if (!level.isClientSide()) {
            // if block is ElevatorCallerBlockEntity
            if (level.getBlockEntity(pos) instanceof ElevatorCallerBlockEntity callerBE) {
                // if CallerBlock is not linked
                if (!callerBE.isLinked()) {
                    player.displayClientMessage(Component.literal("§cThis button isn't linked to any elevator. Use Elevator Call Linker."), true);
                    return InteractionResult.FAIL;
                }

                // if CallerBlock is linked
                UUID shaftId = callerBE.getShaftId();
                // TODO this line
                BlockPos controllerPos = (shaftId != null && level instanceof ServerLevel serverLevel)
                        ? ElevatorShaftRegistry.get(serverLevel, shaftId)
                        : null;

                if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof ElevatorControllerBlockEntity controllerBE) {
                    // TODO this line
                    boolean queued = controllerBE.requestFloor(callerBE.getFloorIndex());
                    if (queued) {
                        // TODO Make custom displaying
                        player.displayClientMessage(Component.literal("§aCalling the elevator..."), true);
                    } else {
                        // TODO Make custom displaying
                        player.displayClientMessage(Component.literal("The elevator is already there or on its way."), true);
                    }
                } else {
                    // Controller is not registered
                    // or elevator is on the way(temporarily is entity)
                    // or its' chank has not been loaded yet
                    // TODO Make custom displaying
                    player.displayClientMessage(Component.literal("§cThe elevator is currently moving, try again in a moment."), true);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
