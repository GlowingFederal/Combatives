package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

/** Aligns the legacy standing-player ray origin with the legacy render camera. */
public final class TargetingOriginResolver {
    private TargetingOriginResolver() { }

    public static Vec3 alignWithRenderedCamera(EntityLivingBase entity, float partialTicks, Vec3 vanillaOrigin) {
        if (!(entity instanceof EntityPlayer) || !(entity instanceof ICombativesPlayerPose)) {
            return vanillaOrigin;
        }

        /* 1.7.10 EntityLivingBase#getPosition applies its own legacy 0.12 eye
         * bias after interpolation. It is independent of the virtual
         * getEyeHeight() result: runtime traces show a zero standing eye still
         * returning interpolation - 0.12, and a -1.34 swimming eye returning
         * interpolation - 1.46. Reconstruct the intended physical origin from
         * the inputs instead of trying to cancel that constant. The cached
         * Combatives eye supplies the pose component, while temporary MPM
         * translations remain present in both interpolation endpoints. */
        double x = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
        double y = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks
                + (double) entity.getEyeHeight();
        double z = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
        return Vec3.createVectorHelper(x, y, z);
    }
}
