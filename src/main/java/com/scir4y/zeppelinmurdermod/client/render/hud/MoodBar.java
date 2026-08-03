package com.scir4y.zeppelinmurdermod.client.render.hud;

import com.scir4y.zeppelinmurdermod.Config;
import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import static com.scir4y.zeppelinmurdermod.data.ModAttachments.PLAYER_ROUND_DATA;

public class MoodBar {

    private static final ResourceLocation EMPTY_MOOD_BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(ZeppelinMurderMod.MODID, "textures/gui/moodbar.png");
    private static final ResourceLocation FIELD_MOOD_BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(ZeppelinMurderMod.MODID, "textures/gui/moodbar2.png");

    // Для плавной анимации изменения значения
    private static float displayedMood = 0.0f;

    public static void render(GuiGraphics gfx, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        int barWidth = 64;
        int barHeight = 16;
        int x = 10;
        int y = 10;

        float targetVal = minecraft.player.getData(PLAYER_ROUND_DATA).currentMoodAmount;
        float maxVal = Config.MAX_MOOD_AMOUNT.getAsInt();

        float deltaTicks = delta.getRealtimeDeltaTicks();
        float smoothing = 1.0f - (float) Math.pow(0.5, deltaTicks / 4.0f);
        displayedMood = Mth.lerp(smoothing, displayedMood, targetVal);

        int filledWidth = (int) (barWidth * (Mth.clamp(displayedMood, 0, maxVal) / maxVal));

        gfx.pose().pushPose();
        gfx.pose().scale(3, 3, 3);

        gfx.blit(EMPTY_MOOD_BAR_TEXTURE, x, y, 0, 0, barWidth, barHeight, barWidth, barHeight);
        if (filledWidth > 0) {
            gfx.blit(FIELD_MOOD_BAR_TEXTURE, x, y, 0, 0, filledWidth, barHeight, barWidth, barHeight);
        }

        gfx.pose().popPose();
    }
}