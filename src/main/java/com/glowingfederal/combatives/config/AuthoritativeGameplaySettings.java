package com.glowingfederal.combatives.config;

import net.minecraft.entity.player.EntityPlayer;

/** Runtime boundary between server-owned gameplay tuning and local presentation preferences. */
public final class AuthoritativeGameplaySettings {
    private static final Snapshot UNSYNCHRONIZED_CLIENT = new Snapshot(false, 0.0D, false);
    private static volatile Snapshot clientSnapshot;

    private AuthoritativeGameplaySettings() { }

    public static boolean isLeaningEnabled(EntityPlayer player) {
        Snapshot settings = forPlayer(player);
        return settings != null ? settings.leaningEnabled : CombativesConfig.enableLeaning;
    }

    public static double getMaxLeanDistance(EntityPlayer player) {
        Snapshot settings = forPlayer(player);
        return settings != null ? settings.maxLeanDistance : CombativesConfig.maxLeanDistance;
    }

    public static boolean isMpmHitboxScalingEnabled(EntityPlayer player) {
        Snapshot settings = forPlayer(player);
        return settings != null ? settings.mpmHitboxScalingEnabled : CombativesConfig.enableMpmHitboxScaling;
    }

    private static Snapshot forPlayer(EntityPlayer player) {
        if (player == null || player.worldObj == null || !player.worldObj.isRemote) return null;
        Snapshot snapshot = clientSnapshot;
        return snapshot == null ? UNSYNCHRONIZED_CLIENT : snapshot;
    }

    public static Snapshot serverSnapshot() {
        return new Snapshot(CombativesConfig.enableLeaning, CombativesConfig.maxLeanDistance,
                CombativesConfig.enableMpmHitboxScaling);
    }

    public static void installClientSnapshot(Snapshot snapshot) { clientSnapshot = snapshot; }
    public static void clearClientSnapshot() { clientSnapshot = null; }

    public static final class Snapshot {
        public final boolean leaningEnabled;
        public final double maxLeanDistance;
        public final boolean mpmHitboxScalingEnabled;

        public Snapshot(boolean leaningEnabled, double maxLeanDistance, boolean mpmHitboxScalingEnabled) {
            this.leaningEnabled = leaningEnabled;
            this.maxLeanDistance = Math.max(0.0D, Math.min(0.5D, maxLeanDistance));
            this.mpmHitboxScalingEnabled = mpmHitboxScalingEnabled;
        }
    }
}
