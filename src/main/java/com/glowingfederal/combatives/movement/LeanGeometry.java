package com.glowingfederal.combatives.movement;

import com.glowingfederal.combatives.config.AuthoritativeGameplaySettings;
import com.glowingfederal.combatives.interaction.InteractionRay;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/** Common-side lean displacement and wall validation used by camera and gameplay rays. */
public final class LeanGeometry {
    private LeanGeometry() {}

    public static double legalOffset(EntityPlayer player, InteractionRay base, float lean) {
        if (!AuthoritativeGameplaySettings.isLeaningEnabled(player) || lean == 0.0F) return 0.0D;
        double yaw = Math.toRadians(player.rotationYaw);
        // Gameplay lean uses negative=left, positive=right. Minecraft's
        // yaw-relative lateral basis below points left, so convert the semantic
        // value once here for both camera and interaction rays.
        double desired = -AuthoritativeGameplaySettings.getMaxLeanDistance(player) * lean;
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
