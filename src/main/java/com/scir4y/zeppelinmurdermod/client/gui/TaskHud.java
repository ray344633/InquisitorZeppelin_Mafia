package com.scir4y.zeppelinmurdermod.client.gui;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.system.task.AbstractTask;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.Objects;

import static com.scir4y.zeppelinmurdermod.system.ModAttachments.PLAYER_ROUND_DATA;

public class TaskHud {

    // Fixed position on screen (top-left corner, below the MoodBar)
    private static final int MARGIN_X = 10;
    private static final int MARGIN_Y = 52; // below the MoodBar (10 + 16*2 + 10)

    private static final long ANIM_DURATION_MS = 300;
    private static final int SLIDE_DISTANCE = 40;

    private enum Phase { HIDDEN, APPEARING, VISIBLE, DISAPPEARING }

    private static Phase phase = Phase.HIDDEN;
    private static String displayedText = null;
    private static String pendingText = null;
    private static ResourceLocation lastServerTaskId = null;
    private static long phaseStartMs = 0L;
    private static boolean initialized = false;

    private TaskHud() {}

    public static void renderLayer(GuiGraphics gfx, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        AbstractTask currentTask = mc.player.getData(PLAYER_ROUND_DATA).currentTask;
        render(gfx, mc.font, currentTask, MARGIN_X, MARGIN_Y);
    }

    public static int render(GuiGraphics gfx, Font font, AbstractTask currentTask, int x, int y) {
        onTaskChanged(currentTask);
        progressPhase();

        if (phase == Phase.HIDDEN || displayedText == null) {
            return y;
        }

        float t = Mth.clamp((System.currentTimeMillis() - phaseStartMs) / (float) ANIM_DURATION_MS, 0f, 1f);
        float eased = t * t * (3f - 2f * t); // smoothstep

        float alpha;
        float xOffset;

        if (phase == Phase.APPEARING) {
            alpha = eased;
            xOffset = SLIDE_DISTANCE * (1f - eased);
        } else if (phase == Phase.DISAPPEARING) {
            alpha = 1f - eased;
            xOffset = -SLIDE_DISTANCE * eased;
        } else {
            alpha = 1f;
            xOffset = 0f;
        }

        int alphaByte = Mth.clamp((int) (alpha * 255f), 0, 255);
        int color = (alphaByte << 24) | 0xFFAA00;

        gfx.drawString(font, Component.literal(displayedText), x + Math.round(xOffset), y, color, true);

        return y + font.lineHeight + 4;
    }


    /**
     * Compares the newly received task against the last known task.
     * Triggers the appropriate animation phase when a change is detected.
     */
    private static void onTaskChanged(AbstractTask currentTask) {
        ResourceLocation newId = currentTask != null ? currentTask.getId() : null;

        if (!initialized) {
            initialized = true;
            lastServerTaskId = newId;
            if (newId != null) {
                displayedText = currentTask.getTaskDescription();
                phase = Phase.APPEARING;
                phaseStartMs = System.currentTimeMillis();
            }
            return;
        }

        if (Objects.equals(newId, lastServerTaskId)) {
            return;
        }
        lastServerTaskId = newId;

        if (newId == null) {
            // Task was completed or cleared - slide out the current label
            if (displayedText != null) {
                phase = Phase.DISAPPEARING;
                phaseStartMs = System.currentTimeMillis();
            }
            pendingText = null;
        } else {
            String desc = currentTask.getTaskDescription();
            if (displayedText == null) {
                // Nothing is currently shown - slide the new task in immediately
                displayedText = desc;
                phase = Phase.APPEARING;
                phaseStartMs = System.currentTimeMillis();
            } else {
                // A different task was assigned while something is still visible -
                // finish the disappear animation first, then slide the new one in
                pendingText = desc;
                phase = Phase.DISAPPEARING;
                phaseStartMs = System.currentTimeMillis();
            }
        }
    }

    /**
     * Advances the animation phase once the current animation duration has elapsed.
     * APPEARING -> VISIBLE
     * DISAPPEARING + pendingText -> APPEARING (cross-fade to next task)
     * DISAPPEARING + no pending -> HIDDEN
     *
     */
    private static void progressPhase() {
        if (phase != Phase.APPEARING && phase != Phase.DISAPPEARING) {
            return;
        }

        long elapsed = System.currentTimeMillis() - phaseStartMs;
        if (elapsed < ANIM_DURATION_MS) {
            return;
        }

        if (phase == Phase.APPEARING) {
            phase = Phase.VISIBLE;
            return;
        }

        if (pendingText != null) {
            displayedText = pendingText;
            pendingText = null;
            phase = Phase.APPEARING;
            phaseStartMs = System.currentTimeMillis();
        } else {
            displayedText = null;
            phase = Phase.HIDDEN;
        }
    }
}
