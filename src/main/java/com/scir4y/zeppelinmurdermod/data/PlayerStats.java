package com.scir4y.zeppelinmurdermod.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class PlayerStats {
    public static final Codec<PlayerStats> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("wins").forGetter(s -> s.wins),
            Codec.INT.fieldOf("kills").forGetter(s -> s.kills)
    ).apply(inst, PlayerStats::new));

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
