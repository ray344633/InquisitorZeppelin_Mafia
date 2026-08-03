package com.scir4y.zeppelinmurdermod.network;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.network.handler.ClientPayloadHandler;
import com.scir4y.zeppelinmurdermod.network.payload.SaveNotePayload;
import com.scir4y.zeppelinmurdermod.network.payload.SyncMoodPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ZeppelinMurderMod.MODID)
public class ModNetwork {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ZeppelinMurderMod.MODID);

        // send from server to client
        registrar.playToClient(
                SyncMoodPayload.TYPE,
                SyncMoodPayload.STREAM_CODEC,
                ClientPayloadHandler::handleMoodSync
        );

        // send from client to server
        registrar.playToServer(
                SaveNotePayload.TYPE,
                SaveNotePayload.STREAM_CODEC,
                SaveNotePayload::handleData
        );
    }
}
