package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.util.math.MathHelperNew;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow @Final private Minecraft mc;

    private float combatives$eyeHeight;
    private float combatives$previousEyeHeight;
    private float combatives$entityEyeHeight;
    private float combatives$partialTicks;

    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void combatives$capturePartialTicks(float partialTicks, CallbackInfo ci) {
        this.combatives$partialTicks = partialTicks;
    }

    @ModifyVariable(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;prevPosX:D", ordinal = 0), ordinal = 1)
    private float combatives$getInterpolatedEyeHeight(float eyeHeight) {
        Entity entity = this.mc.renderViewEntity;
        if (!(entity instanceof EntityPlayer)) {
            return eyeHeight;
        }
        this.combatives$entityEyeHeight = entity.height == 0.6F ? 0.0F : eyeHeight;
        return MathHelperNew.lerp(this.combatives$partialTicks, this.combatives$previousEyeHeight, this.combatives$eyeHeight);
    }

    @Inject(method = "updateRenderer", at = @At("TAIL"))
    private void combatives$interpolateEyeHeight(CallbackInfo ci) {
        this.combatives$previousEyeHeight = this.combatives$eyeHeight;
        this.combatives$eyeHeight += (this.combatives$entityEyeHeight - this.combatives$eyeHeight) * 0.5F;
    }
}
