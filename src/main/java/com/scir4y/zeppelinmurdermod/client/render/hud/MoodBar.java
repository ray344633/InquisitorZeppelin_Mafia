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

    public static float progress = 0.0f;
    private static final float FADE_SPEED_TICKS = 40.0f;

    private static final ResourceLocation EMPTY_MOOD_BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(ZeppelinMurderMod.MODID, "textures/gui/moodbar.png");
    private static final ResourceLocation FIELD_MOOD_BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(ZeppelinMurderMod.MODID, "textures/gui/moodbar2.png");

    public static void render(GuiGraphics gfx, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            progress = 0;
            return;
        }

        float deltaTicks = delta.getRealtimeDeltaTicks();

        if (progress < 1.0f) {
            progress += deltaTicks / FADE_SPEED_TICKS;
            progress = Mth.clamp(progress, 0.0f, 1.0f);
        }

        int screenWidth = gfx.guiWidth();
        int screenHeight = gfx.guiHeight();

        int x = 10;
        int y = 10;

        // Размеры текстуры шкалы
        int barWidth = 64;
        int barHeight = 16;

        float currentVal = minecraft.player.getData(PLAYER_ROUND_DATA).currentMoodAmount;
        float maxVal = Config.MAX_MOOD_AMOUNT.getAsInt();
        int filledWidth = (int) (barWidth * (currentVal / maxVal));

        gfx.pose().pushPose();
        gfx.pose().scale(3, 3, 3);

        gfx.blit(EMPTY_MOOD_BAR_TEXTURE, x, y, 0, 0, barWidth, barHeight, barWidth, barHeight);
        if (filledWidth > 0) {
            gfx.blit(FIELD_MOOD_BAR_TEXTURE, x, y, 0, 0, filledWidth, barHeight, barWidth, barHeight);
        }

        gfx.pose().popPose();
    }
}
