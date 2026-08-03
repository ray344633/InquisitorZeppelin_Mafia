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



    public static final Supplier<AttachmentType<PlayerStats>> PLAYER_STATS =
            ATTACHMENT_TYPES.register("player_stats",
                    () -> AttachmentType.builder(PlayerStats::new)
                            .serialize(PlayerStats.CODEC)
                            .copyOnDeath()
                            .build());

    public static final Supplier<AttachmentType<PlayerRoundData>> PLAYER_ROUND_DATA =
            ATTACHMENT_TYPES.register("player_round_data",
                    () -> AttachmentType.builder(PlayerRoundData::new)
                            .serialize(PlayerRoundData.CODEC)
                            .copyOnDeath()
                            .sync(PlayerRoundData.SYNC_CODEC)
                            .build());
}
