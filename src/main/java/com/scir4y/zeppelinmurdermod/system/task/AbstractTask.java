package com.scir4y.zeppelinmurdermod.system.task;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public abstract class AbstractTask {
    protected final ResourceLocation id;
    protected final String taskDescription;
    protected final int moodPoints;
    protected final ResourceLocation shaderPath;
    protected final TaskCondition completionCondition;

    public AbstractTask(ResourceLocation id, String taskDescription, int moodPoints,
                        ResourceLocation shaderPath, TaskCondition completionCondition) {
        this.id = id;
        this.taskDescription = taskDescription;
        this.moodPoints = moodPoints;
        this.shaderPath = shaderPath;
        this.completionCondition = completionCondition != null ? completionCondition : (player, level) -> false;
    }

    public ResourceLocation getId() {
        return id;
    }

    public boolean checkCompletion(ServerPlayer player, ServerLevel level) {
        return completionCondition.check(player, level);
    }

    public String getTaskDescription() { return taskDescription; }
    public int getMoodPoints() { return moodPoints; }
    public ResourceLocation getShaderPath() { return shaderPath; }
}
