package com.scir4y.zeppelinmurdermod.content.scone;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

public class SconeBlock extends Block {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    // Хитбокс по реальной геометрии модели (плита + рычаг + чаша).
    // Наклонная тонкая свеча (element 3, angle=-45 x) в хитбокс не включена - декоративная деталь.
    private static final Map<Direction, VoxelShape> SHAPES = Util.make(new EnumMap<>(Direction.class), map -> {
        map.put(Direction.SOUTH, Shapes.or(
                Block.box(5, 3, 15, 11, 13, 16),
                Block.box(7, 8, 14, 9, 10, 15),
                Block.box(6, 5.5, 10, 10, 12.5, 14)
        ));
        map.put(Direction.WEST, Shapes.or(
                Block.box(0, 3, 5, 1, 13, 11),
                Block.box(1, 8, 7, 2, 10, 9),
                Block.box(2, 5.5, 6, 6, 12.5, 10)
        ));
        map.put(Direction.NORTH, Shapes.or(
                Block.box(5, 3, 0, 11, 13, 1),
                Block.box(7, 8, 1, 9, 10, 2),
                Block.box(6, 5.5, 2, 10, 12.5, 6)
        ));
        map.put(Direction.EAST, Shapes.or(
                Block.box(15, 3, 5, 16, 13, 11),
                Block.box(14, 8, 7, 15, 10, 9),
                Block.box(10, 5.5, 6, 14, 12.5, 10)
        ));
    });


    public SconeBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
