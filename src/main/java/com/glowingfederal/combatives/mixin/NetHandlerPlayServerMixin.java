package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.server.InteractionDiagnostics;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin {
    /**
     * Vanilla measures digging range from {@code posY + 1.5}. Combatives' client
     * instead targets from the accepted physical eye, which changes with pose and
     * model geometry. Supply the equivalent legacy position so vanilla's existing
     * {@code + 1.5} calculation starts at exactly that same eye without replacing
     * packet handling, reach tolerance, or game-mode checks.
     */
    @Redirect(method = "processPlayerDigging", at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/player/EntityPlayerMP;posY:D"))
    private double combatives$useTargetingEyeForDigDistance(EntityPlayerMP player) {
        ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
        return player.boundingBox.minY + pose.getEffectiveGeometry().eyeAboveMinY - 1.5D;
    }

    @Inject(method = "processPlayerDigging", at = @At("HEAD"))
    private void combatives$traceDigPacket(C07PacketPlayerDigging packet, CallbackInfo ci) {
        InteractionDiagnostics.logPacket((NetHandlerPlayServer) (Object) this, packet);
    }
}
