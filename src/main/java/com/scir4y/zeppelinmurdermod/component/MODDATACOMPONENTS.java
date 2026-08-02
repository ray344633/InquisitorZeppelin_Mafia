package com.scir4y.zeppelinmurdermod.component;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MODDATACOMPONENTS {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ZeppelinMurderMod.MODID);

    // replaces vanilla WRITTEN_BOOK_CONTENT for the "written_note" item
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<NoteContent>> NOTE_CONTENT =
            DATA_COMPONENT_TYPES.register("note_content", () -> DataComponentType.<NoteContent>builder()
                    .persistent(NoteContent.CODEC)
                    .networkSynchronized(NoteContent.STREAM_CODEC)
                    .build());

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
