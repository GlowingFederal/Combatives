package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovementInput;
import com.glowingfederal.combatives.config.CameraVisualSetting;

public final class MovementCameraState {
    private static final float INPUT_DEADZONE = 0.04F;
    private static final float SPEED_DEADZONE = 0.003F;
    // Retain the nominal 60 Hz response, evaluated once per 50 ms client tick.
    private static final float INPUT_SMOOTHING = 1.0F - 0.78F * 0.78F * 0.78F;
    private static final float SPEED_SMOOTHING = 1.0F - 0.82F * 0.82F * 0.82F;

    private float forward;
    private float strafe;
    private float speed;
    private float walkPhase;
    private float vanillaCameraYaw;
    private boolean crawling;
    private boolean swimming;
    private boolean sneaking;
    private boolean sprinting;
    private boolean grounded;
    private boolean landed;
    private float landingStrength;

    public void tick(EntityPlayerSP player) {
        MovementInput input = player.movementInput;
        float targetForward = input == null ? 0.0F : applyDeadzone(input.moveForward, INPUT_DEADZONE);
        float targetStrafe = input == null ? 0.0F : applyDeadzone(input.moveStrafe, INPUT_DEADZONE);
        float horizontalSpeed = (float) Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
        if (horizontalSpeed < SPEED_DEADZONE) horizontalSpeed = 0.0F;

        this.forward += (targetForward - this.forward) * CameraVisualSetting.response(INPUT_SMOOTHING);
        this.strafe += (targetStrafe - this.strafe) * CameraVisualSetting.response(INPUT_SMOOTHING);
        this.speed += (horizontalSpeed - this.speed) * CameraVisualSetting.response(SPEED_SMOOTHING);

        this.grounded = player.onGround;
        this.sneaking = player.isSneaking();
        this.sprinting = player.isSprinting();
        this.swimming = false;
        this.crawling = false;
        if (player instanceof ICombativesPlayerPose) {
            ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
            this.swimming = pose.isSwimming() || player.isInWater();
            this.crawling = !this.swimming && pose.getPose() == Pose.SWIMMING;
        }
        // Landing interpretation now belongs exclusively to the entity motion provider.
        this.landed = false;
        this.landingStrength = 0.0F;
    }

    /** Read-only sampling of vanilla's tick endpoints; partialTicks is never a time step. */
    public void sample(EntityPlayerSP player, float partialTicks) {
        this.walkPhase = -(player.prevDistanceWalkedModified
                + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks);
        this.vanillaCameraYaw = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
    }

    public void reset() {
        forward = strafe = speed = walkPhase = vanillaCameraYaw = landingStrength = 0;
        crawling = swimming = sneaking = sprinting = grounded = landed = false;
    }

    private static float applyDeadzone(float value, float deadzone) {
        return Math.abs(value) < deadzone ? 0.0F : value;
    }


    public float getForward() { return forward; }
    public float getStrafe() { return strafe; }
    public float getSpeed() { return speed; }
    public float getWalkPhase() { return walkPhase; }
    public float getCameraYaw() { return vanillaCameraYaw; }
    public boolean isCrawling() { return crawling; }
    public boolean isSwimming() { return swimming; }
    public boolean isSneaking() { return sneaking; }
    public boolean isSprinting() { return sprinting; }
    public boolean isGrounded() { return grounded; }
    public boolean hasLanded() { return landed; }
    public float getLandingStrength() { return landingStrength; }
}
