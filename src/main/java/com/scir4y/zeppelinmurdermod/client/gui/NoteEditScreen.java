package com.scir4y.zeppelinmurdermod.client.gui;

import com.google.common.collect.Lists;
import com.scir4y.zeppelinmurdermod.network.payload.SaveNotePayload;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.font.TextFieldHelper.CursorStep;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import static com.scir4y.zeppelinmurdermod.registry.ModItems.WRITTEN_NOTE;

@OnlyIn(Dist.CLIENT)
public class NoteEditScreen extends Screen {
    private static final int TEXT_WIDTH = 114;
    private static final int TEXT_HEIGHT = 128;
    private static final int IMAGE_WIDTH = 192;
    private static final int IMAGE_HEIGHT = 192;

    private final Player owner;
    private final ItemStack book;
    private boolean isModified;
    private int frameTick;
    private String pageText = "";

    private final TextFieldHelper pageEdit = new TextFieldHelper(
            () -> this.pageText,
            (text) -> this.pageText = text,
            this::getClipboard,
            this::setClipboard,
            (p) -> p.length() < 1024 && this.font.wordWrapHeight(p, 114) <= 128
    );

    private long lastClickTime;
    private int lastIndex = -1;
    private final InteractionHand hand;
    @Nullable
    private DisplayCache displayCache;

    public NoteEditScreen(Player owner, ItemStack book, InteractionHand hand) {
        super(GameNarrator.NO_TITLE);
        this.displayCache = DisplayCache.EMPTY;
        this.owner = owner;
        this.book = book;
        this.hand = hand;

        WritableBookContent content = book.get(DataComponents.WRITABLE_BOOK_CONTENT);
        if (content != null) {
            List<String> pages = content.getPages(Minecraft.getInstance().isTextFilteringEnabled()).toList();
            if (!pages.isEmpty()) {
                this.pageText = pages.get(0);
            }
        }
    }

    private void setClipboard(String val) {
        if (this.minecraft != null) TextFieldHelper.setClipboardContents(this.minecraft, val);
    }

    private String getClipboard() {
        return this.minecraft != null ? TextFieldHelper.getClipboardContents(this.minecraft) : "";
    }

    @Override
    public void tick() {
        super.tick();
        ++this.frameTick;
    }

    @Override
    protected void init() {
        this.clearDisplayCache();
        //Close Button
        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_DONE, (btn) -> {
                    this.saveChanges(); // pocket to server for saving like writable note
                    this.minecraft.setScreen(null);
                }).bounds(this.width / 2 - 100, 196, 98, 20).build()
        );

        //Finish Button
        this.addRenderableWidget(
                Button.builder(Component.translatable("book.finalizeButton"), (btn) ->
                                this.finalizeNote()) // finish ...
                        .bounds(this.width / 2 + 2, 196, 98, 20)
                        .build()
        );
    }

    private void saveChanges() {
        // if is modified
        if (this.isModified) {
            // pocket to server for saving like writable note
            PacketDistributor.sendToServer(new SaveNotePayload(this.pageText, false));
        }
    }

    private void finalizeNote() {
        // pocket to server for saving like written note
        PacketDistributor.sendToServer(new SaveNotePayload(this.pageText, true));
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else {
            boolean flag = this.bookKeyPressed(keyCode, scanCode, modifiers);
            if (flag) {
                this.clearDisplayCache();
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (super.charTyped(codePoint, modifiers)) {
            return true;
        } else if (StringUtil.isAllowedChatCharacter(codePoint)) {
            this.pageEdit.insertText(Character.toString(codePoint));
            this.isModified = true;
            this.clearDisplayCache();
            return true;
        } else {
            return false;
        }
    }

    private boolean bookKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.isSelectAll(keyCode)) {
            this.pageEdit.selectAll();
            return true;
        } else if (Screen.isCopy(keyCode)) {
            this.pageEdit.copy();
            return true;
        } else if (Screen.isPaste(keyCode)) {
            this.pageEdit.paste();
            this.isModified = true;
            return true;
        } else if (Screen.isCut(keyCode)) {
            this.pageEdit.cut();
            this.isModified = true;
            return true;
        } else {
            CursorStep step = Screen.hasControlDown() ? CursorStep.WORD : CursorStep.CHARACTER;
            switch (keyCode) {
                case 257:
                case 335:
                    this.pageEdit.insertText("\n");
                    this.isModified = true;
                    return true;
                case 259:
                    this.pageEdit.removeFromCursor(-1, step);
                    this.isModified = true;
                    return true;
                case 261:
                    this.pageEdit.removeFromCursor(1, step);
                    this.isModified = true;
                    return true;
                case 262:
                    this.pageEdit.moveBy(1, Screen.hasShiftDown(), step);
                    return true;
                case 263:
                    this.pageEdit.moveBy(-1, Screen.hasShiftDown(), step);
                    return true;
                case 264:
                    this.changeLine(1);
                    return true;
                case 265:
                    this.changeLine(-1);
                    return true;
                case 268:
                    this.keyHome();
                    return true;
                case 269:
                    this.keyEnd();
                    return true;
                default:
                    return false;
            }
        }
    }

    private void changeLine(int yChange) {
        int i = this.pageEdit.getCursorPos();
        int j = this.getDisplayCache().changeLine(i, yChange);
        this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
    }

    private void keyHome() {
        if (Screen.hasControlDown()) {
            this.pageEdit.setCursorToStart(Screen.hasShiftDown());
        } else {
            int i = this.pageEdit.getCursorPos();
            int j = this.getDisplayCache().findLineStart(i);
            this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
        }
    }

    private void keyEnd() {
        if (Screen.hasControlDown()) {
            this.pageEdit.setCursorToEnd(Screen.hasShiftDown());
        } else {
            DisplayCache cache = this.getDisplayCache();
            int i = this.pageEdit.getCursorPos();
            int j = cache.findLineEnd(i);
            this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.setFocused(null);

        DisplayCache displayCache = this.getDisplayCache();
        for (LineInfo lineInfo : displayCache.lines) {
            guiGraphics.drawString(this.font, lineInfo.asComponent, lineInfo.x, lineInfo.y, -16777216, false);
        }

        this.renderHighlight(guiGraphics, displayCache.selection);
        this.renderCursor(guiGraphics, displayCache.cursor, displayCache.cursorAtEnd);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(guiGraphics);
        guiGraphics.blit(BookViewScreen.BOOK_LOCATION, (this.width - 192) / 2, 2, 0, 0, 192, 192);
    }

    private void renderCursor(GuiGraphics guiGraphics, Pos2i cursorPos, boolean isEndOfText) {
        if (this.frameTick / 6 % 2 == 0) {
            cursorPos = this.convertLocalToScreen(cursorPos);
            if (!isEndOfText) {
                guiGraphics.fill(cursorPos.x, cursorPos.y - 1, cursorPos.x + 1, cursorPos.y + 9, -16777216);
            } else {
                guiGraphics.drawString(this.font, "_", cursorPos.x, cursorPos.y, 0, false);
            }
        }
    }

    private void renderHighlight(GuiGraphics guiGraphics, Rect2i[] highlightAreas) {
        for (Rect2i rect2i : highlightAreas) {
            guiGraphics.fill(RenderType.guiTextHighlight(), rect2i.getX(), rect2i.getY(), rect2i.getX() + rect2i.getWidth(), rect2i.getY() + rect2i.getHeight(), -16776961);
        }
    }

    private Pos2i convertScreenToLocal(Pos2i screenPos) {
        return new Pos2i(screenPos.x - (this.width - 192) / 2 - 36, screenPos.y - 32);
    }

    private Pos2i convertLocalToScreen(Pos2i localScreenPos) {
        return new Pos2i(localScreenPos.x + (this.width - 192) / 2 + 36, localScreenPos.y + 32);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        } else {
            if (button == 0) {
                long i = Util.getMillis();
                DisplayCache cache = this.getDisplayCache();
                int j = cache.getIndexAtPosition(this.font, this.convertScreenToLocal(new Pos2i((int)mouseX, (int)mouseY)));
                if (j >= 0) {
                    if (j == this.lastIndex && i - this.lastClickTime < 250L) {
                        if (!this.pageEdit.isSelecting()) {
                            this.selectWord(j);
                        } else {
                            this.pageEdit.selectAll();
                        }
                    } else {
                        this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
                    }
                    this.clearDisplayCache();
                }
                this.lastIndex = j;
                this.lastClickTime = i;
            }
            return true;
        }
    }

    private void selectWord(int index) {
        this.pageEdit.setSelectionRange(StringSplitter.getWordPosition(this.pageText, -1, index, false), StringSplitter.getWordPosition(this.pageText, 1, index, false));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        } else {
            if (button == 0) {
                DisplayCache cache = this.getDisplayCache();
                int i = cache.getIndexAtPosition(this.font, this.convertScreenToLocal(new Pos2i((int)mouseX, (int)mouseY)));
                this.pageEdit.setCursorPos(i, true);
                this.clearDisplayCache();
            }
            return true;
        }
    }

    private DisplayCache getDisplayCache() {
        if (this.displayCache == null) {
            this.displayCache = this.rebuildDisplayCache();
        }
        return this.displayCache;
    }

    private void clearDisplayCache() {
        this.displayCache = null;
    }

    private DisplayCache rebuildDisplayCache() {
        String s = this.pageText;
        if (s.isEmpty()) {
            return DisplayCache.EMPTY;
        } else {
            int i = this.pageEdit.getCursorPos();
            int j = this.pageEdit.getSelectionPos();
            IntList intlist = new IntArrayList();
            List<LineInfo> list = Lists.newArrayList();
            MutableInt mutableint = new MutableInt();
            MutableBoolean mutableboolean = new MutableBoolean();
            StringSplitter stringsplitter = this.font.getSplitter();

            stringsplitter.splitLines(s, 114, Style.EMPTY, true, (p_98132_, p_98133_, p_98134_) -> {
                int k3 = mutableint.getAndIncrement();
                String s2 = s.substring(p_98133_, p_98134_);
                mutableboolean.setValue(s2.endsWith("\n"));
                String s3 = StringUtils.stripEnd(s2, " \n");
                int l3 = k3 * 9;
                Pos2i pos2i1 = this.convertLocalToScreen(new Pos2i(0, l3));
                intlist.add(p_98133_);
                list.add(new LineInfo(p_98132_, s3, pos2i1.x, pos2i1.y));
            });

            int[] aint = intlist.toIntArray();
            boolean flag = i == s.length();
            Pos2i bookeditscreen$pos2i;
            if (flag && mutableboolean.isTrue()) {
                bookeditscreen$pos2i = new Pos2i(0, list.size() * 9);
            } else {
                int k = findLineFromPos(aint, i);
                int l = this.font.width(s.substring(aint[k], i));
                bookeditscreen$pos2i = new Pos2i(l, k * 9);
            }

            List<Rect2i> list1 = Lists.newArrayList();
            if (i != j) {
                int l2 = Math.min(i, j);
                int i1 = Math.max(i, j);
                int j1 = findLineFromPos(aint, l2);
                int k1 = findLineFromPos(aint, i1);
                if (j1 == k1) {
                    int l1 = j1 * 9;
                    int i2 = aint[j1];
                    list1.add(this.createPartialLineSelection(s, stringsplitter, l2, i1, l1, i2));
                } else {
                    int i3 = j1 + 1 > aint.length ? s.length() : aint[j1 + 1];
                    list1.add(this.createPartialLineSelection(s, stringsplitter, l2, i3, j1 * 9, aint[j1]));

                    for (int j3 = j1 + 1; j3 < k1; ++j3) {
                        int j2 = j3 * 9;
                        String s1 = s.substring(aint[j3], aint[j3 + 1]);
                        int k2 = (int) stringsplitter.stringWidth(s1);
                        list1.add(this.createSelection(new Pos2i(0, j2), new Pos2i(k2, j2 + 9)));
                    }

                    list1.add(this.createPartialLineSelection(s, stringsplitter, aint[k1], i1, k1 * 9, aint[k1]));
                }
            }

            return new DisplayCache(s, bookeditscreen$pos2i, flag, aint, list.toArray(new LineInfo[0]), list1.toArray(new Rect2i[0]));
        }
    }

    static int findLineFromPos(int[] lineStarts, int find) {
        int i = Arrays.binarySearch(lineStarts, find);
        return i < 0 ? -(i + 2) : i;
    }

    private Rect2i createPartialLineSelection(String input, StringSplitter splitter, int startPos, int endPos, int y, int lineStart) {
        String s = input.substring(lineStart, startPos);
        String s1 = input.substring(lineStart, endPos);
        Pos2i pos2i = new Pos2i((int) splitter.stringWidth(s), y);
        Pos2i pos2i1 = new Pos2i((int) splitter.stringWidth(s1), y + 9);
        return this.createSelection(pos2i, pos2i1);
    }

    private Rect2i createSelection(Pos2i corner1, Pos2i corner2) {
        Pos2i pos2i = this.convertLocalToScreen(corner1);
        Pos2i pos2i1 = this.convertLocalToScreen(corner2);
        int i = Math.min(pos2i.x, pos2i1.x);
        int j = Math.max(pos2i.x, pos2i1.x);
        int k = Math.min(pos2i.y, pos2i1.y);
        int l = Math.max(pos2i.y, pos2i1.y);
        return new Rect2i(i, k, j - i, l - k);
    }

    @OnlyIn(Dist.CLIENT)
    static class DisplayCache {
        static final DisplayCache EMPTY = new DisplayCache("", new Pos2i(0, 0), true, new int[]{0}, new LineInfo[]{new LineInfo(Style.EMPTY, "", 0, 0)}, new Rect2i[0]);
        private final String fullText;
        final Pos2i cursor;
        final boolean cursorAtEnd;
        private final int[] lineStarts;
        final LineInfo[] lines;
        final Rect2i[] selection;

        public DisplayCache(String fullText, Pos2i cursor, boolean cursorAtEnd, int[] lineStarts, LineInfo[] lines, Rect2i[] selection) {
            this.fullText = fullText;
            this.cursor = cursor;
            this.cursorAtEnd = cursorAtEnd;
            this.lineStarts = lineStarts;
            this.lines = lines;
            this.selection = selection;
        }

        public int getIndexAtPosition(Font font, Pos2i cursorPosition) {
            int i = cursorPosition.y / 9;
            if (i < 0) {
                return 0;
            } else if (i >= this.lines.length) {
                return this.fullText.length();
            } else {
                LineInfo lineInfo = this.lines[i];
                return this.lineStarts[i] + font.getSplitter().plainIndexAtWidth(lineInfo.contents, cursorPosition.x, lineInfo.style);
            }
        }

        public int changeLine(int xChange, int yChange) {
            int i = NoteEditScreen.findLineFromPos(this.lineStarts, xChange);
            int j = i + yChange;
            int k;
            if (0 <= j && j < this.lineStarts.length) {
                int l = xChange - this.lineStarts[i];
                int i1 = this.lines[j].contents.length();
                k = this.lineStarts[j] + Math.min(l, i1);
            } else {
                k = xChange;
            }
            return k;
        }

        public int findLineStart(int line) {
            int i = NoteEditScreen.findLineFromPos(this.lineStarts, line);
            return this.lineStarts[i];
        }

        public int findLineEnd(int line) {
            int i = NoteEditScreen.findLineFromPos(this.lineStarts, line);
            return this.lineStarts[i] + this.lines[i].contents.length();
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class LineInfo {
        final Style style;
        final String contents;
        final Component asComponent;
        final int x;
        final int y;

        public LineInfo(Style style, String contents, int x, int y) {
            this.style = style;
            this.contents = contents;
            this.x = x;
            this.y = y;
            this.asComponent = Component.literal(contents).setStyle(style);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Pos2i {
        public final int x;
        public final int y;

        Pos2i(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}