package com.scir4y.zeppelinmurdermod.network;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.scir4y.zeppelinmurdermod.data.ModAttachments.PLAYER_ROUND_DATA;

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