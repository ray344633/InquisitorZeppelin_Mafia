package com.scir4y.zeppelinmurdermod.block.entity.custom;

import com.scir4y.zeppelinmurdermod.block.entity.MODBLOCKENTITIES;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ElevatorControllerBlockEntity extends BlockEntity {
    public ElevatorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(MODBLOCKENTITIES.ELEVATOR_CONTROLLER_BE.get(), pos, state);
    }
}
