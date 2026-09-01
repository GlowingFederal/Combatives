package com.glowingfederal.combatives.movement;

import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

/** Deterministic, server-owned slide lifecycle and horizontal momentum. */
public final class SlidePhysics {
    private SlidePhysics() {}

    public static boolean canEnter(EntityPlayer player) {
        double speed = horizontalSpeed(player);
        return CombativesConfig.enableSliding && player.onGround && player.isSprinting()
            && speed >= CombativesConfig.slideMinimumEntrySpeed && !player.isInWater()
            && !MovementController.shouldBypassUnsafe(player);
    }

    public static boolean begin(EntityPlayer player) {
        if (!(player instanceof ICombativesLocomotion) || !(player instanceof ICombativesPlayerPose) || !canEnter(player)) return false;
        ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
        if (!pose.isPoseClear(Pose.SWIMMING)) return false;
        ICombativesLocomotion movement = (ICombativesLocomotion) player;
        movement.setLocomotionState(LocomotionState.SLIDING);
        movement.setSlideTicks(0);
        player.setSprinting(false);
        return true;
    }

    public static boolean tick(EntityPlayer player) {
        if (!(player instanceof ICombativesLocomotion)) return false;
        ICombativesLocomotion movement = (ICombativesLocomotion) player;
        if (movement.getLocomotionState() != LocomotionState.SLIDING) return false;
        int ticks = movement.getSlideTicks() + 1;
        movement.setSlideTicks(ticks);
        double speed = horizontalSpeed(player);
        boolean incompatible = !player.onGround || player.isInWater() || player.isCollidedHorizontally || player.hurtTime > 0 || MovementController.shouldBypassUnsafe(player)
            || ticks >= CombativesConfig.slideMaximumTicks || speed < CombativesConfig.slideExitSpeed;
        if (incompatible) return false;
        double next = Math.max(0.0D, speed - CombativesConfig.slideDeceleration);
        if (speed > 1.0E-6D) {
            player.motionX *= next / speed;
            player.motionZ *= next / speed;
        }
        return true;
    }

    public static double horizontalSpeed(EntityPlayer player) {
        return Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
    }

    public static void steer(EntityPlayer player, float strafe, float forward) {
        double speed = horizontalSpeed(player);
        double inputLength = Math.sqrt(strafe * strafe + forward * forward);
        if (speed < 1.0E-6D || inputLength < 1.0E-4D) return;
        double sin = MathHelper.sin(player.rotationYaw * (float) Math.PI / 180.0F);
        double cos = MathHelper.cos(player.rotationYaw * (float) Math.PI / 180.0F);
        double wishX = (strafe / inputLength) * cos - (forward / inputLength) * sin;
        double wishZ = (forward / inputLength) * cos + (strafe / inputLength) * sin;
        double influence = CombativesConfig.slideSteeringInfluence;
        double x = player.motionX / speed * (1.0D - influence) + wishX * influence;
        double z = player.motionZ / speed * (1.0D - influence) + wishZ * influence;
        double length = Math.sqrt(x * x + z * z);
        if (length > 1.0E-6D) {
            player.motionX = x / length * speed;
            player.motionZ = z / length * speed;
        }
    }
}
