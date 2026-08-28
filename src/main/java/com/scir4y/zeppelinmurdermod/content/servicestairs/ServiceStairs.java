package com.scir4y.zeppelinmurdermod.content.servicestairs;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

public class ServiceStairs extends Block {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");

    private static final Map<Direction, VoxelShape> SHAPES = Util.make(new EnumMap<>(Direction.class), map -> {
        map.put(Direction.SOUTH, Shapes.or(
                Block.box(14, 0, 6, 16, 6, 8),
                Block.box(2, 8, 8, 14, 10, 10),
                Block.box(7, 10, 8, 9, 14, 10),
                Block.box(14, 0, 0, 16, 2, 6),
                Block.box(0, 0, 0, 2, 2, 6),
                Block.box(2, 0, 0, 14, 2, 2),
                Block.box(7, 2, 0, 9, 6, 2),
                Block.box(0, 0, 6, 2, 6, 8),
                Block.box(14, 8, 14, 16, 14, 16),
                Block.box(14, 8, 8, 16, 10, 14),
                Block.box(0, 8, 8, 2, 10, 14),
                Block.box(0, 8, 14, 2, 14, 16),
                Block.box(0, 14, 8, 16, 16, 16),
                Block.box(0, 6, 0, 16, 8, 8)
        ));
        map.put(Direction.WEST, Shapes.or(
                Block.box(8, 0, 14, 10, 6, 16),
                Block.box(6, 8, 2, 8, 10, 14),
                Block.box(6, 10, 7, 8, 14, 9),
                Block.box(10, 0, 14, 16, 2, 16),
                Block.box(10, 0, 0, 16, 2, 2),
                Block.box(14, 0, 2, 16, 2, 14),
                Block.box(14, 2, 7, 16, 6, 9),
                Block.box(8, 0, 0, 10, 6, 2),
                Block.box(0, 8, 14, 2, 14, 16),
                Block.box(2, 8, 14, 8, 10, 16),
                Block.box(2, 8, 0, 8, 10, 2),
                Block.box(0, 8, 0, 2, 14, 2),
                Block.box(0, 14, 0, 8, 16, 16),
                Block.box(8, 6, 0, 16, 8, 16)
        ));
        map.put(Direction.NORTH, Shapes.or(
                Block.box(0, 0, 8, 2, 6, 10),
                Block.box(2, 8, 6, 14, 10, 8),
                Block.box(7, 10, 6, 9, 14, 8),
                Block.box(0, 0, 10, 2, 2, 16),
                Block.box(14, 0, 10, 16, 2, 16),
                Block.box(2, 0, 14, 14, 2, 16),
                Block.box(7, 2, 14, 9, 6, 16),
                Block.box(14, 0, 8, 16, 6, 10),
                Block.box(0, 8, 0, 2, 14, 2),
                Block.box(0, 8, 2, 2, 10, 8),
                Block.box(14, 8, 2, 16, 10, 8),
                Block.box(14, 8, 0, 16, 14, 2),
                Block.box(0, 14, 0, 16, 16, 8),
                Block.box(0, 6, 8, 16, 8, 16)
        ));
        map.put(Direction.EAST, Shapes.or(
                Block.box(6, 0, 0, 8, 6, 2),
                Block.box(8, 8, 2, 10, 10, 14),
                Block.box(8, 10, 7, 10, 14, 9),
                Block.box(0, 0, 0, 6, 2, 2),
                Block.box(0, 0, 14, 6, 2, 16),
                Block.box(0, 0, 2, 2, 2, 14),
                Block.box(0, 2, 7, 2, 6, 9),
                Block.box(6, 0, 14, 8, 6, 16),
                Block.box(14, 8, 0, 16, 14, 2),
                Block.box(8, 8, 0, 14, 10, 2),
                Block.box(8, 8, 14, 14, 10, 16),
                Block.box(14, 8, 14, 16, 14, 16),
                Block.box(8, 14, 0, 16, 16, 16),
                Block.box(0, 6, 0, 8, 8, 16)
        ));
    });

    public ServiceStairs(BlockBehaviour.Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false)
                .setValue(CONNECTED, false));
    }

    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel()
                .getFluidState(context.getClickedPos());
        Direction facing = context.getHorizontalDirection();

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER)
                .setValue(CONNECTED, isConnected(context.getLevel(), context.getClickedPos(), facing));
    }

    private static boolean isConnected(BlockGetter level, BlockPos pos, Direction facing) {
        BlockPos checkPos = pos.above().relative(facing);
        BlockState checkState = level.getBlockState(checkPos);
        return checkState.getBlock() instanceof ServiceStairs
                && checkState.getValue(FACING) == facing;
    }

    private static void updateDependentStairs(Level level, BlockPos changedPos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos candidatePos = changedPos.below().relative(dir.getOpposite());
            BlockState candidateState = level.getBlockState(candidatePos);

            if (candidateState.getBlock() instanceof ServiceStairs
                    && candidateState.getValue(FACING) == dir) {

                boolean connected = isConnected(level, candidatePos, dir);
                if (candidateState.getValue(CONNECTED) != connected) {
                    level.setBlock(candidatePos, candidateState.setValue(CONNECTED, connected), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(state.getBlock())) {
            updateDependentStairs(level, pos);
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            updateDependentStairs(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected BlockState updateShape(BlockState state, Direction facing,
                                     BlockState facingState, LevelAccessor level,
                                     BlockPos currentPos, BlockPos facingPos) {

        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(
                    currentPos,
                    Fluids.WATER,
                    Fluids.WATER.getTickDelay(level)
            );
        }

        return super.updateShape(
                state,
                facing,
                facingState,
                level,
                currentPos,
                facingPos
        );
    }

    protected BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, CONNECTED);
    }

    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
