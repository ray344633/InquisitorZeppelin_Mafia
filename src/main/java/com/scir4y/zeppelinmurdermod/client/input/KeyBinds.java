package com.scir4y.zeppelinmurdermod.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ZeppelinMurderMod.MODID, value = Dist.CLIENT)
public final class KeyBinds {
    public static final String CATEGORY = "key.categories.zeppelinmurder";

    public static KeyMapping RELOAD_CONFIG;
    public static KeyMapping SHADER_TOGGLE;

    private KeyBinds() {}

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent e) {
        RELOAD_CONFIG = new KeyMapping(
                "key.zeppelinmurder.reload",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                CATEGORY
        );
        SHADER_TOGGLE = new KeyMapping(
                "key.zeppelinmurder.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                CATEGORY
        );
        e.register(RELOAD_CONFIG);
        e.register(SHADER_TOGGLE);
    }
}