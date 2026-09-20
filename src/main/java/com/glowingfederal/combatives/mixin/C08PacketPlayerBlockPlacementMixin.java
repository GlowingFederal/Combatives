package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.client.InteractionDiagnostics;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C08PacketPlayerBlockPlacement.class)
public abstract class C08PacketPlayerBlockPlacementMixin {
    @Inject(method = "<init>(IIIILnet/minecraft/item/ItemStack;FFF)V", at = @At("RETURN"))
    private void combatives$traceBlockPacket(int x, int y, int z, int face, ItemStack stack,
            float hitX, float hitY, float hitZ, CallbackInfo ci) {
        InteractionDiagnostics.logUsePacket((C08PacketPlayerBlockPlacement) (Object) this);
    }
}
