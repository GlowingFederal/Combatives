package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.server.ServerInteractionDiagnostics;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.glowingfederal.combatives.movement.MovementDiagnostics;

/** Adds diagnostics without changing the coordinates owned by interaction packets. */
@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin {
    @Shadow public EntityPlayerMP playerEntity;
    @Unique private boolean combatives$tracePlayerPacket;
    @Unique private String combatives$playerPacketBefore;

    @Inject(method = "processPlayer", at = @At("HEAD"))
    private void combatives$tracePlayerPacketHead(C03PacketPlayer packet, CallbackInfo ci) {
        this.combatives$tracePlayerPacket = MovementDiagnostics.isVerboseEnabled() && packet.func_149466_j()
            && Math.abs(packet.func_149467_d() - this.playerEntity.boundingBox.minY) > 0.05D;
        if (!this.combatives$tracePlayerPacket) return;
        this.combatives$playerPacketBefore = this.combatives$verticalState();
        MovementDiagnostics.verbose(this.playerEntity, "C03 vertical divergence BEFORE server=" + this.combatives$playerPacketBefore
            + " packet={hasPosition=" + packet.func_149466_j() + ",x=" + packet.func_149464_c()
            + ",y=" + packet.func_149467_d() + ",z=" + packet.func_149472_e()
            + ",onGround=" + packet.func_149465_i() + "}");
    }

    @Inject(method = "processPlayer", at = @At("RETURN"))
    private void combatives$tracePlayerPacketReturn(C03PacketPlayer packet, CallbackInfo ci) {
        if (this.combatives$tracePlayerPacket) {
            MovementDiagnostics.verbose(this.playerEntity, "C03 vertical divergence AFTER server=" + this.combatives$verticalState()
                + " previous={" + this.combatives$playerPacketBefore + "}");
        }
        this.combatives$tracePlayerPacket = false;
        this.combatives$playerPacketBefore = null;
    }

    @Unique
    private String combatives$verticalState() {
        return "{posY=" + this.playerEntity.posY + ",bbox.minY=" + this.playerEntity.boundingBox.minY
            + ",motionY=" + this.playerEntity.motionY + ",onGround=" + this.playerEntity.onGround + "}";
    }

    @Inject(method = "processPlayerDigging", at = @At("HEAD"))
    private void combatives$diagnoseDigTarget(C07PacketPlayerDigging packet, CallbackInfo ci) {
        ServerInteractionDiagnostics.logDig(this.playerEntity, packet);
    }

    @Inject(method = "processPlayerBlockPlacement", at = @At("HEAD"))
    private void combatives$diagnoseUseTarget(C08PacketPlayerBlockPlacement packet, CallbackInfo ci) {
        ServerInteractionDiagnostics.logUse(this.playerEntity, packet);
    }
}
