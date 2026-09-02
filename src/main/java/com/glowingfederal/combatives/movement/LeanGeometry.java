package com.glowingfederal.combatives.movement;

import com.glowingfederal.combatives.config.AuthoritativeGameplaySettings;
import com.glowingfederal.combatives.interaction.InteractionRay;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/** Common-side lean displacement and wall validation used by camera and gameplay rays. */
public final class LeanGeometry {
    private LeanGeometry() {}

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
