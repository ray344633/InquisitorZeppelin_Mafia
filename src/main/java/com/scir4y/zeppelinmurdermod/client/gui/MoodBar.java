package com.scir4y.zeppelinmurdermod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.config.Config;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import static com.scir4y.zeppelinmurdermod.system.ModAttachments.PLAYER_ROUND_DATA;

public class MoodBar {

    private static final ResourceLocation EMPTY_MOOD_BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(ZeppelinMurderMod.MODID, "textures/gui/empty_moodbar.png");
    private static final ResourceLocation FIELD_MOOD_BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(ZeppelinMurderMod.MODID, "textures/gui/field_mood.png");

    private static float displayedMood = 0.0f;

    public static void render(GuiGraphics gfx, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui) {
            return;
        }

        // Fixed margin distance from window top-left border
        final int marginX = 10;
        final int marginY = 10;

        // Texture size & independent render scale
        final int barWidth = 64;
        final int barHeight = 16;
        final float hudScale = 2.0f;

        var roundData = player.getData(PLAYER_ROUND_DATA);

        gfx.pose().pushPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Render the mood bar texture
        float targetVal = roundData.currentMoodAmount;
        float maxVal = Math.max(1.0f, Config.MAX_MOOD_AMOUNT.getAsInt());

        // Smooth tick-based interpolation toward the target mood value
        float deltaTicks = delta.getRealtimeDeltaTicks();
        float smoothing = 1.0f - (float) Math.pow(0.5, deltaTicks / 4.0f);
        displayedMood = Mth.lerp(smoothing, displayedMood, targetVal);

        // Min mood is 0.0, max mood is maxVal
        float clampedMood = Mth.clamp(displayedMood, 0.0f, maxVal);
        int filledWidth = (int) (barWidth * (clampedMood / maxVal));

        // Scale texture locally at (marginX, marginY) to keep screen margin consistent
        gfx.pose().pushPose();
        gfx.pose().translate(marginX, marginY, 0);
        gfx.pose().scale(hudScale, hudScale, 1.0f);

        // Draw empty background
        gfx.blit(EMPTY_MOOD_BAR_TEXTURE, 0, 0, 0, 0, barWidth, barHeight, barWidth, barHeight);

        // Draw filled bar on top of the background
        if (filledWidth > 0) {
            gfx.blit(FIELD_MOOD_BAR_TEXTURE, 0, 0, 0, 0, filledWidth, barHeight, barWidth, barHeight);
        }

        gfx.pose().popPose(); // End local scaling matrix

        RenderSystem.disableBlend();
        gfx.pose().popPose();
    }
}