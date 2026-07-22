package com.scir4y.zeppelinmurdermod.client.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.util.Mth;

public class TextRender implements LayeredDraw.Layer {

    public static float alpha = 0.0f;

    private static final float FADE_SPEED_TICKS = 10.0f;


    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (Minecraft.getInstance().options.hideGui) {
            alpha = 0.0f;
            return;
        }

        float deltaTicks = deltaTracker.getRealtimeDeltaTicks();

        if (alpha < 1.0f) {
            alpha += deltaTicks / FADE_SPEED_TICKS;
            alpha = Mth.clamp(alpha, 0.0f, 1.0f);
        }

        int alphaInt = (int) (alpha * 255.0f);

        int colorWithAlpha = (alphaInt << 24) | 0x00FFFFFF;

        Font font = Minecraft.getInstance().font;

        // 3. Рисуем текст в координатах X=100, Y=50
        guiGraphics.drawString(
                font,
                "TEST Привет, мир!",
                100,       // Координата X
                50,        // Координата Y
                colorWithAlpha,  // Цвет (Белый HEX)
                true       // Добавить тень тексту (выглядит аккуратнее)
        );
    }
}