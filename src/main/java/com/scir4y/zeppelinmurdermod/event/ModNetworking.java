package com.scir4y.zeppelinmurdermod.event;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.network.SaveNotePayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ZeppelinMurderMod.MODID)
public class ModNetworking {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(ZeppelinMurderMod.MODID);

        registrar.playToServer(
                SaveNotePayload.TYPE,
                SaveNotePayload.STREAM_CODEC,
                SaveNotePayload::handleData
        );
    }
}
