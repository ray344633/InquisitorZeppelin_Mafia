package com.scir4y.zeppelinmurdermod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.scir4y.zeppelinmurdermod.network.SyncMoodPayload;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;

import static com.scir4y.zeppelinmurdermod.data.ModAttachments.PLAYER_ROUND_DATA;

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
                                                int currentMood = getPlayerMood(player);

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
                                        .then(Commands.argument("mood", IntegerArgumentType.integer())
                                                .executes(context -> {
                                                    Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "target");
                                                    int newMood = IntegerArgumentType.getInteger(context, "mood");

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
    }

    private static int getPlayerMood(ServerPlayer player) {
        return player.getData(PLAYER_ROUND_DATA).currentMoodAmount;
    }

    private static void setPlayerMood(ServerPlayer player, int newMood) {
        player.getData(PLAYER_ROUND_DATA).currentMoodAmount = newMood;
        PacketDistributor.sendToPlayer(player, new SyncMoodPayload(newMood));
    }
}