package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
public abstract class RendererLivingEntityMixin {
    @Inject(method = "func_147906_a", at = @At("HEAD"), cancellable = true)
    private void combatives$hideCrawlLivingLabel(Entity entity, String name, double x, double y, double z, int maxDistance, CallbackInfo ci) {
        if (entity instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) entity).isCrawlKeyDown()) {
            ci.cancel();
        }
    }
}
