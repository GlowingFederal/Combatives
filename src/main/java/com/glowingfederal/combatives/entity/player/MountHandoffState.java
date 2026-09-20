package com.glowingfederal.combatives.entity.player;

/** Common-side owner of player geometry across a mount transition. */
public enum MountHandoffState {
    PLAYER,
    MOUNTED,
    EXIT_POSITION,
    POSE_CLEARANCE
}
