package com.scir4y.zeppelinmurdermod.network.handler;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.scir4y.zeppelinmurdermod.system.ModAttachments.PLAYER_ROUND_DATA;
import com.scir4y.zeppelinmurdermod.network.payload.SyncMoodPayload;

public class ClientPayloadHandler {

    public static void handleMoodSync(SyncMoodPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.getData(PLAYER_ROUND_DATA).currentMoodAmount = payload.mood();
            }
        });
    }
}