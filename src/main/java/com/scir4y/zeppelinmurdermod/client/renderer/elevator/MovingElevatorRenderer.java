package com.scir4y.zeppelinmurdermod.client.renderer.elevator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.scir4y.zeppelinmurdermod.content.elevator.entity.MovingElevatorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.Map;

public class MovingElevatorRenderer extends EntityRenderer<MovingElevatorEntity> {
    public MovingElevatorRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MovingElevatorEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Map<BlockPos, BlockState> blocks = entity.getBlocks();
        if (blocks.isEmpty()) {
            super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
            return;
        }

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        // Interpolation entity's coordinates for smooth light displacement
        double lerpX = Mth.lerp(partialTick, entity.xo, entity.getX());
        double lerpY = Mth.lerp(partialTick, entity.yo, entity.getY());
        double lerpZ = Mth.lerp(partialTick, entity.zo, entity.getZ());

        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos relativeOffset = entry.getKey();
            BlockState state = entry.getValue();

            // Calculation of dynamic lighting for each block
            BlockPos blockWorldPos = BlockPos.containing(
                    lerpX + relativeOffset.getX(),
                    lerpY + relativeOffset.getY(),
                    lerpZ + relativeOffset.getZ()
            );
            int dynamicPackedLight = LevelRenderer.getLightColor(entity.level(), blockWorldPos);

            // 1. Save current state of matric
            poseStack.pushPose();

            // 2. Offset of the block relative to the center of the entity
            poseStack.translate(
                    relativeOffset.getX() - 0.5,
                    relativeOffset.getY(),
                    relativeOffset.getZ() - 0.5
            );

            // 3. Render block
            blockRenderer.renderSingleBlock(
                    state,
                    poseStack,
                    buffer,
                    dynamicPackedLight,
                    OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY,
                    null
            );

            // 4. Necessary restore matric's state
            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MovingElevatorEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}