package com.glowingfederal.combatives.interaction;

import com.glowingfederal.combatives.entity.player.EffectivePlayerGeometry;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import com.glowingfederal.combatives.movement.ICombativesLocomotion;
import com.glowingfederal.combatives.movement.LeanGeometry;
import com.glowingfederal.combatives.movement.PlayerLocalBasis;

/**
 * The common-side definition of gameplay aim.  The anchor is always measured
 * from the accepted physical AABB floor; legacy entity eye/offset fields and
 * renderer translations are intentionally not inputs.
 */
public final class InteractionRay {
    public final Vec3 origin;
    public final Vec3 direction;
    public final int geometryRevision;

    private InteractionRay(Vec3 origin, Vec3 direction, int geometryRevision) {
        this.origin = origin;
        this.direction = direction;
        this.geometryRevision = geometryRevision;
    }

    /** Tick-authoritative ray used by server interaction validation. */
    public static InteractionRay authoritative(EntityPlayer player) {
        return applyLean(player, create(player, player.posX, player.boundingBox.minY, player.posZ,
                player.rotationYaw, player.rotationPitch), player.rotationYaw);
    }

    /** Same geometry semantics with position/orientation interpolation for rendering. */
    public static InteractionRay interpolated(EntityPlayer player, float partialTicks) {
        double x = interpolate(player.prevPosX, player.posX, partialTicks);
        double z = interpolate(player.prevPosZ, player.posZ, partialTicks);
        double positionY = interpolate(player.prevPosY, player.posY, partialTicks);
        double floorY = positionY + player.boundingBox.minY - player.posY;
        float yaw = PlayerLocalBasis.interpolateYaw(player.prevRotationYaw, player.rotationYaw, partialTicks);
        float pitch = player.prevRotationPitch
                + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
        return applyLean(player, create(player, x, floorY, z, yaw, pitch), yaw);
    }

    private static InteractionRay applyLean(EntityPlayer player, InteractionRay base, float yaw) {
        if (!(player instanceof ICombativesLocomotion)) return base;
        float lean = ((ICombativesLocomotion) player).getLean();
        Vec3 offset = LeanGeometry.legalOffset(player, base, lean, yaw);
        if (offset.xCoord == 0.0D && offset.zCoord == 0.0D) return base;
        return new InteractionRay(base.origin.addVector(offset.xCoord, 0.0D, offset.zCoord),
                base.direction, base.geometryRevision);
    }

    private static InteractionRay create(EntityPlayer player, double x, double floorY,
            double z, float yaw, float pitch) {
        ICombativesPlayerPose state = (ICombativesPlayerPose) player;
        EffectivePlayerGeometry geometry = state.getEffectiveGeometry();
        PlayerLocalBasis basis = PlayerLocalBasis.fromYaw(yaw);
        double pitchRadians = Math.toRadians(pitch);
        double horizontal = Math.cos(pitchRadians);
        Vec3 direction = Vec3.createVectorHelper(basis.forwardX * horizontal,
                -Math.sin(pitchRadians), basis.forwardZ * horizontal);
        return new InteractionRay(Vec3.createVectorHelper(x, floorY + geometry.eyeAboveMinY, z),
                direction, state.getGeometryRevision());
    }

    public Vec3 end(double reach) {
        return this.origin.addVector(this.direction.xCoord * reach,
                this.direction.yCoord * reach, this.direction.zCoord * reach);
    }

    public MovingObjectPosition traceBlocks(EntityLivingBase player, double reach) {
        return player.worldObj.rayTraceBlocks(this.origin, end(reach), false);
    }

    private static double interpolate(double previous, double current, float partialTicks) {
        return previous + (current - previous) * partialTicks;
    }

}
