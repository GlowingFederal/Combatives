package com.fuzs.aquaacrobatics.mixins.early.minecraft.client;

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

import com.fuzs.aquaacrobatics.util.math.MathHelperNew;

@SuppressWarnings("unused")
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Shadow
    @Final
    private Minecraft mc;

    private float eyeHeight;
    private float previousEyeHeight;
    private float entityEyeHeight;
    private float partialTicks;

    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void orientCamera(float partialTicks, CallbackInfo callbackInfo) {

        // field for passing on partialTicks, workaround as @ModifyVariable is unable to handle method arguments in
        // Mixin <0.8
        this.partialTicks = partialTicks;
    }

    @ModifyVariable(
        method = "orientCamera",
        at = @At(value = "FIELD", target = "net/minecraft/entity/EntityLivingBase.prevPosX:D", ordinal = 0),
        ordinal = 1)
    public float getEyeHeight(float eyeHeight) {
        Entity entity = this.mc.renderViewEntity;
        // Do not apply eye height patch if the camera is not a player, or if Random Patches is installed
        if (!(entity instanceof EntityPlayer)) {
            return eyeHeight;
        }
        // Fix the eye height
        this.entityEyeHeight = entity.height == 0.6F ? 0F : eyeHeight;
        return MathHelperNew.lerp(this.partialTicks, this.previousEyeHeight, this.eyeHeight);
    }

    @Inject(method = "updateRenderer", at = @At("TAIL"))
    public void updateRenderer(CallbackInfo callbackInfo) {

        this.interpolateHeight();
    }

    private void interpolateHeight() {

        this.previousEyeHeight = this.eyeHeight;
        this.eyeHeight += (this.entityEyeHeight - this.eyeHeight) * 0.5F;
    }
}
