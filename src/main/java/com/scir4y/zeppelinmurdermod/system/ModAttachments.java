package com.scir4y.zeppelinmurdermod.system;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;
import com.scir4y.zeppelinmurdermod.system.game.PlayerStats;
import com.scir4y.zeppelinmurdermod.system.game.PlayerRoundData;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "mafia");
    // "mafia" - mod id
    // register of ATTACHMENT_TYPES
    // all attachments of this class list under the "mafia"

    // Attachment is a method of storing own data on objects
    // create Attachment with type PlayerStats - we save attachment like PlayerStats object
    public static final Supplier<AttachmentType<PlayerStats>> PLAYER_STATS =
            // register attachment with identifier "player_stats" -> mafia:player_stats
            ATTACHMENT_TYPES.register("player_stats",
                    // new object PlayerStats
                    () -> AttachmentType.builder(PlayerStats::new)
                            // serialize from PlayerStats to JSON format and vice versa
                            .serialize(PlayerStats.CODEC)
                            // without this all player_states would reset after death
                            .copyOnDeath()
                            // build AttachmentType
                            .build());

    // create Attachment with type PlayerRoundData - we save attachment like PlayerRoundData object
    public static final Supplier<AttachmentType<PlayerRoundData>> PLAYER_ROUND_DATA =
            // register attachment with identifier "player_round_data" -> mafia:player_round_data
            ATTACHMENT_TYPES.register("player_round_data",
                    // new object PlayerRoundData
                    () -> AttachmentType.builder(PlayerRoundData::new)
                            // serialize from PlayerRoundData to JSON format and vice versa
                            .serialize(PlayerRoundData.CODEC)
                            // without this all player_states would reset after death
                            .copyOnDeath()
                            // ONLY server see saved data(serialize())
                            // server -> nbt
                            // for synchronization we use sync
                            // server -> network packet -> client
                            .sync(PlayerRoundData.SYNC_CODEC)
                            // build AttachmentType
                            .build());
}
