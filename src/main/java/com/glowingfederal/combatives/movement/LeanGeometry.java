package com.glowingfederal.combatives.movement;

import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.interaction.InteractionRay;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/** Common-side lean displacement and wall validation used by camera and gameplay rays. */
public final class LeanGeometry {
    private LeanGeometry() {}

    public static double legalOffset(EntityPlayer player, InteractionRay base, float lean) {
        if (!CombativesConfig.enableLeaning || lean == 0.0F) return 0.0D;
        double yaw = Math.toRadians(player.rotationYaw);
        double desired = CombativesConfig.maxLeanDistance * lean;
        double dx = Math.cos(yaw) * desired;
        double dz = Math.sin(yaw) * desired;
        Vec3 end = base.origin.addVector(dx, 0.0D, dz);
        MovingObjectPosition hit = player.worldObj.rayTraceBlocks(base.origin, end, false);
        if (hit == null || hit.hitVec == null) return desired;
        double available = base.origin.distanceTo(hit.hitVec) - 0.05D;
        if (available <= 0.0D) return 0.0D;
        return Math.copySign(Math.min(Math.abs(desired), available), desired);
    }
}
