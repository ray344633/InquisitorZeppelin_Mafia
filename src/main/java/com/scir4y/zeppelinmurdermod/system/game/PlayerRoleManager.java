package com.scir4y.zeppelinmurdermod.system.game;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * save role (Role) every palyer in server memory
 *
 * role often changes (lobby -> round -> lobby)
 * by server restarting mod is restarting (look at {@link GameState}), so role resets
 */

public class PlayerRoleManager {

    private static final Map<UUID, Role> ROLES = new HashMap<>(); // memory for role

    private PlayerRoleManager() {}

    public static Role getRole(UUID playerId) {
        return ROLES.getOrDefault(playerId, Role.NONE);
    }

    public static Role getRole(Player player) {
        return getRole(player.getUUID());
    }

    public static void setRole(UUID playerId, Role role) {
        ROLES.put(playerId, role);
    }

    public static void setRole(Player player, Role role) {
        setRole(player.getUUID(), role);
    }

    public static void clear() {
        ROLES.clear();
    }

    @EventBusSubscriber(modid = ZeppelinMurderMod.MODID)
    public static class LifecycleHandler {
        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            PlayerRoleManager.clear();
        }

        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) {
            PlayerRoleManager.clear();
        }
    }
}
