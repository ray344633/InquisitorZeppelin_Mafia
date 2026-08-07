package com.scir4y.zeppelinmurdermod.system.game;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.config.Config;
import com.scir4y.zeppelinmurdermod.network.payload.SyncMoodPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.scir4y.zeppelinmurdermod.system.ModAttachments.PLAYER_ROUND_DATA;

/**
 * every server tick (if isGameStarted) decrease currentMoodAmount
 * for all players with role Role.PLAYER by Config.MOOD_DISCOUNT_SPEED / TICKS_PER_SECOND
 * Config.MOOD_DISCOUNT_SPEED measured in m/s (mood per second)
 * and send to player via SyncMoodPayload.
 * Role.NONE and Role.SPECTATOR currently are unaffected.
 */
@EventBusSubscriber(modid = ZeppelinMurderMod.MODID)
public class MoodTickHandler {

    private static final int TICKS_PER_SECOND = 20;

    // every server tick
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // check game status
        if (!GameState.isGameStarted()) {
            return;
        }

        // get discount value
        int discount = Config.MOOD_DISCOUNT_SPEED.getAsInt();
        if (discount <= 0) {
            return;
        }

        // discount per tick value
        float discountPerTick = discount / (float) TICKS_PER_SECOND;

        // checks all players
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            // check player's role
            if (PlayerRoleManager.getRole(player) != Role.PLAYER) {
                continue;
            }
            // get player's Data
            var roundData = player.getData(PLAYER_ROUND_DATA);

            // check MoodAmount
            if (roundData.currentMoodAmount <= 0) {
                continue;
            }

            // set new currentMoodAmount
            roundData.currentMoodAmount = Math.max(0f, roundData.currentMoodAmount - discountPerTick);
            // send data to player client
            PacketDistributor.sendToPlayer(player, new SyncMoodPayload(roundData.currentMoodAmount));
        }
    }
}
