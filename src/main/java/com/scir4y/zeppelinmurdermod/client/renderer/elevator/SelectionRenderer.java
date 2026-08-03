package com.scir4y.zeppelinmurdermod.client.renderer.elevator;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import com.scir4y.zeppelinmurdermod.content.elevator.item.ElevatorGlueItem;

@EventBusSubscriber(modid = "zeppelinmurdermod", value = Dist.CLIENT)
public class SelectionRenderer {

    // Свой RenderType: без culling (двусторонний рендер), с блендингом и без записи в depth-буфер,
    // чтобы грани не пропадали/не мерцали в зависимости от порядка вершин и угла обзора.
    private static final RenderType FILLED_BOX_NO_CULL = RenderType.create(
            "zeppelinmurdermod:filled_box_no_cull",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(false)
    );

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        // Check if the item is in the player's hands
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ElevatorGlueItem)) {
            stack = player.getOffhandItem();
            if (!(stack.getItem() instanceof ElevatorGlueItem)) return; // Exit if not in hands
        }

        // Read data from the item
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (!tag.contains("Pos1")) return; // Point 1 is not set yet - exit

        BlockPos pos1 = BlockPos.of(tag.getLong("Pos1"));
        BlockPos pos2;

        // If Point 2 is locked/set
        if (tag.contains("Pos2")) {
            pos2 = BlockPos.of(tag.getLong("Pos2"));
        } else {
            // Otherwise, stretch/follow the cursor
            HitResult hit = mc.hitResult;
            if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                pos2 = ((BlockHitResult) hit).getBlockPos();
            } else {
                // If the cursor is looking at the sky, outline only the 1st block to prevent crashes
                pos2 = pos1;
            }
        }

        // Create the bounding box. inflate(0.01) makes it slightly larger than the block so it sticks out and doesn't z-fight
        AABB box = AABB.encapsulatingFullBlocks(pos1, pos2).inflate(0.01D);

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();

        poseStack.pushPose();
        // Shift rendering from camera to world coordinates
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // 1. Render standard Minecraft outline (Green line)
        VertexConsumer lineBuilder = bufferSource.getBuffer(RenderType.lines());
        // RGBA (Red, Green, Blue, Alpha)
        LevelRenderer.renderLineBox(poseStack, lineBuilder, box, 0.0f, 1.0f, 0.0f, 1.0f);

        // 2. Render translucent green fill inside (свой RenderType без culling)
        VertexConsumer fillBuilder = bufferSource.getBuffer(FILLED_BOX_NO_CULL);
        renderFilledBox(poseStack, fillBuilder, box, 0.0f, 1.0f, 0.0f, 0.2f);

        // Force the game to draw this immediately
        bufferSource.endBatch(FILLED_BOX_NO_CULL);
        bufferSource.endBatch(RenderType.lines());

        poseStack.popPose();
    }

    // Manual method for creating cube faces.
    // Порядок вершин приведён к единому CCW (против часовой стрелки), если смотреть на грань снаружи куба.
    private static void renderFilledBox(PoseStack poseStack, VertexConsumer builder, AABB box, float r, float g, float b, float a) {
        Matrix4f matrix = poseStack.last().pose();
        float minX = (float) box.minX; float minY = (float) box.minY; float minZ = (float) box.minZ;
        float maxX = (float) box.maxX; float maxY = (float) box.maxY; float maxZ = (float) box.maxZ;

        // Bottom (Y = minY), смотрим снизу вверх (+Y — наружу нет, наружу тут -Y)
        builder.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        builder.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);

        // Top (Y = maxY)
        builder.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        builder.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);

        // North (Z = minZ)
        builder.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        builder.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        builder.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);

        // South (Z = maxZ)
        builder.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        builder.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);

        // West (X = minX)
        builder.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        builder.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        builder.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        builder.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);

        // East (X = maxX)
        builder.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
    }
}