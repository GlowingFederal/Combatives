package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.client.camera.CameraCompatibilityManager;
import com.glowingfederal.combatives.client.camera.CameraController;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.util.math.MathHelperNew;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow @Final private Minecraft mc;

    private float combatives$eyeHeight;
    private float combatives$previousEyeHeight;
    private float combatives$entityEyeHeight;
    private float combatives$partialTicks;
    private int combatives$lastRenderCallTick = Integer.MIN_VALUE;
    private int combatives$renderCallCountThisTick;

    @Inject(method = "updateCameraAndRender", at = @At("HEAD"))
    private void combatives$logCameraRenderEntry(float partialTicks, CallbackInfo ci) {
        int currentTick = this.mc.thePlayer == null ? -1 : this.mc.thePlayer.ticksExisted;
        if (this.combatives$lastRenderCallTick != currentTick) {
            this.combatives$lastRenderCallTick = currentTick;
            this.combatives$renderCallCountThisTick = 0;
        }
        this.combatives$renderCallCountThisTick++;

        if (Combatives.logger == null || !CombativesConfig.debugCamera) {
            return;
        }

        long nanoTime = System.nanoTime();
        long currentTime = System.currentTimeMillis();
        boolean displayActive = Display.isActive();

        Combatives.logger.info(
            "Combatives updateCameraAndRender: tick={}, partialTicks={}, nanoTime={}, currentTime={}, renderCallCountThisTick={}, inGameHasFocus={}, displayActive={}",
            currentTick,
            partialTicks,
            nanoTime,
            currentTime,
            this.combatives$renderCallCountThisTick,
            this.mc.inGameHasFocus,
            displayActive
        );

        if (this.combatives$renderCallCountThisTick > 1) {
            Combatives.logger.warn(
                "Combatives repeated updateCameraAndRender in one client tick: tick={}, partialTicks={}, nanoTime={}, currentTime={}, renderCallCountThisTick={}, inGameHasFocus={}, displayActive={}, stack={}",
                currentTick,
                partialTicks,
                nanoTime,
                currentTime,
                this.combatives$renderCallCountThisTick,
                this.mc.inGameHasFocus,
                displayActive,
                combatives$partialStack()
            );
        }
    }

    @Inject(method = "updateCameraAndRender", at = @At("TAIL"))
    private void combatives$sampleCameraAfterVanillaInput(float partialTicks, CallbackInfo ci) {
        if (this.mc.thePlayer instanceof EntityPlayerSP) {
            CameraController.INSTANCE.update(this.mc, (EntityPlayerSP) this.mc.thePlayer, partialTicks);
        } else {
            CameraController.INSTANCE.reset();
        }
    }

    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void combatives$capturePartialTicks(float partialTicks, CallbackInfo ci) {
        this.combatives$partialTicks = partialTicks;
    }

    @Inject(method = "orientCamera", at = @At("TAIL"))
    private void combatives$applyCameraTransforms(float partialTicks, CallbackInfo ci) {
        CameraController.INSTANCE.applyTransforms(partialTicks);
    }

    @Inject(
            method = "renderHand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemInFirstPerson(F)V"
            )
    )
    private void combatives$applyHandBobbing(float partialTicks, int pass, CallbackInfo ci) {
        CameraController.INSTANCE.applyHandTransforms(partialTicks);
    }

    @Inject(method = "setupViewBobbing", at = @At("HEAD"), cancellable = true)
    private void combatives$cancelVanillaViewBobbing(float partialTicks, CallbackInfo ci) {
        if (CameraCompatibilityManager.shouldCancelVanillaViewBobbing()) {
            ci.cancel();
        }
    }

    @Inject(method = "getFOVModifier", at = @At("RETURN"), cancellable = true)
    private void combatives$applyMovementFov(float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Float> cir) {
        if (CameraCompatibilityManager.ownsDynamicFov()) {
            cir.setReturnValue(cir.getReturnValue() * (1.0F + CameraController.INSTANCE.getFovModifier()));
        }
    }

    @ModifyVariable(
            method = "orientCamera",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/EntityLivingBase;prevPosX:D",
                    ordinal = 0
            ),
            ordinal = 1
    )
    private float combatives$getInterpolatedEyeHeight(float eyeHeight) {
        Entity entity = this.mc.renderViewEntity;

        if (!(entity instanceof EntityPlayer)) {
            return eyeHeight;
        }

        this.combatives$entityEyeHeight = ((EntityPlayer) entity).getEyeHeight();

        return MathHelperNew.lerp(
                this.combatives$partialTicks,
                this.combatives$previousEyeHeight,
                this.combatives$eyeHeight
        );
    }

    @Inject(method = "updateRenderer", at = @At("TAIL"))
    private void combatives$interpolateEyeHeight(CallbackInfo ci) {
        this.combatives$previousEyeHeight = this.combatives$eyeHeight;
        this.combatives$eyeHeight += (this.combatives$entityEyeHeight - this.combatives$eyeHeight) * 0.5F;
    }

    private static String combatives$partialStack() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StringBuilder builder = new StringBuilder();
        int appended = 0;
        for (int i = 3; i < stack.length && appended < 10; i++) {
            if (builder.length() > 0) {
                builder.append(" <- ");
            }
            builder.append(stack[i].getClassName()).append('#').append(stack[i].getMethodName()).append(':').append(stack[i].getLineNumber());
            appended++;
        }
        return builder.toString();
    }
}
