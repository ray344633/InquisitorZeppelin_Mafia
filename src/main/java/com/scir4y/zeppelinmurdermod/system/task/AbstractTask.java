package com.scir4y.zeppelinmurdermod.system.task;

import net.minecraft.resources.ResourceLocation;
import java.util.function.BooleanSupplier;

public abstract class AbstractTask {
    protected final ResourceLocation id;
    protected final String taskDescription;
    protected final int moodPoints;
    protected final ResourceLocation shaderPath;
    protected final BooleanSupplier completionCondition;

    public AbstractTask(ResourceLocation id, String taskDescription, int moodPoints,
                        ResourceLocation shaderPath, BooleanSupplier completionCondition) {
        this.id = id;
        this.taskDescription = taskDescription;
        this.moodPoints = moodPoints;
        this.shaderPath = shaderPath;
        this.completionCondition = completionCondition != null ? completionCondition : () -> false;
    }

    public ResourceLocation getId() {
        return id;
    }

    public boolean checkCompletion() {
        return completionCondition.getAsBoolean();
    }

    public String getTaskDescription() { return taskDescription; }
    public int getMoodPoints() { return moodPoints; }
    public ResourceLocation getShaderPath() { return shaderPath; }
}