package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.server.InteractionDiagnostics;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin {
    @Inject(method = "processPlayerDigging", at = @At("HEAD"))
    private void combatives$traceDigPacket(C07PacketPlayerDigging packet, CallbackInfo ci) {
        InteractionDiagnostics.logPacket((NetHandlerPlayServer) (Object) this, packet);
    }
}
