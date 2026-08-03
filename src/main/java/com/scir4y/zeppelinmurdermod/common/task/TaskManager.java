package com.scir4y.zeppelinmurdermod.common.task;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TaskManager {
    private static final Map<ResourceLocation, AbstractTask> TASKS = new HashMap<>();

    private TaskManager() {}

    public static ResourceLocation makeId(String id) {
        return ResourceLocation.fromNamespaceAndPath(ZeppelinMurderMod.MODID, id);
    }

    public static Task register(String id, Task task) {
        ResourceLocation location = task.getId();
        if (TASKS.containsKey(location)) {
            throw new IllegalArgumentException("Task with ID " + location + " is already registered!");
        }
        TASKS.put(location, task);
        return task;
    }

    public static Optional<AbstractTask> getTask(ResourceLocation id) {
        return Optional.ofNullable(TASKS.get(id));
    }

    public static Map<ResourceLocation, AbstractTask> getAllTasks() {
        return Collections.unmodifiableMap(TASKS);
    }
}