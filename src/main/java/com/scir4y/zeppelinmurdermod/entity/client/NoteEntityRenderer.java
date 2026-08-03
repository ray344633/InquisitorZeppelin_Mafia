package com.scir4y.zeppelinmurdermod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.entity.custom.NoteEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class NoteEntityRenderer extends EntityRenderer<NoteEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ZeppelinMurderMod.MODID, "textures/entity/note.png");

    private static final float SIZE = 0.7f;

    public NoteEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(NoteEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        // save the current transformation matrix state
        poseStack.pushPose();

        // rotate and align the entity based on its placement and rotation in the world
        orient(poseStack, entity);

        // get the vertex buffer with cutout transparency and disabled face culling
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        // extract the current transformation matrix
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        // calculate half-size to center the rendered plane at (0, 0)
        float half = SIZE / 2.0f;

        // Determine offset direction along Y-axis to prevent z-fighting and embedding into blocks
        int sign = 1;
        if (entity.getFacing().getAxis() == Direction.Axis.Y) {
            sign = entity.getFacing().getAxisDirection().getStep();
        }

        // tiny offset along Z-axis to separate front and back faces
        float zOffset = 0.001f * sign;

        // render front face (facing positive Z)
        vertex(consumer, matrix, pose, -half, -half, zOffset, 0f, 1f, 0f, 0f, 1f, packedLight);
        vertex(consumer, matrix, pose, half, -half, zOffset, 1f, 1f, 0f, 0f, 1f, packedLight);
        vertex(consumer, matrix, pose, half, half, zOffset, 1f, 0f, 0f, 0f, 1f, packedLight);
        vertex(consumer, matrix, pose, -half, half, zOffset, 0f, 0f, 0f, 0f, 1f, packedLight);

        // render back face (facing negative Z)
        vertex(consumer, matrix, pose, -half, half, -zOffset, 0f, 0f, 0f, 0f, -1f, packedLight);
        vertex(consumer, matrix, pose, half, half, -zOffset, 1f, 0f, 0f, 0f, -1f, packedLight);
        vertex(consumer, matrix, pose, half, -half, -zOffset, 1f, 1f, 0f, 0f, -1f, packedLight);
        vertex(consumer, matrix, pose, -half, -half, -zOffset, 0f, 1f, 0f, 0f, -1f, packedLight);

        // restore the previous transformation matrix state after rendering
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, PoseStack.Pose pose,
                               float x, float y, float z, float u, float v,
                               float nx, float ny, float nz, int light) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private static void orient(PoseStack poseStack, NoteEntity entity) {
        // apply horizontal (yaw) rotation to face the correct direction
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        // apply (pitch) rotation to face the correct direction
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));

        // if facing is UP or DOWN apply rotation (roll)
        if (entity.getFacing().getAxis() == Direction.Axis.Y) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getRollYaw()));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(NoteEntity entity) {
        return TEXTURE;
    }
}