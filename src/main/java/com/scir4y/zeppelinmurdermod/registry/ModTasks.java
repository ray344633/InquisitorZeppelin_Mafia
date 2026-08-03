package com.scir4y.zeppelinmurdermod.registry;

import com.scir4y.zeppelinmurdermod.system.task.Task;
import com.scir4y.zeppelinmurdermod.system.task.TaskManager;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import net.minecraft.resources.ResourceLocation;

public class ModTasks {
    public static final Task TEST_TASK = TaskManager.register(
            "test_task",
            new Task(
                    TaskManager.makeId("test_task"),
                    "Fix electrical wiring on upper deck",
                    25,
                    ResourceLocation.fromNamespaceAndPath(ZeppelinMurderMod.MODID, "shaders/post/test.json"),
                    () -> false
            )
    );

    public static void register() {}
}