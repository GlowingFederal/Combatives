package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin {
    @Redirect(method = "moveEntityWithHeading", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;isSneaking()Z"))
    private boolean combatives$useActualSneakForTravel(EntityLivingBase entity) {
        if (entity instanceof ICombativesPlayerPose) {
            return ((ICombativesPlayerPose) entity).isActuallySneaking();
        }
        return entity.isSneaking();
    }
}
