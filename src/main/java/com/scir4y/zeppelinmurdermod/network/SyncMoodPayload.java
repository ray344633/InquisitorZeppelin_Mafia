package com.scir4y.zeppelinmurdermod.network;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncMoodPayload(int mood) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncMoodPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ZeppelinMurderMod.MODID, "sync_mood"));

    public static final StreamCodec<FriendlyByteBuf, SyncMoodPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncMoodPayload::mood,
            SyncMoodPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}