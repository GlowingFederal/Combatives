package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.server.InteractionDiagnostics;
import net.minecraft.server.management.ItemInWorldManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemInWorldManager.class)
public abstract class ItemInWorldManagerMixin {
    @Inject(method = "onBlockClicked", at = @At("HEAD"))
    private void combatives$traceStart(int x, int y, int z, int face, CallbackInfo ci) {
        InteractionDiagnostics.logDamage((ItemInWorldManager) (Object) this, "onBlockClicked", x, y, z, face, null);
    }

    @Inject(method = "blockRemoving", at = @At("HEAD"))
    private void combatives$traceContinue(int x, int y, int z, CallbackInfo ci) {
        InteractionDiagnostics.logDamage((ItemInWorldManager) (Object) this, "blockRemoving", x, y, z, -1, null);
    }

    @Inject(method = "tryHarvestBlock", at = @At("RETURN"))
    private void combatives$traceHarvest(int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        InteractionDiagnostics.logDamage((ItemInWorldManager) (Object) this, "tryHarvestBlock", x, y, z, -1, cir.getReturnValue());
    }
}
