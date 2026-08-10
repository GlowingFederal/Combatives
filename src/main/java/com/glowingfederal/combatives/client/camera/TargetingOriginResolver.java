package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

/** Aligns the legacy standing-player ray origin with the legacy render camera. */
public final class TargetingOriginResolver {
    private TargetingOriginResolver() { }

    public static Vec3 alignWithRenderedCamera(EntityLivingBase entity, Vec3 vanillaOrigin) {
        if (!(entity instanceof EntityPlayer) || !(entity instanceof ICombativesPlayerPose)) {
            return vanillaOrigin;
        }

        ICombativesPlayerPose state = (ICombativesPlayerPose) entity;
        if (state.getPose() != Pose.STANDING || state.isSwimming()
                || state.isCrawlKeyDown() || state.isActuallySwimming()) {
            return vanillaOrigin;
        }

        /* EntityPlayer overrides EntityLivingBase#getPosition and adds
         * getEyeHeight(). Combatives' cached value is position-relative and can
         * legitimately be negative (for example while ySize is settling), but
         * orientCamera's standing path uses the interpolated legacy position.
         * Remove that method-specific addition rather than compensating for an
         * observed constant. Temporary MPM translations remain in all three
         * interpolated position components. */
        return vanillaOrigin.addVector(0.0D, -(double) entity.getEyeHeight(), 0.0D);
    }
}
