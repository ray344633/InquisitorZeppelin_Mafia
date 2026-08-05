package com.scir4y.zeppelinmurdermod.registry;

import com.scir4y.zeppelinmurdermod.system.task.Task;
import com.scir4y.zeppelinmurdermod.system.task.TaskManager;
import com.scir4y.zeppelinmurdermod.system.task.condition.PickPoppyCondition;

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
                    (player, level) -> false
            )
    );

    public static final Task PICK_POPPY_TASK = TaskManager.register(
            "pick_poppy_task",
            new Task(
                    TaskManager.makeId("pick_poppy_task"),
                    "Pick a poppy",
                    15,
                    ResourceLocation.fromNamespaceAndPath(ZeppelinMurderMod.MODID, "shaders/post/test.json"),
                    new PickPoppyCondition()
            )
    );

    public static void register() {}
}
