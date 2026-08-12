package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.client.InteractionDiagnostics;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C07PacketPlayerDigging.class)
public abstract class C07PacketPlayerDiggingMixin {
    @Inject(method = "<init>(IIIII)V",
            at = @At("RETURN"))
    private void combatives$traceConstructed(int action, int x, int y, int z,
            int face, CallbackInfo ci) {
        InteractionDiagnostics.logDigPacket((C07PacketPlayerDigging) (Object) this);
    }
}
