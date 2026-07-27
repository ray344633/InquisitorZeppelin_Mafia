package com.scir4y.zeppelinmurdermod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.util.List;

public class EffectManager {

    private static final ResourceLocation SHADER_LOC =
            ResourceLocation.fromNamespaceAndPath(
                    "zeppelinmurder",
                    "shaders/post/heartbeat.json"
            );

    private static float currentFear = 0.0f;
    private static float heartbeatPhase = 0.0f;
    private static boolean active = false;

    private static Field passesField;

    public static void enableCustomEffect() {

        Minecraft mc = Minecraft.getInstance();

        if (active)
            return;

        mc.gameRenderer.loadEffect(SHADER_LOC);

        currentFear = 0.0f;
        heartbeatPhase = 0.0f;
        active = true;
    }

    public static void disableCustomEffect() {

        Minecraft mc = Minecraft.getInstance();

        mc.gameRenderer.shutdownEffect();

        currentFear = 0.0f;
        heartbeatPhase = 0.0f;
        active = false;
    }

    public static boolean isEffectActive() {
        return active && Minecraft.getInstance().gameRenderer.currentEffect() != null;
    }

    public static void toggleCustomEffect() {

        if (active)
            disableCustomEffect();
        else
            enableCustomEffect();
    }

    public static void tickEffect() {

        if (!active)
            return;

        // Быстрое нарастание страха (теперь занимает около 5 секунд вместо 25)
        if (currentFear < 1.0f) {
            currentFear += 0.01f; 
            if (currentFear > 1.0f) {
                currentFear = 1.0f;
            }
        }
        
        // Учащение сердцебиения в зависимости от уровня страха
        float minSpeed = 0.02f; // Медленные стуки в начале
        float maxSpeed = 0.15f; // Быстрые стуки при максимуме
        float currentSpeed = minSpeed + (maxSpeed - minSpeed) * currentFear;

        heartbeatPhase += currentSpeed;
        if (heartbeatPhase >= 1.0f) {
            heartbeatPhase -= 1.0f;
        }

        updateShaderUniforms();
    }

    @SuppressWarnings("unchecked")
    private static void updateShaderUniforms() {

        PostChain chain = Minecraft.getInstance().gameRenderer.currentEffect();

        if (chain == null)
            return;

        try {

            if (passesField == null) {

                passesField = PostChain.class.getDeclaredField("passes");
                passesField.setAccessible(true);

            }

            List<PostPass> passes = (List<PostPass>) passesField.get(chain);

            if (passes == null || passes.isEmpty())
                return;

            PostPass pass = passes.get(0);

            if (pass.getEffect().getUniform("FearProgress") != null)
                pass.getEffect().getUniform("FearProgress").set(currentFear);

            if (pass.getEffect().getUniform("HeartbeatPhase") != null)
                pass.getEffect().getUniform("HeartbeatPhase").set(heartbeatPhase);

        } catch (Exception ignored) {
        }

    }

}