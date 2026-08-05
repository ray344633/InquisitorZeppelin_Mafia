package com.scir4y.zeppelinmurdermod.command;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.scir4y.zeppelinmurdermod.config.Config;
import com.scir4y.zeppelinmurdermod.network.payload.SyncMoodPayload;
import com.scir4y.zeppelinmurdermod.system.game.GameState;
import com.scir4y.zeppelinmurdermod.system.game.PlayerRoleManager;
import com.scir4y.zeppelinmurdermod.system.game.PlayerRoundData;
import com.scir4y.zeppelinmurdermod.system.game.Role;
import com.scir4y.zeppelinmurdermod.system.task.AbstractTask;
import com.scir4y.zeppelinmurdermod.system.task.Task;
import com.scir4y.zeppelinmurdermod.system.task.TaskManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.scir4y.zeppelinmurdermod.system.ModAttachments.PLAYER_ROUND_DATA;

@EventBusSubscriber(modid = ZeppelinMurderMod.MODID)
public class CommandRegisterHandler {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("mood")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.players())

                                // /mood <target> get
                                .then(Commands.literal("get")
                                        .executes(context -> {
                                            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "target");

                                            for (ServerPlayer player : targets) {
                                                float currentMood = getPlayerMood(player);

                                                context.getSource().sendSuccess(
                                                        () -> Component.literal(player.getScoreboardName() + "'s mood is: " + currentMood),
                                                        false
                                                );
                                            }

                                            return targets.size();
                                        })
                                )

                                // /mood <target> set <mood_value>
                                .then(Commands.literal("set")
                                        .then(Commands.argument("mood", FloatArgumentType.floatArg())
                                                .executes(context -> {
                                                    Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "target");
                                                    float newMood = FloatArgumentType.getFloat(context, "mood");

                                                    for (ServerPlayer player : targets) {
                                                        setPlayerMood(player, newMood);

                                                        player.sendSystemMessage(Component.literal("Your mood has been set to: " + newMood));
                                                    }

                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal("Set mood to " + newMood + " for " + targets.size() + " player(s)."),
                                                            true
                                                    );

                                                    return targets.size();
                                                })
                                        )
                                )
                        )
        );

        event.getDispatcher().register(
                Commands.literal("mafia")
                        .requires(source -> source.hasPermission(2))

                        // /mafia role <target> get|set <role>
                        .then(Commands.literal("role")
                                .then(Commands.argument("target", EntityArgument.players())
                                        .then(Commands.literal("get")
                                                .executes(context -> {
                                                    Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "target");

                                                    for (ServerPlayer player : targets) {
                                                        Role role = PlayerRoleManager.getRole(player);

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(player.getScoreboardName() + "'s role is: " + role),
                                                                false
                                                        );
                                                    }

                                                    return targets.size();
                                                })
                                        )
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("role", StringArgumentType.word())
                                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                                Arrays.stream(Role.values()).map(Enum::name), builder))
                                                        .executes(context -> {
                                                            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "target");
                                                            String roleName = StringArgumentType.getString(context, "role").toUpperCase();

                                                            Role role;
                                                            try {
                                                                role = Role.valueOf(roleName);
                                                            } catch (IllegalArgumentException e) {
                                                                context.getSource().sendFailure(Component.literal("Unknown role: " + roleName));
                                                                return 0;
                                                            }

                                                            for (ServerPlayer player : targets) {
                                                                PlayerRoleManager.setRole(player, role);
                                                                player.sendSystemMessage(Component.literal("Your role has been set to: " + role));
                                                            }

                                                            Role finalRole = role;
                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal("Set role to " + finalRole + " for " + targets.size() + " player(s)."),
                                                                    true
                                                            );

                                                            return targets.size();
                                                        })
                                                )
                                        )
                                )
                        )

                        // /mafia task <target> get|set|clear
                        .then(Commands.literal("task")
                                .then(Commands.argument("target", EntityArgument.players())
                                        .then(Commands.literal("get")
                                                .executes(context -> {
                                                    Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "target");

                                                    for (ServerPlayer player : targets) {
                                                        AbstractTask task = player.getData(PLAYER_ROUND_DATA).currentTask;
                                                        String description = task != null
                                                                ? task.getId() + " (" + task.getTaskDescription() + ")"
                                                                : "none";

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(player.getScoreboardName() + "'s task is: " + description),
                                                                false
                                                        );
                                                    }

                                                    return targets.size();
                                                })
                                        )
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("task", ResourceLocationArgument.id())
                                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                                TaskManager.getAllTasks().keySet().stream().map(ResourceLocation::toString), builder))
                                                        .executes(context -> {
                                                            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "target");
                                                            ResourceLocation taskId = ResourceLocationArgument.getId(context, "task");

                                                            AbstractTask found = TaskManager.getTask(taskId).orElse(null);
                                                            if (found == null) {
                                                                context.getSource().sendFailure(Component.literal("Unknown task: " + taskId));
                                                                return 0;
                                                            }
                                                            Task task = (Task) found;

                                                            for (ServerPlayer player : targets) {
                                                                PlayerRoundData roundData = player.getData(PLAYER_ROUND_DATA);
                                                                roundData.currentTask = task;
                                                                roundData.nextTaskDelayTicks = -1;
                                                                player.setData(PLAYER_ROUND_DATA, roundData);
                                                            }

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal("Set task " + taskId + " for " + targets.size() + " player(s)."),
                                                                    true
                                                            );

                                                            return targets.size();
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("clear")
                                                .executes(context -> {
                                                    Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "target");

                                                    for (ServerPlayer player : targets) {
                                                        PlayerRoundData roundData = player.getData(PLAYER_ROUND_DATA);
                                                        roundData.currentTask = null;
                                                        roundData.nextTaskDelayTicks = -1;
                                                        player.setData(PLAYER_ROUND_DATA, roundData);
                                                    }

                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal("Cleared task for " + targets.size() + " player(s)."),
                                                            true
                                                    );

                                                    return targets.size();
                                                })
                                        )
                                )
                        )

                        // /mafia start
                        .then(Commands.literal("start")
                                .executes(CommandRegisterHandler::startGame)
                        )

                        // /mafia stop
                        .then(Commands.literal("stop")
                                .executes(context -> {
                                    GameState.stopGame();
                                    context.getSource().sendSuccess(() -> Component.literal("Game stopped."), true);
                                    return 1;
                                })
                        )
        );
    }

    private static int startGame(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();

        // All players with the PLAYER role (not NONE — those are not in the game,
        // and not SPECTATOR — those are only observing)
        List<ServerPlayer> participants = server.getPlayerList().getPlayers().stream()
                .filter(player -> PlayerRoleManager.getRole(player) == Role.PLAYER)
                .toList();

        int count = participants.size();
        int minPlayers = Config.MIN_PLAYERS.getAsInt();
        int maxPlayers = Config.MAX_PLAYERS.getAsInt();

        if (count < minPlayers) {
            context.getSource().sendFailure(Component.literal(
                    "Not enough players to start: " + count + "/" + minPlayers + " (minimum)."));
            return 0;
        }

        if (count > maxPlayers) {
            context.getSource().sendFailure(Component.literal(
                    "Too many players to start: " + count + "/" + maxPlayers + " (maximum)."));
            return 0;
        }

        GameState.startGame();

        for (ServerPlayer player : participants) {
            PlayerRoundData roundData = new PlayerRoundData();
            player.setData(PLAYER_ROUND_DATA, roundData);

            PacketDistributor.sendToPlayer(player, new SyncMoodPayload(roundData.currentMoodAmount));
            player.sendSystemMessage(Component.literal("The game has started! Your mood will start decreasing."));
        }

        context.getSource().sendSuccess(
                () -> Component.literal("Game started with " + count + " player(s)."), true);

        return count;
    }

    private static float getPlayerMood(ServerPlayer player) {
        return player.getData(PLAYER_ROUND_DATA).currentMoodAmount;
    }

    private static void setPlayerMood(ServerPlayer player, float newMood) {
        player.getData(PLAYER_ROUND_DATA).currentMoodAmount = newMood;
        PacketDistributor.sendToPlayer(player, new SyncMoodPayload(newMood));
    }
}