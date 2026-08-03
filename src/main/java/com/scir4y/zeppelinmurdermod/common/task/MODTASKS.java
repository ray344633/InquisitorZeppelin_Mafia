package com.scir4y.zeppelinmurdermod.common.task;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import net.minecraft.resources.ResourceLocation;

public class MODTASKS {
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