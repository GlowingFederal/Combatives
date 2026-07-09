package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovementInput;

public final class MovementCameraState {
    private static final float INPUT_DEADZONE = 0.04F;
    private static final float SPEED_DEADZONE = 0.003F;
    private static final float INPUT_SMOOTHING = 0.22F;
    private static final float SPEED_SMOOTHING = 0.18F;

    private float forward;
    private float strafe;
    private float speed;
    private float walkPhase;
    private float vanillaCameraYaw;
    private float vanillaCameraPitch;
    private boolean crawling;
    private boolean swimming;
    private boolean sneaking;
    private boolean sprinting;
    private boolean grounded;
    private boolean landed;
    private float landingStrength;
    private float lastFallDistance;
    private double lastAirborneMotionY;
    private boolean wasGrounded = true;
    private double previousMotionY;
    private float previousFallDistance;

    public void update(EntityPlayerSP player, float partialTicks) {
        MovementInput input = player.movementInput;
        float targetForward = input == null ? 0.0F : applyDeadzone(input.moveForward, INPUT_DEADZONE);
        float targetStrafe = input == null ? 0.0F : applyDeadzone(input.moveStrafe, INPUT_DEADZONE);
        float horizontalSpeed = (float) Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
        if (horizontalSpeed < SPEED_DEADZONE) horizontalSpeed = 0.0F;

        this.forward += (targetForward - this.forward) * INPUT_SMOOTHING;
        this.strafe += (targetStrafe - this.strafe) * INPUT_SMOOTHING;
        this.speed += (horizontalSpeed - this.speed) * SPEED_SMOOTHING;

        float walkedDelta = player.distanceWalkedModified - player.prevDistanceWalkedModified;
        this.walkPhase = -(player.distanceWalkedModified + walkedDelta * partialTicks);
        this.vanillaCameraYaw = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
        this.vanillaCameraPitch = player.prevCameraPitch + (player.cameraPitch - player.prevCameraPitch) * partialTicks;

        this.grounded = player.onGround;
        this.sneaking = player.isSneaking();
        this.sprinting = player.isSprinting();
        this.swimming = false;
        this.crawling = false;
        if (player instanceof ICombativesPlayerPose) {
            ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
            this.swimming = pose.isSwimming() || pose.isActuallySwimming();
            this.crawling = !this.swimming && pose.getPose() == Pose.SWIMMING;
        }
        boolean wasAirborne = !this.wasGrounded;
        boolean realLanding = wasAirborne && player.onGround && (this.previousFallDistance > 1.5F || this.previousMotionY < -0.35D);
        float fallSeverity = Math.max(this.previousFallDistance, (float) (-this.previousMotionY * 4.0D));
        this.landed = CombativesConfig.enableCombativesCamera && CombativesConfig.enableLandingCameraFeedback && realLanding && fallSeverity > 1.5F;
        this.landingStrength = this.landed ? Math.min((fallSeverity - 1.5F) / 7.0F, 1.0F) : 0.0F;

        if (Combatives.logger != null && CombativesConfig.debugCamera && !player.onGround) {
            Combatives.logger.info("Combatives landing airborne tracking: motionY={}, fallDistance={}, previousMotionY={}, previousFallDistance={}", player.motionY, player.fallDistance, this.previousMotionY, this.previousFallDistance);
        }
        if (Combatives.logger != null && CombativesConfig.debugCamera && realLanding) {
            Combatives.logger.info("Combatives landing detected: previousMotionY={}, previousFallDistance={}, severity={}, strength={}", this.previousMotionY, this.previousFallDistance, fallSeverity, this.landingStrength);
            if (!this.landed) Combatives.logger.info("Combatives landing ignored due to low severity or disabled toggles: enableCombativesCamera={}, enableLandingCameraFeedback={}, severity={}", CombativesConfig.enableCombativesCamera, CombativesConfig.enableLandingCameraFeedback, fallSeverity);
        }
        if (Combatives.logger != null && CombativesConfig.debugCamera && this.landed) {
            Combatives.logger.info("Combatives landing impulse added: strength={}", this.landingStrength);
        }

        if (!player.onGround && player.motionY < 0.0D) {
            this.lastAirborneMotionY = player.motionY;
        } else if (player.onGround) {
            this.lastAirborneMotionY = 0.0D;
        }
        this.wasGrounded = player.onGround;
        this.previousMotionY = player.motionY;
        this.previousFallDistance = player.fallDistance;
        this.lastFallDistance = player.fallDistance;
    }

    private static float applyDeadzone(float value, float deadzone) {
        return Math.abs(value) < deadzone ? 0.0F : value;
    }

    public float getForward() { return forward; }
    public float getStrafe() { return strafe; }
    public float getSpeed() { return speed; }
    public float getWalkPhase() { return walkPhase; }
    public float getCameraYaw() { return vanillaCameraYaw; }
    public float getCameraPitch() { return vanillaCameraPitch; }
    public boolean isCrawling() { return crawling; }
    public boolean isSwimming() { return swimming; }
    public boolean isSneaking() { return sneaking; }
    public boolean isSprinting() { return sprinting; }
    public boolean isGrounded() { return grounded; }
    public boolean hasLanded() { return landed; }
    public float getLandingStrength() { return landingStrength; }
}
