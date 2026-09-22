package com.glowingfederal.combatives.movement;

import com.glowingfederal.combatives.config.AuthoritativeGameplaySettings;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.interaction.InteractionRay;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/** Common-side lean displacement and wall validation used by camera and gameplay rays. */
public final class LeanGeometry {
    private LeanGeometry() {}

    /** Server-resolved (or locally predicted) semantic amount consumed by render and combat poses. */
    public static float acceptedLean(EntityPlayer player) {
        if (!(player instanceof ICombativesLocomotion) || !(player instanceof ICombativesPlayerPose)) return 0.0F;
        ICombativesLocomotion locomotion = (ICombativesLocomotion) player;
        ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
        if (!AuthoritativeGameplaySettings.isLeaningEnabled(player)
                || player.isRiding() || player.isPlayerSleeping() || player.isInWater()
                || pose.isSwimming() || pose.getPose() == Pose.SWIMMING
                || locomotion.getLocomotionState() != LocomotionState.NORMAL || !pose.canCrawl()) return 0.0F;
        return locomotion.getAcceptedLean();
    }

    /** Resolve a requested lean against current authoritative state and wall clearance. */
    public static float calculateAcceptedLean(EntityPlayer player) {
        if (!(player instanceof ICombativesLocomotion) || !(player instanceof ICombativesPlayerPose)) return 0.0F;
        ICombativesLocomotion locomotion = (ICombativesLocomotion) player;
        ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
        float requested = locomotion.getLean();
        if (requested == 0.0F || !AuthoritativeGameplaySettings.isLeaningEnabled(player)
                || player.isRiding() || player.isPlayerSleeping() || player.isInWater()
                || pose.isSwimming() || pose.getPose() == Pose.SWIMMING
                || locomotion.getLocomotionState() != LocomotionState.NORMAL || !pose.canCrawl()) return 0.0F;
        double max = AuthoritativeGameplaySettings.getMaxLeanDistance(player);
        if (max <= 0.0D) return 0.0F;
        InteractionRay base = InteractionRay.authoritativeBase(player);
        Vec3 offset = legalOffset(player, base, requested, player.rotationYaw);
        return (float) (PlayerLocalBasis.fromYaw(player.rotationYaw).projectRight(
                offset.xCoord, offset.zCoord) / max);
    }

    public static Vec3 acceptedOffset(EntityPlayer player, float yaw) {
        double max = AuthoritativeGameplaySettings.getMaxLeanDistance(player);
        return PlayerLocalBasis.fromYaw(yaw).lateralOffset(max * acceptedLean(player));
    }

    public static Vec3 legalOffset(EntityPlayer player, InteractionRay base, float lean, float yaw) {
        if (!AuthoritativeGameplaySettings.isLeaningEnabled(player) || lean == 0.0F) {
            return Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
        }
        double desiredDistance = AuthoritativeGameplaySettings.getMaxLeanDistance(player) * lean;
        PlayerLocalBasis basis = PlayerLocalBasis.fromYaw(yaw);
        Vec3 desiredOffset = basis.lateralOffset(desiredDistance);
        Vec3 end = base.origin.addVector(desiredOffset.xCoord, 0.0D, desiredOffset.zCoord);
        MovingObjectPosition hit = player.worldObj.rayTraceBlocks(base.origin, end, false);
        if (hit == null || hit.hitVec == null) return desiredOffset;
        double available = base.origin.distanceTo(hit.hitVec) - 0.05D;
        if (available <= 0.0D) return Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
        double acceptedDistance = Math.copySign(Math.min(Math.abs(desiredDistance), available), desiredDistance);
        return basis.lateralOffset(acceptedDistance);
    }
}
