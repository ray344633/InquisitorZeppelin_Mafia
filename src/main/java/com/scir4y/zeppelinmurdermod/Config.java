package com.scir4y.zeppelinmurdermod;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue ROUND_DURATION =
            BUILDER.comment("Round's duration in seconds")
                    .defineInRange("roundDuration", 600, 30, 3600);

    public static final ModConfigSpec.IntValue MIN_PLAYERS =
            BUILDER.comment("Minimal amount of players for start")
                    .defineInRange("minPlayers", 6, 3, 12);

    public static final ModConfigSpec.IntValue MAX_PLAYERS =
            BUILDER.comment("Maximal amount of players for start")
                    .defineInRange("maxPlayers", 12, 6, 24);

    public static final ModConfigSpec.IntValue MAX_MOOD_AMOUNT =
            BUILDER.comment("Maximal amount of mood")
                    .defineInRange("maxMood", 100, 1, 1000);

    public static final ModConfigSpec.IntValue MOOD_DISCOUNT_SPEED =
            BUILDER.comment("Mood discount speed in seconds")
                    .defineInRange("moodDiscountSpeed", 1, 1, 1000);

    static final ModConfigSpec SPEC = BUILDER.build();
}
