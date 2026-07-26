package com.scir4y.zeppelinmurdermod.client.input;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.client.hud.TextOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = ZeppelinMurderMod.MODID, value = Dist.CLIENT)
public final class KeyBindsHandler {

    private KeyBindsHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post e) {
        if (KeyBinds.RELOAD_CONFIG != null && KeyBinds.RELOAD_CONFIG.consumeClick()) {
            TextOverlay.alpha = 0;
        }
    }
}