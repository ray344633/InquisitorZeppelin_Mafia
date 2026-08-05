package com.scir4y.zeppelinmurdermod.system.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.scir4y.zeppelinmurdermod.client.gui.MoodBar;
import com.scir4y.zeppelinmurdermod.client.gui.TaskHud;
import com.scir4y.zeppelinmurdermod.config.Config;
import com.scir4y.zeppelinmurdermod.registry.ModTasks;
import com.scir4y.zeppelinmurdermod.system.ModAttachments;
import com.scir4y.zeppelinmurdermod.system.task.Task;
import com.scir4y.zeppelinmurdermod.system.task.TaskManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class PlayerRoundData {
    // client variables
    public float currentMoodAmount;
    public Task currentTask; // could be null if previous task was completed

    // server only variables
    public int nextTaskDelayTicks = -1; // countdown state if is not active

    public PlayerRoundData() {
        this.currentMoodAmount = Config.MAX_MOOD_AMOUNT.getAsInt();
        this.currentTask = ModTasks.PICK_POPPY_TASK;
    }

    /**
     * A partial {@link StreamCodec} used exclusively for the NeoForge attachment
     * .sync(...) mechanism (see {@link ModAttachments}).
     *
     * Only the fields that the client actually needs are transmitted:
     *   currentMoodAmount displayed by {@link MoodBar}
     *   currentTask displayed by {@link TaskHud}.
     *       Since tasks are registered objects (not inline data), only the task's
     *       {@link ResourceLocation} ID is sent; the client resolves it back
     *       to a {@link Task} instance via {@link TaskManager#getTask}.
     *
     * Server-only fields such as nextTaskDelayTicks are intentionally excluded
     * from this codec they are never needed by the client and must not be exposed.
     *
     * Wire format (in order):
     *   float - currentMoodAmount}
     *   boolean - whether a task is present (true = has task)
     *   ResourceLocation (only if previous boolean was true) - task ID
     *
     */
    public static final StreamCodec<ByteBuf, PlayerRoundData> SYNC_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeFloat(data.currentMoodAmount);
                buf.writeBoolean(data.currentTask != null);
                if (data.currentTask != null) {
                    ResourceLocation.STREAM_CODEC.encode(buf, data.currentTask.getId());
                }
            },
            (buf) -> {
                PlayerRoundData d = new PlayerRoundData();
                d.currentMoodAmount = buf.readFloat();
                boolean hasTask = buf.readBoolean();
                if (hasTask) {
                    ResourceLocation taskId = ResourceLocation.STREAM_CODEC.decode(buf);
                    d.currentTask = (Task) TaskManager.getTask(taskId).orElse(null);
                } else {
                    d.currentTask = null;
                }
                return d;
            }
    );

    /**
     * A full {@link Codec} used for disk serialization (NBT save/load).
     *
     * NeoForge's attachment system calls this codec when saving the world and when
     * restoring data on player respawn (via copyOnDeath()).
     *
     * It encodes the same client-visible fields as {@link #SYNC_CODEC}, but uses
     * Mojang's data-driven {@link RecordCodecBuilder} instead of a manual byte-buffer
     * approach, which makes the saved format human-readable in NBT.
     *
     * Server-only fields (e.g. nextTaskDelayTicks) are not persisted here;
     * they reset to their defaults on construction.
     */
    public static final Codec<PlayerRoundData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("currentMoodAmount").forGetter(d -> d.currentMoodAmount),
            ResourceLocation.CODEC.optionalFieldOf("currentTaskId").forGetter(
                    d -> d.currentTask != null ? Optional.of(d.currentTask.getId()) : Optional.empty())
    ).apply(instance, (mood, taskId) -> {
        PlayerRoundData d = new PlayerRoundData();
        d.currentMoodAmount = mood;
        d.currentTask = taskId
                .flatMap(id -> TaskManager.getTask(id).map(t -> (Task) t))
                .orElse(null);
        return d;
    }));
}
