package com.scir4y.zeppelinmurdermod.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Simplified data component for the "Note" item.
 * Unlike vanilla {@link net.minecraft.world.item.component.WrittenBookContent}
 * (title, author, generation, multiple pages, resolved flag) a note only ever needs
 * a single raw text string, so all the unused fields were dropped.
 */
public record NoteContent(String text) {

    public static final NoteContent EMPTY = new NoteContent("");

    // used for saving/loading (world save, /give commands, etc.)
    public static final Codec<NoteContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("text").forGetter(NoteContent::text)
    ).apply(instance, NoteContent::new));

    // used for sending the component between server and client
    public static final StreamCodec<RegistryFriendlyByteBuf, NoteContent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, NoteContent::text,
            NoteContent::new
    );

    public NoteContent {
        text = text == null ? "" : text;
    }
}
