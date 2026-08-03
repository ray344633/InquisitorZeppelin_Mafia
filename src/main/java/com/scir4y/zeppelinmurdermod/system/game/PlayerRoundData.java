package com.scir4y.zeppelinmurdermod.system.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.scir4y.zeppelinmurdermod.config.Config;
import com.scir4y.zeppelinmurdermod.registry.ModTasks;
import com.scir4y.zeppelinmurdermod.system.task.Task;
import com.scir4y.zeppelinmurdermod.system.task.TaskManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import com.scir4y.zeppelinmurdermod.config.Config;

public class PlayerRoundData {
    // client variables
    public int currentMoodAmount;
    public Task currentTask;

    // server only variables

    public PlayerRoundData() {
        this.currentMoodAmount = Config.MAX_MOOD_AMOUNT.getAsInt();
        this.currentTask = ModTasks.TEST_TASK;
    }

    // Кодек только для публичной части (используется в .sync(...))
    public static final StreamCodec<ByteBuf, PlayerRoundData> SYNC_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeInt(data.currentMoodAmount);
                ResourceLocation.STREAM_CODEC.encode(buf, data.currentTask.getId());
            },
            (buf) -> {
                PlayerRoundData d = new PlayerRoundData();
                d.currentMoodAmount = buf.readInt();
                ResourceLocation taskId = ResourceLocation.STREAM_CODEC.decode(buf);
                d.currentTask = (Task) TaskManager.getTask(taskId).orElse(ModTasks.TEST_TASK);
                return d;
            }
    );

    public static final Codec<PlayerRoundData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("currentMoodAmount").forGetter(d -> d.currentMoodAmount),
            ResourceLocation.CODEC.fieldOf("currentTaskId").forGetter(d -> d.currentTask.getId())
    ).apply(instance, (mood, taskId) -> {
        PlayerRoundData d = new PlayerRoundData();
        d.currentMoodAmount = mood;
        d.currentTask = (Task) TaskManager.getTask(taskId).orElse(ModTasks.TEST_TASK);
        return d;
    }));
}