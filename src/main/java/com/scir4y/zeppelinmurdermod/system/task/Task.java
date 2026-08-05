package com.scir4y.zeppelinmurdermod.system.task;

import net.minecraft.resources.ResourceLocation;

public class Task extends AbstractTask {
    public Task(ResourceLocation id, String taskDescription, int moodPoints,
                ResourceLocation shaderPath, TaskCondition condition) {
        super(id, taskDescription, moodPoints, shaderPath, condition);
    }
}
