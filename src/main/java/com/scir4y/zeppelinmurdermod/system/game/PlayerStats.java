package com.scir4y.zeppelinmurdermod.system.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class PlayerStats {
    // Codec for serializing PlayerStats to better data format
    public static final Codec<PlayerStats> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("wins").forGetter(s -> s.wins),
            // INT - field data type
            // fieldOf("wins") - key name in serialized data ("wins")
            // forGetter(s -> s.wins) - explain how to get this value from object PlayerStats
            Codec.INT.fieldOf("kills").forGetter(s -> s.kills)
    ).apply(inst, PlayerStats::new));
    // .apply(inst, PlayerStats::new) = (wins, kills) -> new PlayerStats(wins, kills)

    public int wins;
    public int kills;

    public PlayerStats() {
        this(0, 0);
    }

    public PlayerStats(int wins, int kills) {
        this.wins = wins;
        this.kills = kills;
    }

    public void addWin() { wins++; }
    public void addKill() { kills++; }
}
