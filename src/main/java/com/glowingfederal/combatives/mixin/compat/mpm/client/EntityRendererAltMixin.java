package com.glowingfederal.combatives.mixin.compat.mpm.client;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Neutralizes MPM+'s temporary POV translation only while vanilla targeting runs. */
@Pseudo
@Mixin(targets = "noppes.mpm.client.EntityRendererAlt", remap = false)
public abstract class EntityRendererAltMixin {
    @Unique private double combatives$originalPosY;
    @Unique private double combatives$originalPrevPosY;
    @Unique private double combatives$originalLastTickPosY;
    @Unique private double combatives$mpmPosY;
    @Unique private double combatives$mpmPrevPosY;
    @Unique private double combatives$mpmLastTickPosY;
    @Unique private boolean combatives$ownsTargetGeometry;
    @Unique private boolean combatives$targetCallActive;
    @Unique private boolean combatives$logTargetPass;

    /* MPM+ 4.2 is reobfuscated: its source getMouseOver name is func_78473_a in the shipped class. */
    @Inject(method = "func_78473_a(F)V", at = @At("HEAD"), require = 0, remap = false)
    private void combatives$captureOriginalPosition(float partialTicks, CallbackInfo ci) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        this.combatives$ownsTargetGeometry = this.combatives$isPhysicalLowPose(player);
        this.combatives$targetCallActive = false;
        this.combatives$logTargetPass = this.combatives$ownsTargetGeometry
                && CombativesConfig.debugCamera && player.ticksExisted % 20 == 0;
        if (this.combatives$ownsTargetGeometry) {
            this.combatives$originalPosY = player.posY;
            this.combatives$originalPrevPosY = player.prevPosY;
            this.combatives$originalLastTickPosY = player.lastTickPosY;
        }
    }

    @Inject(method = "func_78473_a(F)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/EntityRenderer;func_78473_a(F)V",
            remap = false
    ), require = 0, remap = false)
    private void combatives$restoreWorldPositionForVanilla(float partialTicks, CallbackInfo ci) {
        if (!this.combatives$ownsTargetGeometry) {
            return;
        }
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) {
            return;
        }
        this.combatives$mpmPosY = player.posY;
        this.combatives$mpmPrevPosY = player.prevPosY;
        this.combatives$mpmLastTickPosY = player.lastTickPosY;
        player.posY = this.combatives$originalPosY;
        player.prevPosY = this.combatives$originalPrevPosY;
        player.lastTickPosY = this.combatives$originalLastTickPosY;
        this.combatives$targetCallActive = true;
        if (this.combatives$logTargetPass && Combatives.logger != null) {
            Combatives.logger.info("MPM TARGET COMPAT ACTIVE: before MPM=[{},{},{}] MPM-mutated=[{},{},{}] Combatives-restored=[{},{},{}] mutationY={}",
                    this.combatives$originalPosY, this.combatives$originalPrevPosY, this.combatives$originalLastTickPosY,
                    this.combatives$mpmPosY, this.combatives$mpmPrevPosY, this.combatives$mpmLastTickPosY,
                    player.posY, player.prevPosY, player.lastTickPosY,
                    this.combatives$mpmPosY - this.combatives$originalPosY);
        }
    }

    @Inject(method = "func_78473_a(F)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/EntityRenderer;func_78473_a(F)V",
            shift = At.Shift.AFTER,
            remap = false
    ), require = 0, remap = false)
    private void combatives$restoreMpmMutationForCleanup(float partialTicks, CallbackInfo ci) {
        if (!this.combatives$targetCallActive) {
            return;
        }
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            player.posY = this.combatives$mpmPosY;
            player.prevPosY = this.combatives$mpmPrevPosY;
            player.lastTickPosY = this.combatives$mpmLastTickPosY;
            if (this.combatives$logTargetPass && Combatives.logger != null) {
                Combatives.logger.info("MPM target returned: post-target MPM values=[{},{},{}]; returning ownership to MPM cleanup",
                        player.posY, player.prevPosY, player.lastTickPosY);
            }
        }
        this.combatives$targetCallActive = false;
    }

    @Unique
    private boolean combatives$isPhysicalLowPose(EntityPlayer player) {
        if (!(player instanceof ICombativesPlayerPose)) {
            return false;
        }
        ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
        return pose.getPose() == Pose.SWIMMING || pose.isSwimming()
                || pose.isCrawlKeyDown() || pose.isActuallySwimming();
    }
}
