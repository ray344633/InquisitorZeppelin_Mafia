package com.scir4y.zeppelinmurdermod.client.hud;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = ZeppelinMurderMod.MODID, value = Dist.CLIENT)
public class TextOverlay {

    private static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(ZeppelinMurderMod.MODID, "text_hud");

    public static float alpha = 0.0f;
    public static float slide = 0.0f;
    private static final float FADE_SPEED_TICKS = 10.0f;
    private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(ZeppelinMurderMod.MODID, "textures/hud/icon.png");

    private TextOverlay() {}

    @SubscribeEvent
    public static void onRegisterLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(LAYER_ID, (gfx, delta) -> render(gfx, delta));
    }

    public static void render(GuiGraphics gfx, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) return;
        if (mc.options.hideGui) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        drawOverlay(gfx, delta, sw, sh);
    }

    public static void drawOverlay(GuiGraphics gfx, DeltaTracker delta, int sw, int sh) {
        if (Minecraft.getInstance().options.hideGui) {
            alpha = 0.0f;
            return;
        }

        float deltaTicks = delta.getRealtimeDeltaTicks();

        if (alpha < 1.0f) {
            alpha += deltaTicks / FADE_SPEED_TICKS;
            alpha = Mth.clamp(alpha, 0.0f, 1.0f);
        }

        if (slide < 1.0f) {
            slide = Mth.clamp(slide + deltaTicks / FADE_SPEED_TICKS, 0.0f, 1.0f);
        }


        float t = Mth.sin(slide * ((float)Math.PI / 2.0f));
        int startOffset = -8; // начинается на 8 пикселей выше
        int y = 20 + Math.round(startOffset * (1.0f - t));


        int alphaInt = (int) (alpha * 255.0f);
        int colorWithAlpha = (alphaInt << 24) | 0x00FFFFFF;

        Font font = Minecraft.getInstance().font;

        gfx.drawString(
                font,
                "TEST Привет, мир!",
                25,
                20,
                colorWithAlpha,
                true
        );

        gfx.blit(
                ICON,
                10,
                y - 4,
                0,
                0,
                16,
                16,
                16,
                16
        );
    }
}