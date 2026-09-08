package com.glowingfederal.combatives.compat.flans;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.interaction.InteractionRay;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Only the handheld constructor: mounted/explicit-origin shots retain their own origin. */
@Pseudo
@Mixin(targets = "com.flansmod.common.guns.EntityBullet", remap = false)
public abstract class FlansBulletMixin {
    @Redirect(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;FFLcom/flansmod/common/guns/BulletType;FZLcom/flansmod/common/types/InfoType;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Vec3;createVectorHelper(DDD)Lnet/minecraft/util/Vec3;", remap = true))
    private static Vec3 combatives$handheldOrigin(double x, double y, double z, World world,
            EntityLivingBase shooter, float spread, float damage, @Coerce Object bulletType,
            float speed, boolean shotgun, @Coerce Object shotFrom) {
        if (shooter instanceof EntityPlayer && shooter instanceof ICombativesPlayerPose && !shooter.isRiding()) {
            return InteractionRay.authoritative((EntityPlayer) shooter).origin;
        }
        return Vec3.createVectorHelper(x, y, z);
    }
}
