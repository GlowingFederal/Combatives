package com.glowingfederal.combatives.compat.flans;

import com.glowingfederal.combatives.client.model.ICombativesLeanModel;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Closes the lean lifecycle for Flan's ModelCustomArmour, whose render override bypasses ModelBiped.render. */
@Pseudo
@Mixin(targets = "com.flansmod.client.model.ModelCustomArmour", remap = false)
public abstract class FlansCustomArmourMixin {
    @Inject(method = "render(Lnet/minecraft/entity/Entity;FFFFFF)V", at = @At("RETURN"), remap = false)
    private void combatives$restoreLeanAfterCustomArmourRender(Entity entity, float limbSwing,
            float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
            float scaleFactor, CallbackInfo ci) {
        ((ICombativesLeanModel) this).combatives$restoreVisualLean();
    }
}
