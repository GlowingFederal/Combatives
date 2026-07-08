package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.ICombativesMovementState;
import com.glowingfederal.combatives.movement.MovementController;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

    @Redirect(method = "moveEntityWithHeading", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;moveFlying(FFF)V"))
    private void combatives$shapeMoveFlying(EntityLivingBase entity, float strafe, float forward, float friction) {
        if (!(entity instanceof EntityPlayer) || MovementController.shouldBypass((EntityPlayer) entity)) {
            entity.moveFlying(strafe, forward, friction);
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        double currentX = player.motionX;
        double currentZ = player.motionZ;
        entity.moveFlying(strafe, forward, friction);
        MovementController.MovementResult result = MovementController.shape(player, strafe, forward, player.rotationYaw, currentX, currentZ, player.motionX, player.motionZ);
        player.motionX = result.motionX;
        player.motionZ = result.motionZ;
        if (player instanceof ICombativesMovementState) {
            ((ICombativesMovementState) player).setCombativesMovementSnapshot(result.snapshot);
        }
    }
}
