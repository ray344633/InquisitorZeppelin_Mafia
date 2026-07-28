package com.scir4y.zeppelinmurdermod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.scir4y.zeppelinmurdermod.entity.custom.MovingElevatorEntity;
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

        // Интерполяция координат сущности для плавного смещения света
        double lerpX = Mth.lerp(partialTick, entity.xo, entity.getX());
        double lerpY = Mth.lerp(partialTick, entity.yo, entity.getY());
        double lerpZ = Mth.lerp(partialTick, entity.zo, entity.getZ());

        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos relativeOffset = entry.getKey();
            BlockState state = entry.getValue();

            // Динамический расчет света для каждого блока
            BlockPos blockWorldPos = BlockPos.containing(
                    lerpX + relativeOffset.getX(),
                    lerpY + relativeOffset.getY(),
                    lerpZ + relativeOffset.getZ()
            );
            int dynamicPackedLight = LevelRenderer.getLightColor(entity.level(), blockWorldPos);

            // 1. Сохраняем текущее состояние матрицы
            poseStack.pushPose();

            // 2. Смещаем блок относительно центра сущности
            poseStack.translate(
                    relativeOffset.getX() - 0.5,
                    relativeOffset.getY(),
                    relativeOffset.getZ() - 0.5
            );

            // 3. Рендерим блок
            blockRenderer.renderSingleBlock(
                    state,
                    poseStack,
                    buffer,
                    dynamicPackedLight,
                    OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY,
                    null
            );

            // 4. Обязательно восстанавливаем состояние матрицы ровно 1 раз за итерацию
            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MovingElevatorEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}