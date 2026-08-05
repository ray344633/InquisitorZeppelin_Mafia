package com.scir4y.zeppelinmurdermod.system.game;

// TODO for audit ( who could plays by role) change isPlayer to isNONE MoodTickHandler

/**
 * Player's role on server
 *
 * NONE       - player who is not in game but will play
 * SPECTATOR  - player who is in game but only spectate
 * PLAYER     - plug for roles
 *
 */
public enum Role {
    NONE,
    SPECTATOR,
    PLAYER
}
