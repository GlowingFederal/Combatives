package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "setPosition", at = @At("RETURN"))
    private void combatives$resynchronizePostDismountPosition(double x, double y, double z, CallbackInfo ci) {
        if (this instanceof ICombativesPlayerPose) {
            ((ICombativesPlayerPose) this).onPositionSetAfterDismount();
        }
    }

    @Redirect(method = "moveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
    private boolean combatives$useActualSneakForMovement(Entity entity) {
        if (entity instanceof ICombativesPlayerPose) {
            return ((ICombativesPlayerPose) entity).isActuallySneaking();
        }
        return entity.isSneaking();
    }

    @ModifyConstant(method = "handleWaterMovement", constant = @Constant(doubleValue = -0.4000000059604645D))
    private double combatives$adjustSwimmingWaterProbe(double original) {
        return this instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) this).getPose() == Pose.SWIMMING
            ? -0.2500000059604645D
            : original;
    }
}
