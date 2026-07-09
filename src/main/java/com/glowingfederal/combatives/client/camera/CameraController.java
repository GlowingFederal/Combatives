package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import org.lwjgl.opengl.GL11;

public final class CameraController {
    public static final CameraController INSTANCE = new CameraController();

    private final MovementCameraState movement = new MovementCameraState();
    private final LeanController lean = new LeanController();
    private final BobController bob = new BobController();
    private final FOVController fov = new FOVController();
    private final ShakeController shake = new ShakeController();

    private static final float MAX_CAMERA_PITCH_DEGREES = 2.0F;
    private static final float MAX_CAMERA_ROLL_DEGREES = 4.0F;
    private static final float MAX_CAMERA_X_OFFSET = 0.04F;
    private static final float MAX_CAMERA_Y_OFFSET = 0.06F;
    private static final float MAX_CAMERA_Z_OFFSET = 0.04F;

    private float leanRoll, leanPitch, bobVertical, bobSway, bobPitch, bobRoll, shakeVertical, shakeForward, shakePitch, shakeRoll, fovModifier;

    private CameraController() {}

    public void update(Minecraft mc, EntityPlayerSP player, float partialTicks) {
        if (!CombativesConfig.enableCombativesCamera || mc == null || player == null) { reset(); return; }
        movement.update(player, partialTicks);
        if (CombativesConfig.enableCameraShake && CombativesConfig.enableLandingCameraFeedback && movement.hasLanded()) shake.addLandingImpulse(movement.getLandingStrength());
        if (CombativesConfig.enableMovementLean) lean.update(movement); else lean.reset();
        if (CombativesConfig.enableProceduralBob) bob.update(movement); else bob.reset();
        if (CombativesConfig.enableMovementFov) fov.update(movement); else fov.reset();
        if (CombativesConfig.enableCameraShake) shake.update(movement, partialTicks); else shake.reset();
        leanRoll = lean.getRoll(); leanPitch = lean.getPitch(); bobVertical = bob.getVertical(); bobSway = bob.getSway(); bobPitch = bob.getPitch(); bobRoll = bob.getRoll();
        shakeVertical = shake.getVertical(); shakeForward = shake.getForward(); shakePitch = shake.getPitch(); shakeRoll = shake.getRoll(); fovModifier = fov.getModifier();

        if (Combatives.logger != null && CombativesConfig.debugCamera && hasNonzeroFeedback()) {
            Combatives.logger.info("Combatives camera transform sampled nonzero impulse: partialTicks={}, shakeVertical={}, shakeForward={}, shakePitch={}, shakeRoll={}", partialTicks, shakeVertical, shakeForward, shakePitch, shakeRoll);
        }
    }

    public void applyTransforms(float partialTicks) {
        if (!CombativesConfig.enableCombativesCamera) return;

        float xOffset = clamp(bobSway, -MAX_CAMERA_X_OFFSET, MAX_CAMERA_X_OFFSET);
        float yOffset = clamp(bobVertical + shakeVertical, -MAX_CAMERA_Y_OFFSET, MAX_CAMERA_Y_OFFSET);
        float zOffset = clamp(shakeForward, -MAX_CAMERA_Z_OFFSET, MAX_CAMERA_Z_OFFSET);
        GL11.glTranslatef(xOffset, yOffset, zOffset);

        float pitch = 0.0F;
        float roll = 0.0F;
        if (CombativesConfig.enableCameraRotations) {
            pitch = clamp(bobPitch + shakePitch + leanPitch, -MAX_CAMERA_PITCH_DEGREES, MAX_CAMERA_PITCH_DEGREES);
            roll = clamp(bobRoll + shakeRoll + leanRoll, -MAX_CAMERA_ROLL_DEGREES, MAX_CAMERA_ROLL_DEGREES);
            GL11.glRotatef(pitch, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(roll, 0.0F, 0.0F, 1.0F);
        }

        if (Combatives.logger != null && CombativesConfig.debugCamera && hasNonzeroFeedback()) {
            Combatives.logger.info("Combatives actual GL transform applied: partialTicks={}, translateX={}, translateY={}, translateZ={}, rotatePitch={}, rotateRoll={}, rotationsEnabled={}", partialTicks, xOffset, yOffset, zOffset, pitch, roll, CombativesConfig.enableCameraRotations);
        }
    }

    public void applyHandTransforms(float partialTicks) {
        if (!CombativesConfig.enableCombativesCamera || !CombativesConfig.enableProceduralBob) return;
        applyVanillaStyleBob();
    }

    private void applyVanillaStyleBob() {
        float xOffset = clamp(bobSway, -MAX_CAMERA_X_OFFSET, MAX_CAMERA_X_OFFSET);
        float yOffset = clamp(bobVertical, -MAX_CAMERA_Y_OFFSET, MAX_CAMERA_Y_OFFSET);
        float zOffset = clamp(0.0F, -MAX_CAMERA_Z_OFFSET, MAX_CAMERA_Z_OFFSET);
        GL11.glTranslatef(xOffset, yOffset, zOffset);
        if (!CombativesConfig.enableCameraRotations) return;
        GL11.glRotatef(clamp(bobPitch, -MAX_CAMERA_PITCH_DEGREES, MAX_CAMERA_PITCH_DEGREES), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(clamp(bobRoll, -MAX_CAMERA_ROLL_DEGREES, MAX_CAMERA_ROLL_DEGREES), 0.0F, 0.0F, 1.0F);
    }

    private boolean hasNonzeroFeedback() {
        return Math.abs(shakeVertical) > 0.0001F || Math.abs(shakeForward) > 0.0001F || Math.abs(shakePitch) > 0.0001F || Math.abs(shakeRoll) > 0.0001F;
    }

    private static float clamp(float value, float min, float max) {
        return value < min ? min : value > max ? max : value;
    }

    public void reset() { lean.reset(); bob.reset(); fov.reset(); shake.reset(); leanRoll = leanPitch = bobVertical = bobSway = bobPitch = bobRoll = shakeVertical = shakeForward = shakePitch = shakeRoll = fovModifier = 0.0F; }

    public void addExplosionFeedback(EntityPlayerSP player, double x, double y, double z, float strength) {
        if (!CombativesConfig.enableCombativesCamera || !CombativesConfig.enableCameraShake || !CombativesConfig.enableExplosionCameraFeedback || player == null) {
            if (Combatives.logger != null && CombativesConfig.debugCamera) Combatives.logger.info("Combatives explosion feedback rejected: enableCombativesCamera={}, enableCameraShake={}, enableExplosionCameraFeedback={}, hasPlayer={}", CombativesConfig.enableCombativesCamera, CombativesConfig.enableCameraShake, CombativesConfig.enableExplosionCameraFeedback, player != null);
            return;
        }
        double dx = player.posX - x;
        double dy = player.posY + player.getEyeHeight() - y;
        double dz = player.posZ - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float radius = Math.max(4.0F, strength * 4.0F);
        float falloff = 1.0F - (float) (distance / radius);
        if (Combatives.logger != null && CombativesConfig.debugCamera) Combatives.logger.info("Combatives explosion feedback distance: distance={}, radius={}, falloff={}, strength={}", distance, radius, falloff, strength);
        if (distance > radius) {
            if (Combatives.logger != null && CombativesConfig.debugCamera) Combatives.logger.info("Combatives explosion feedback rejected: outside radius");
            return;
        }
        float impulse = clamp((strength / 6.0F) * falloff, 0.0F, 1.0F);
        if (impulse <= 0.0F) {
            if (Combatives.logger != null && CombativesConfig.debugCamera) Combatives.logger.info("Combatives explosion feedback rejected: impulse={}", impulse);
            return;
        }
        shake.addExplosionImpulse(impulse);
        if (Combatives.logger != null && CombativesConfig.debugCamera) Combatives.logger.info("Combatives explosion feedback accepted: impulse={}", impulse);
    }
    public float getFovModifier() { return fovModifier; }
}
