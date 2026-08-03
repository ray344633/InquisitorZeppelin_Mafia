package com.scir4y.zeppelinmurdermod.system.game;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.config.Config;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import com.scir4y.zeppelinmurdermod.config.Config;

public class GameState {
    private static boolean isGameStarted = false;
    private static int currentRoundTime = 0;

    public static boolean isGameStarted() {
        return isGameStarted;
    }

    public static int getCurrentRoundTime() {
        return currentRoundTime;
    }

    public static void setCurrentRoundTime(int time) {
        currentRoundTime = time;
    }

    public static void startGame() {
        isGameStarted = true;
        currentRoundTime = Config.ROUND_DURATION.getAsInt();
    }

    public static void stopGame() {
        isGameStarted = false;
        currentRoundTime = 0;
    }

    public static void reset() {
        isGameStarted = false;
        currentRoundTime = Config.ROUND_DURATION.getAsInt();
    }

    @EventBusSubscriber(modid = ZeppelinMurderMod.MODID)
    public static class LifecycleHandler {
        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            GameState.reset();
        }

        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) {
            GameState.reset();
        }
    }
}