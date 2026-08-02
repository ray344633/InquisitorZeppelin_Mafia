package com.scir4y.zeppelinmurdermod.network;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.item.MODITEMS;
import com.scir4y.zeppelinmurdermod.item.custom.WritableNoteItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record SaveNotePayload(String text, boolean isFinalized) implements CustomPacketPayload {
    // unique pocket's identifier
    public static final Type<SaveNotePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ZeppelinMurderMod.MODID, "save_note"));

    // Codec for encoding/decoding pocket's data
    public static final StreamCodec<FriendlyByteBuf, SaveNotePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SaveNotePayload::text,
            ByteBufCodecs.BOOL, SaveNotePayload::isFinalized,
            SaveNotePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // server method
    public static void handleData(final SaveNotePayload payload, final IPayloadContext context) {
        // Safely execute code on the main server thread
        context.enqueueWork(() -> {
            Player player = context.player(); // get player
            ItemStack stack = player.getMainHandItem(); // get ItemStack in main hand

            // is in main hand exactly WritableNoteItem
            if (stack.getItem() instanceof WritableNoteItem) {
                List<String> pages = List.of(payload.text()); // get text
                // if note is FINISHED
                if (payload.isFinalized()) {
                    // convert into WrittenNote
                    // get pages
                    List<Filterable<Component>> componentPages = pages.stream()
                            .map(page -> Filterable.passThrough((Component) Component.literal(page)))
                            .toList();

                    WrittenBookContent writtenContent = new WrittenBookContent(
                            Filterable.passThrough(""), // empty title
                            "", // without author
                            0,
                            componentPages,
                            true
                    );

                    ItemStack finishedNote = new ItemStack(MODITEMS.WRITTEN_NOTE.get()); // WRITTEN NOTE ITEM
                    finishedNote.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenContent); // SET NECESSARY DATA

                    // set item in main hand
                    player.setItemInHand(InteractionHand.MAIN_HAND, finishedNote);
                } else {
                    // save changes if player clicks on SAVE BUTTON(NOT FINISH BUTTON)
                    stack.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(pages.stream().map(Filterable::passThrough).toList()));
                }
            }
        });
    }
}