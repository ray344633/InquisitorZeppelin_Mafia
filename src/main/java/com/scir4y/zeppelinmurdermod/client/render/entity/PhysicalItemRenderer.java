package com.scir4y.zeppelinmurdermod.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Custom ground renderer for ItemEntity.
 *
 * Fixes vs. the original code:
 *  1. Every pushPose() is now matched by exactly one popPose() — the original
 *     crashed with "Pose stack not empty" because it called popPose() without
 *     ever calling pushPose() first (and dropped the vanilla super.render()
 *     call that normally owns that push).
 *  2. The early return (empty stack) happens BEFORE pushPose(), so we never
 *     leave a dangling push on that path either.
 *  3. Transformations are wrapped in try/finally so an exception thrown while
 *     rendering can't skip popPose() and corrupt the pose stack for the rest
 *     of the frame.
 */
public class PhysicalItemRenderer extends ItemEntityRenderer {

    private final ItemRenderer customItemRenderer;

    public PhysicalItemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.customItemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ItemEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        ItemStack itemStack = entity.getItem();
        if (itemStack.isEmpty()) {
            // Nothing to render — return before touching the pose stack at all.
            return;
        }

        poseStack.pushPose();
        try {
            renderItemInWorld(entity, itemStack, partialTicks, poseStack, buffer, packedLight);
        } finally {
            // Guaranteed to run even if rendering throws, keeping the stack balanced.
            poseStack.popPose();
        }

        if (this.shouldShowName(entity)) {
            this.renderNameTag(entity, entity.getDisplayName(), poseStack, buffer, packedLight, partialTicks);
        }
    }

    private void renderItemInWorld(ItemEntity entity, ItemStack itemStack, float partialTicks,
                                   PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        // Smooth yaw interpolation between the last tick and this frame,
        // so the item's spin doesn't look choppy at low tick rates.
        float currentYaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(currentYaw));

        // Small per-entity rotational jitter so stacked/dropped items don't
        // all look perfectly aligned.
        float randomTilt = (float) (entity.getId() % 360) * 0.1F;
        poseStack.mulPose(Axis.ZP.rotationDegrees(randomTilt));

        BakedModel bakedModel = this.customItemRenderer.getModel(
                itemStack, entity.level(), null, entity.getId());

        this.customItemRenderer.render(
                itemStack,
                ItemDisplayContext.GROUND,
                false,
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                bakedModel
        );
    }

    @Override
    protected float getShadowRadius(ItemEntity entity) {
        return 0;
    }
}