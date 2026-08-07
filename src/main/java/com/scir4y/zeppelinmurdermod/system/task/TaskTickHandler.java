package com.scir4y.zeppelinmurdermod.system.task;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.config.Config;
import com.scir4y.zeppelinmurdermod.network.payload.SyncMoodPayload;
import com.scir4y.zeppelinmurdermod.system.game.GameState;
import com.scir4y.zeppelinmurdermod.system.game.PlayerRoleManager;
import com.scir4y.zeppelinmurdermod.system.game.PlayerRoundData;
import com.scir4y.zeppelinmurdermod.system.game.Role;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.scir4y.zeppelinmurdermod.system.ModAttachments.PLAYER_ROUND_DATA;

/**
 * every server tick (if isGameStarted) for every player with Role.PLAYER:
 * - if currentTask != null: check his completionCondition;
 * - if is completed:
 *   increase player's currentMoodAmount by moodPoints of currentTask (not higher than MAX_MOOD_AMOUNT),
 *   reset currentTask and started random countdown
 *   (TASK_INTERVAL_MIN..TASK_INTERVAL_MAX seconds) before next task;
 * - if currentTask == null: nextTaskDelayTicks, and when it equals 0 take a task for a player.
 * change currentTask send to client via sync attachment'а
 */

@EventBusSubscriber(modid = ZeppelinMurderMod.MODID)
public class TaskTickHandler {

    private static final int TICKS_PER_SECOND = 20;

    // every server tick
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // check game status
        if (!GameState.isGameStarted()) {
            return;
        }

        // check all players by cycle
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (PlayerRoleManager.getRole(player) != Role.PLAYER) {
                continue;
            }

            PlayerRoundData roundData = player.getData(PLAYER_ROUND_DATA);

            if (roundData.currentTask != null) {
                handleTaskCompletion(player, roundData); // finish task and increase currentMoodAmount
            } else {
                handleTaskCooldown(player, roundData); // task timer decrease and new task picker and setter
            }
        }
    }

    private static void handleTaskCompletion(ServerPlayer player, PlayerRoundData roundData) {
        AbstractTask currentTask = roundData.currentTask;

        if (!currentTask.checkCompletion(player, player.serverLevel())) {
            return;
        }

        float maxMood = Config.MAX_MOOD_AMOUNT.getAsInt();
        roundData.currentMoodAmount = Math.min(maxMood, roundData.currentMoodAmount + currentTask.getMoodPoints());

        roundData.currentTask = null;
        roundData.nextTaskDelayTicks = randomDelayTicks(); // random task timer
        player.setData(PLAYER_ROUND_DATA, roundData); // set new data

        // send to player client
        PacketDistributor.sendToPlayer(player, new SyncMoodPayload(roundData.currentMoodAmount));
    }

    private static void handleTaskCooldown(ServerPlayer player, PlayerRoundData roundData) {
        if (roundData.nextTaskDelayTicks < 0) {
            // a safeguard in case the timer was not set beforehand
            roundData.nextTaskDelayTicks = randomDelayTicks();
            return;
        }

        // decrease timer
        if (roundData.nextTaskDelayTicks > 0) {
            roundData.nextTaskDelayTicks--;
            return;
        }

        // pick new task
        Task nextTask = pickRandomTask();
        if (nextTask == null) {
            return;
        }

        roundData.currentTask = nextTask;
        roundData.nextTaskDelayTicks = -1;
        // set new data
        player.setData(PLAYER_ROUND_DATA, roundData);
    }

    private static int randomDelayTicks() {
        int minSeconds = Config.TASK_INTERVAL_MIN.getAsInt();
        int maxSeconds = Config.TASK_INTERVAL_MAX.getAsInt();

        int low = Math.min(minSeconds, maxSeconds);
        int high = Math.max(minSeconds, maxSeconds);

        int seconds = ThreadLocalRandom.current().nextInt(low, high + 1);
        return seconds * TICKS_PER_SECOND;
    }

    private static Task pickRandomTask() {
        // get all task by TaskManager
        List<Task> tasks = TaskManager.getAllTasks().values().stream()
                .filter(Task.class::isInstance)
                .map(Task.class::cast)
                .toList();

        // check is task null or not
        if (tasks.isEmpty()) {
            return null;
        }

        return tasks.get(ThreadLocalRandom.current().nextInt(tasks.size()));
    }
}
