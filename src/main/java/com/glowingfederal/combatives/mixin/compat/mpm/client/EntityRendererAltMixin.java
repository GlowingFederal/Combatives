package com.glowingfederal.combatives.mixin.compat.mpm.client;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.EffectivePlayerGeometry;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Neutralizes only MPM+'s temporary targeting position mutation. */
@Pseudo
@Mixin(targets = "noppes.mpm.client.EntityRendererAlt", remap = false)
public abstract class EntityRendererAltMixin {
    @Unique private double combatives$targetPosY;
    @Unique private double combatives$targetPrevPosY;
    @Unique private double combatives$targetLastTickPosY;
    @Unique private boolean combatives$ownsTargetGeometry;

    @Inject(method = "getMouseOver(F)V", at = @At("HEAD"), require = 0)
    private void combatives$captureTargetPosition(float partialTicks, CallbackInfo ci) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        this.combatives$ownsTargetGeometry = this.combatives$isPhysicalLowPose(player);
        if (this.combatives$ownsTargetGeometry) {
            this.combatives$targetPosY = player.posY;
            this.combatives$targetPrevPosY = player.prevPosY;
            this.combatives$targetLastTickPosY = player.lastTickPosY;
        }
    }

    @Inject(method = "getMouseOver(F)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/EntityRenderer;getMouseOver(F)V",
            remap = true
    ), require = 0)
    private void combatives$restoreBeforeVanillaTargeting(float partialTicks, CallbackInfo ci) {
        this.combatives$restoreTargetPosition();
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (this.combatives$ownsTargetGeometry && CombativesConfig.debugCamera && player != null) {
            EffectivePlayerGeometry geometry = ((ICombativesPlayerPose) player).getEffectiveGeometry();
            double rayStartY = player.boundingBox.minY + geometry.eyeAboveMinY;
            Combatives.logger.info("[camera:mpm] targeting adjustment bypassed; pose=" + geometry.pose
                    + " rayStart=" + player.posX + "," + rayStartY + "," + player.posZ
                    + " resolved=" + geometry.width + "x" + geometry.height + " eyeAboveMinY=" + geometry.eyeAboveMinY
                    + " actual=" + player.width + "x" + player.height + " box=" + player.boundingBox
                    + " getEyeHeight=" + player.getEyeHeight() + " renderer=" + this.getClass().getName()
                    + " mpmActive=true mpmTargetingAdjustmentBypassed=true");
        }
    }

    @Inject(method = "getMouseOver(F)V", at = @At("RETURN"), require = 0)
    private void combatives$restoreAfterMpmWrapper(float partialTicks, CallbackInfo ci) {
        this.combatives$restoreTargetPosition();
    }

    @Unique
    private void combatives$restoreTargetPosition() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (this.combatives$ownsTargetGeometry && player != null) {
            player.posY = this.combatives$targetPosY;
            player.prevPosY = this.combatives$targetPrevPosY;
            player.lastTickPosY = this.combatives$targetLastTickPosY;
        }
    }

    @Unique
    private boolean combatives$isPhysicalLowPose(EntityPlayer player) {
        if (!(player instanceof ICombativesPlayerPose)) {
            return false;
        }
        ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
        return pose.getPose() == Pose.SWIMMING
                || pose.isSwimming()
                || pose.isCrawlKeyDown()
                || pose.isActuallySwimming();
    }
}
