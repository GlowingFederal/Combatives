package com.glowingfederal.combatives.movement;

import net.minecraft.util.Vec3;

/** Minecraft-yaw player-local horizontal axes shared by lean camera and gameplay geometry. */
public final class PlayerLocalBasis {
    public final double forwardX;
    public final double forwardZ;
    public final double rightX;
    public final double rightZ;

    private PlayerLocalBasis(double forwardX, double forwardZ, double rightX, double rightZ) {
        this.forwardX = forwardX;
        this.forwardZ = forwardZ;
        this.rightX = rightX;
        this.rightZ = rightZ;
    }

    public static PlayerLocalBasis fromYaw(float yawDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        return new PlayerLocalBasis(-sin, cos, -cos, -sin);
    }

    /** Interpolate the same entity yaw on either side, including the wrap seam. */
    public static float interpolateYaw(float previous, float current, float partialTicks) {
        float delta = (current - previous) % 360.0F;
        if (delta >= 180.0F) delta -= 360.0F;
        if (delta < -180.0F) delta += 360.0F;
        return previous + delta * partialTicks;
    }

    public Vec3 lateralOffset(double semanticLeanDistance) {
        return Vec3.createVectorHelper(this.rightX * semanticLeanDistance, 0.0D,
                this.rightZ * semanticLeanDistance);
    }

    public double projectRight(double worldX, double worldZ) {
        return worldX * this.rightX + worldZ * this.rightZ;
    }
}
