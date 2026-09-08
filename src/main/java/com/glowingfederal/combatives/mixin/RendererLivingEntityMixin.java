package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.client.model.ModelBase;
import com.glowingfederal.combatives.client.model.ICombativesLeanModel;
import com.glowingfederal.combatives.movement.ICombativesLocomotion;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
public abstract class RendererLivingEntityMixin {
    @Shadow protected ModelBase mainModel;
    @Shadow protected ModelBase renderPassModel;

    @Unique private static boolean combatives$leaning(EntityLivingBase entity) {
        return entity instanceof EntityPlayer && entity instanceof ICombativesLocomotion
                && !entity.isRiding() && ((ICombativesLocomotion) entity).getLean() != 0.0F;
    }

    @Redirect(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/EntityLivingBase;prevRenderYawOffset:F"))
    private float combatives$previousBodyYaw(EntityLivingBase entity) {
        return combatives$leaning(entity) ? entity.prevRotationYaw : entity.prevRenderYawOffset;
    }

    @Redirect(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/EntityLivingBase;renderYawOffset:F"))
    private float combatives$bodyYaw(EntityLivingBase entity) {
        return combatives$leaning(entity) ? entity.rotationYaw : entity.renderYawOffset;
    }

    @Redirect(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/EntityLivingBase;prevRotationYawHead:F"))
    private float combatives$previousHeadYaw(EntityLivingBase entity) {
        return combatives$leaning(entity) ? entity.prevRotationYaw : entity.prevRotationYawHead;
    }

    @Redirect(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/EntityLivingBase;rotationYawHead:F"))
    private float combatives$headYaw(EntityLivingBase entity) {
        return combatives$leaning(entity) ? entity.rotationYaw : entity.rotationYawHead;
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("RETURN"))
    private void combatives$finishLean(EntityLivingBase entity, double x, double y, double z,
            float yaw, float partialTicks, CallbackInfo ci) {
        if (mainModel instanceof ICombativesLeanModel) ((ICombativesLeanModel) mainModel).combatives$restoreVisualLean();
        if (renderPassModel instanceof ICombativesLeanModel) ((ICombativesLeanModel) renderPassModel).combatives$restoreVisualLean();
    }

    @Inject(method = "passSpecialRender", at = @At("HEAD"), cancellable = true)
    private void combatives$hideCrawlSpecials(EntityLivingBase entity, double x, double y, double z, CallbackInfo ci) {
        this.combatives$debugAndCancelCrawlNameplate("RendererLivingEntity#passSpecialRender", entity, x * x + y * y + z * z, ci);
    }

    @Inject(method = "func_96449_a(Lnet/minecraft/entity/EntityLivingBase;DDDLjava/lang/String;FD)V", at = @At("HEAD"), cancellable = true)
    private void combatives$hideCrawlLivingLabel(EntityLivingBase entity, double x, double y, double z, String name, float scale, double distance, CallbackInfo ci) {
        this.combatives$debugAndCancelCrawlNameplate("RendererLivingEntity#func_96449_a", entity, distance, ci);
    }

    private void combatives$debugAndCancelCrawlNameplate(String hook, EntityLivingBase entity, double distance, CallbackInfo ci) {
        if (!(entity instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) entity;
        boolean crawl = entity instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) entity).isCrawlKeyDown();
        if (crawl) {
            ci.cancel();
        }
    }
}
