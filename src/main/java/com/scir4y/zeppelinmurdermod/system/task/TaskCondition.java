package com.scir4y.zeppelinmurdermod.system.task;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface TaskCondition {
    boolean check(ServerPlayer player, ServerLevel level);
}
