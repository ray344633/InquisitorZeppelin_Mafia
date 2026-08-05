package com.scir4y.zeppelinmurdermod.network.payload;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.client.gui.MoodBar;
import com.scir4y.zeppelinmurdermod.network.ModNetwork;
import com.scir4y.zeppelinmurdermod.network.handler.ClientPayloadHandler;
import com.scir4y.zeppelinmurdermod.system.game.MoodTickHandler;
import com.scir4y.zeppelinmurdermod.system.game.PlayerRoundData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Network payload sent from the server to the client to synchronize the player's
 * current mood value.
 *
 * Mood is stored server-side in {@link PlayerRoundData}
 * and decreases over time via {@link MoodTickHandler}.
 * Whenever the mood changes (each tick while the game is running, or immediately after
 * a command like /mood set), the server sends this packet to the affected client
 * so the client-side {@link MoodBar} can display the up-to-date value without
 * needing a full attachment sync.
 *
 * Registration: {@link ModNetwork} registers this payload as {@code playToClient}.
 * The client handler is
 * {@link ClientPayloadHandler#handleMoodSync}.
 */
public record SyncMoodPayload(float mood) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncMoodPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ZeppelinMurderMod.MODID, "sync_mood"));

    public static final StreamCodec<FriendlyByteBuf, SyncMoodPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SyncMoodPayload::mood,
            SyncMoodPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}