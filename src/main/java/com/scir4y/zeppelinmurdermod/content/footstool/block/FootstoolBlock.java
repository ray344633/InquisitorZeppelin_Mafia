package com.scir4y.zeppelinmurdermod.content.footstool.block;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.scir4y.zeppelinmurdermod.content.footstool.entity.FootstoolEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.neoforged.neoforge.common.util.FakePlayer;

public class FootstoolBlock extends Block {

    public static final MapCodec<FootstoolBlock> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(DyeColor.CODEC.fieldOf("color").forGetter(FootstoolBlock::getColor), propertiesCodec())
                    .apply(builder, FootstoolBlock::new)
    );

    private final DyeColor color;

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(13.5, 0, 0.5, 15.5, 1, 2.5),   // leg (front-right)
            Block.box(0.5, 0, 0.5, 2.5, 1, 2.5),     // leg (front-left)
            Block.box(13.5, 0, 13.5, 15.5, 1, 15.5), // leg (back-right)
            Block.box(0.5, 0, 13.5, 2.5, 1, 15.5),   // leg (back-left)
            Block.box(0, 2, 0, 16, 7, 16),           // main body
            Block.box(0.5, 1, 0.5, 15.5, 2, 15.5)    // cushion trim
    );

    public FootstoolBlock(DyeColor color, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = color;
    }

    @Override
    public MapCodec<FootstoolBlock> codec() {
        return CODEC;
    }

    public DyeColor getColor() {
        return this.color;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Collision now matches the actual model shape (legs + body +
        // cushion). This is safe now that dismounting steps the player off
        // to the side instead of dropping them back inside the block.
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                                BlockHitResult hitResult) {
        if (player.isShiftKeyDown() || player instanceof FakePlayer)
            return InteractionResult.PASS;

        // Only sit down when both hands are actually empty
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty())
            return InteractionResult.PASS;

        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        if (isSeatOccupied(level, pos))
            return InteractionResult.PASS;

        sitDown(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    public static boolean isSeatOccupied(Level level, BlockPos pos) {
        return !level.getEntitiesOfClass(FootstoolEntity.class, new AABB(pos)).isEmpty();
    }

    public static void sitDown(Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide)
            return;

        FootstoolEntity seat = new FootstoolEntity(level);
        seat.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        level.addFreshEntity(seat);
        entity.startRiding(seat, true);
    }

    public static List<FootstoolEntity> getSeatsAt(Level level, BlockPos pos) {
        return level.getEntitiesOfClass(FootstoolEntity.class, new AABB(pos));
    }
}
