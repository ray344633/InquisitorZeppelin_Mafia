package com.scir4y.zeppelinmurdermod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.scir4y.zeppelinmurdermod.entity.custom.MovingElevatorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class MovingElevatorRenderer extends EntityRenderer<MovingElevatorEntity> {
    public MovingElevatorRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MovingElevatorEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        for (Map.Entry<BlockPos, BlockState> entry : entity.getBlocks().entrySet()) {
            poseStack.pushPose();
            BlockPos pos = entry.getKey();
            poseStack.translate(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5);
            
            // In 1.21.1, renderSingleBlock might need ModelData. We can pass net.neoforged.neoforge.client.model.data.ModelData.EMPTY
            blockRenderer.renderSingleBlock(entry.getValue(), poseStack, buffer, packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, net.neoforged.neoforge.client.model.data.ModelData.EMPTY, null);
            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MovingElevatorEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
