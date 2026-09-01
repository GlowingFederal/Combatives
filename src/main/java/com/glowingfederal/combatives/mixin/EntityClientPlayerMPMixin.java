package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.client.ICombativesClientPlayerSwimming;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.ICombativesLocomotion;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import net.minecraft.client.entity.EntityClientPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityClientPlayerMP.class)
public abstract class EntityClientPlayerMPMixin {
    @Inject(method = "sendMotionUpdates", at = @At("HEAD"))
    private void combatives$traceOutgoingMovement(CallbackInfo ci) {
        EntityClientPlayerMP player = (EntityClientPlayerMP) (Object) this;
        if (!MovementDiagnostics.isVerboseEnabled() || !(player instanceof ICombativesPlayerPose)) return;
        ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
        if (player.onGround || player.motionY >= -0.05D) return;
        ICombativesLocomotion locomotion = player instanceof ICombativesLocomotion ? (ICombativesLocomotion) player : null;
        MovementDiagnostics.verbose(player, "client before movement packet posY=" + player.posY
            + " bbox.minY/packetY=" + player.boundingBox.minY + " motionY=" + player.motionY
            + " onGround=" + player.onGround + " pose=" + pose.getPose()
            + " crawlRequested=" + pose.isCrawlKeyDown() + " locomotion="
            + (locomotion == null ? "unavailable" : locomotion.getLocomotionState()));
    }

    @Redirect(method = "sendMotionUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityClientPlayerMP;isSneaking()Z"))
    private boolean combatives$sendActualSneaking(EntityClientPlayerMP player) {
        return player instanceof ICombativesClientPlayerSwimming
            ? ((ICombativesClientPlayerSwimming) player).isActuallySneaking()
            : player.isSneaking();
    }
}
