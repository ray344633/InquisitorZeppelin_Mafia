package com.scir4y.zeppelinmurdermod.common.task;

import net.minecraft.resources.ResourceLocation;
import java.util.function.BooleanSupplier;

public class Task extends AbstractTask {
    public Task(ResourceLocation id, String taskDescription, int moodPoints,
                ResourceLocation shaderPath, BooleanSupplier condition) {
        super(id, taskDescription, moodPoints, shaderPath, condition);
    }
}