package com.scir4y.zeppelinmurdermod.network;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ZeppelinMurderMod.MODID)
public class ModNetwork {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ZeppelinMurderMod.MODID);

        // Отправка с сервера на клиент
        registrar.playToClient(
                SyncMoodPayload.TYPE,
                SyncMoodPayload.STREAM_CODEC,
                ClientPayloadHandler::handleMoodSync
        );
    }
}