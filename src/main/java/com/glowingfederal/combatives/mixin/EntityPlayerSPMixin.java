package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.client.ICombativesClientPlayerSwimming;
import com.glowingfederal.combatives.client.MovementInputStorage;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovementInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public abstract class EntityPlayerSPMixin implements ICombativesClientPlayerSwimming {
    @Shadow protected Minecraft mc;
    @Shadow public MovementInput movementInput;
    @Shadow protected int sprintToggleTimer;
    @Shadow public abstract boolean isSprinting();
    @Shadow public abstract void setSprinting(boolean sprinting);
    @Shadow public abstract boolean isInWater();
    @Shadow public abstract boolean isPotionActive(Potion potion);
    @Shadow public abstract boolean isUsingItem();
    @Shadow public abstract boolean isRiding();

    private final MovementInputStorage combatives$movementStorage = new MovementInputStorage();
    private boolean combatives$isCrouching;

    @Inject(method = "isSneaking", at = @At("HEAD"), cancellable = true)
    private void combatives$isSneaking(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.combatives$isCrouching);
    }

    @Override
    public boolean isActuallySneaking() {
        return this.movementInput != null && this.movementInput.sneak;
    }

    @Override
    public boolean isForcedDown() {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        ICombativesPlayerPose pose = (ICombativesPlayerPose) self;
        return pose.isResizingAllowed() && !self.capabilities.isFlying ? this.combatives$isCrouching || pose.isVisuallySwimming() : this.isActuallySneaking();
    }

    @Override
    public boolean isUsingSwimmingAnimation() {
        return this.movementInput != null && this.isUsingSwimmingAnimation(this.movementInput.moveForward, this.movementInput.moveStrafe);
    }

    @Override
    public boolean isUsingSwimmingAnimation(float moveForward, float moveStrafe) {
        if (this.canSwimClient()) {
            return this.isMovingForward(moveForward, moveStrafe);
        }
        return moveForward >= 0.8F;
    }

    @Override
    public boolean canSwimClient() {
        return ((ICombativesPlayerPose) (Object) this).getEyesInWaterPlayer();
    }

    @Override
    public boolean isMovingForward(float moveForward, float moveStrafe) {
        return moveForward > 1.0E-5F;
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    private void combatives$onLivingUpdateHead(CallbackInfo ci) {
        if (this.movementInput == null) {
            return;
        }
        this.combatives$updateSprintToggleTimer();
        this.combatives$movementStorage.copyFrom(this.movementInput);
        this.combatives$movementStorage.isSprinting = this.isSprinting();
        this.combatives$movementStorage.isFlying = ((EntityPlayerSP) (Object) this).capabilities.isFlying;
    }

    private void combatives$updateSprintToggleTimer() {
        if (this.movementInput.sneak) {
            this.sprintToggleTimer = 0;
        }
        this.combatives$movementStorage.sprintToggleTimer = this.sprintToggleTimer;
        if (this.combatives$movementStorage.sprintToggleTimer > 0) {
            --this.combatives$movementStorage.sprintToggleTimer;
        }
        if (this.isUsingItem() && !this.isRiding()) {
            this.combatives$movementStorage.sprintToggleTimer = 0;
        }
    }

    @Inject(method = "onLivingUpdate", at = @At("TAIL"))
    private void combatives$onLivingUpdateTail(CallbackInfo ci) {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        ICombativesPlayerPose pose = (ICombativesPlayerPose) self;
        if (this.movementInput == null) {
            return;
        }

        this.combatives$updatePlayerMoveState();
        this.combatives$isCrouching = this.combatives$isCrouching(!pose.isPoseClear(Pose.STANDING));

        if (this.isSprinting() != this.combatives$movementStorage.isSprinting && (this.isInWater() || pose.isSwimming())) {
            MovementDiagnostics.debug(self, "client restored Aqua water sprint state");
            this.setSprinting(this.combatives$movementStorage.isSprinting);
        }

        boolean isSaturated = (float) self.getFoodStats().getFoodLevel() > 6.0F || self.capabilities.allowFlying;
        this.combatives$startSprinting(isSaturated);
        this.combatives$stopSprinting(isSaturated);
        this.combatives$handleWaterSneaking();
    }

    private void combatives$updatePlayerMoveState() {
        if (!this.movementInput.sneak && this.isForcedDown()) {
            this.movementInput.moveStrafe *= 0.3F;
            this.movementInput.moveForward *= 0.3F;
            MovementDiagnostics.debug((EntityPlayerSP) (Object) this, "client movement slowed for forced crawl/swim pose");
        }
        if (this.movementInput.sneak && !this.isForcedDown()) {
            this.movementInput.moveStrafe /= 0.3F;
            this.movementInput.moveForward /= 0.3F;
        }
    }

    private boolean combatives$isCrouching(boolean cantStand) {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        ICombativesPlayerPose pose = (ICombativesPlayerPose) self;
        if ((!this.combatives$movementStorage.isFlying || !cantStand) && !pose.isSwimming() && (self.onGround || !this.isInWater())) {
            if (!self.isOnLadder() && (pose.isPoseClear(Pose.CROUCHING) || self.noClip)) {
                return this.movementInput.sneak || pose.isResizingAllowed() && !self.isPlayerSleeping() && cantStand;
            }
        }
        return false;
    }

    private void combatives$startSprinting(boolean isSaturated) {
        boolean wasSneaking = this.combatives$movementStorage.sneak;
        boolean wasSwimmingMove = this.isUsingSwimmingAnimation(this.combatives$movementStorage.moveForward, this.combatives$movementStorage.moveStrafe);
        boolean sprintEnvironment = ((EntityPlayerSP) (Object) this).onGround || this.canSwimClient() || this.combatives$movementStorage.isFlying;
        boolean sprintKeyDown = this.mc != null && this.mc.gameSettings.keyBindSprint.getIsKeyPressed();
        if (sprintEnvironment && !wasSneaking && !wasSwimmingMove && this.isUsingSwimmingAnimation() && !this.isSprinting()
            && isSaturated && !this.isPotionActive(Potion.blindness)) {
            if (this.combatives$movementStorage.sprintToggleTimer <= 0 && !sprintKeyDown) {
                this.sprintToggleTimer = 7;
            } else {
                MovementDiagnostics.debug((EntityPlayerSP) (Object) this, "client started Aqua swim sprint");
                this.setSprinting(true);
            }
        }
        if (!this.isSprinting() && (!this.isInWater() || this.canSwimClient()) && this.isUsingSwimmingAnimation()
            && isSaturated && !this.isPotionActive(Potion.blindness) && sprintKeyDown) {
            MovementDiagnostics.debug((EntityPlayerSP) (Object) this, "client started Aqua swim sprint from sprint key");
            this.setSprinting(true);
        }
    }

    private void combatives$stopSprinting(boolean isSaturated) {
        if (!this.isSprinting()) {
            return;
        }
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        ICombativesPlayerPose pose = (ICombativesPlayerPose) self;
        boolean notMoving = !this.isMovingForward(this.movementInput.moveForward, this.movementInput.moveStrafe) || !isSaturated;
        boolean collided = notMoving || this.isInWater() && !this.canSwimClient() && !this.combatives$movementStorage.isFlying;
        if (pose.isSwimming()) {
            if (!this.movementInput.sneak && notMoving || !this.isInWater()) {
                MovementDiagnostics.debug(self, !this.isInWater() ? "swimming cancelled: left water" : "swimming cancelled: movement/saturation stopped");
                this.setSprinting(false);
            }
        } else if (collided) {
            this.setSprinting(false);
        }
    }

    private void combatives$handleWaterSneaking() {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        if (this.isInWater() && this.movementInput.sneak && !self.capabilities.isFlying) {
            self.motionY -= 0.03999999910593033D * self.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed).getAttributeValue();
        }
    }
}
