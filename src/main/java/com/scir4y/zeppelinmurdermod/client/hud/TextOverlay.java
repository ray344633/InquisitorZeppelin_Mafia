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
    private static final float FADE_SPEED_TICKS = 10.0f;

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

        int alphaInt = (int) (alpha * 255.0f);
        int colorWithAlpha = (alphaInt << 24) | 0x00FFFFFF;

        Font font = Minecraft.getInstance().font;

        gfx.drawString(
                font,
                "TEST Привет, мир!",
                20,
                20,
                colorWithAlpha,
                true
        );
    }
}