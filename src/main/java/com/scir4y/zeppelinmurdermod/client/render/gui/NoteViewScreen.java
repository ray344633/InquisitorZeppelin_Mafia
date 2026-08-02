package com.scir4y.zeppelinmurdermod.client.render.gui;

import com.scir4y.zeppelinmurdermod.component.MODDATACOMPONENTS;
import com.scir4y.zeppelinmurdermod.component.NoteContent;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// Client-side GUI screen for reading note contents
@OnlyIn(Dist.CLIENT)
public class NoteViewScreen extends Screen {
    // Standard book GUI texture
    public static final ResourceLocation BOOK_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/book.png");

    private final BookAccess bookAccess;
    private List<FormattedCharSequence> cachedPageComponents;
    private Component pageMsg;

    public NoteViewScreen(BookAccess bookAccess) {
        super(GameNarrator.NO_TITLE);
        this.cachedPageComponents = Collections.emptyList();
        this.pageMsg = CommonComponents.EMPTY;
        this.bookAccess = bookAccess;
    }

    @Override
    protected void init() {
        // done button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (btn) -> this.onClose()).bounds(this.width / 2 - 50, 196, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // calculate X coordinate to center the book texture on screen
        int i = (this.width - 192) / 2;

        // split raw note text into lines that fit the book page width (114 pixels)
        if (this.cachedPageComponents.isEmpty()) {
            FormattedText formattedtext = this.bookAccess.getPage(0);
            this.cachedPageComponents = this.font.split(formattedtext, 114);
        }

        // draw up to 14 lines of text on the note
        int k = Math.min(14, this.cachedPageComponents.size());
        for (int l = 0; l < k; ++l) {
            FormattedCharSequence formattedcharsequence = this.cachedPageComponents.get(l);
            guiGraphics.drawString(this.font, formattedcharsequence, i + 36, 32 + l * 9, 0, false);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(guiGraphics);
        guiGraphics.blit(BOOK_LOCATION, (this.width - 192) / 2, 2, 0, 0, 192, 192);
    }

    // Helper record to extract and store text pages from the note item
    @OnlyIn(Dist.CLIENT)
    public static record BookAccess(List<Component> pages) {

        // Get the first page content
        public FormattedText getPage(int page) {
            return !this.pages.isEmpty() ? this.pages.get(0) : FormattedText.EMPTY;
        }

        // Helper method to create BookAccess from a written note item stack
        @Nullable
        public static BookAccess fromItem(ItemStack stack) {
            NoteContent content = stack.get(MODDATACOMPONENTS.NOTE_CONTENT.get());
            if (content != null) {
                return new BookAccess(List.of(Component.literal(content.text())));
            }
            return null;
        }
    }
}