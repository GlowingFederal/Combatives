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

    private static final float MAX_CAMERA_PITCH_DEGREES = 3.0F;
    private static final float MAX_CAMERA_ROLL_DEGREES = 4.0F;
    private static final float MAX_AMBIENT_X_OFFSET = 0.04F;
    private static final float MAX_AMBIENT_Y_OFFSET = 0.06F;
    private static final float MAX_IMPACT_PITCH_DEGREES = 3.0F;
    private static final float MAX_IMPACT_ROLL_DEGREES = 1.5F;
    private static final float MAX_IMPACT_X_OFFSET = 0.035F;
    private static final float MAX_IMPACT_Y_OFFSET = 0.065F;
    private static final float MAX_IMPACT_Z_OFFSET = 0.035F;

    private float leanRoll, leanPitch, bobVertical, bobSway, bobPitch, bobRoll, shakeVertical, shakeForward, shakeLateral, shakePitch, shakeRoll, fovModifier;

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
        shakeVertical = shake.getVertical(); shakeForward = shake.getForward(); shakeLateral = shake.getLateral(); shakePitch = shake.getPitch(); shakeRoll = shake.getRoll(); fovModifier = fov.getModifier();
    }

    public void applyTransforms(float partialTicks) {
        if (!CombativesConfig.enableCombativesCamera) return;

        float ambientX = clamp(bobSway, -MAX_AMBIENT_X_OFFSET, MAX_AMBIENT_X_OFFSET);
        float ambientY = clamp(bobVertical, -MAX_AMBIENT_Y_OFFSET, MAX_AMBIENT_Y_OFFSET);
        float impactX = clamp(shakeLateral, -MAX_IMPACT_X_OFFSET, MAX_IMPACT_X_OFFSET);
        float impactY = clamp(shakeVertical, -MAX_IMPACT_Y_OFFSET, MAX_IMPACT_Y_OFFSET);
        float impactZ = clamp(shakeForward, -MAX_IMPACT_Z_OFFSET, MAX_IMPACT_Z_OFFSET);
        float xOffset = ambientX + impactX;
        float yOffset = ambientY + impactY;
        float zOffset = impactZ;
        GL11.glTranslatef(xOffset, yOffset, zOffset);

        float pitch = 0.0F;
        float roll = 0.0F;
        if (CombativesConfig.enableCameraRotations) {
            float ambientPitch = clamp(bobPitch + leanPitch, -MAX_CAMERA_PITCH_DEGREES, MAX_CAMERA_PITCH_DEGREES);
            float ambientRoll = clamp(bobRoll + leanRoll, -MAX_CAMERA_ROLL_DEGREES, MAX_CAMERA_ROLL_DEGREES);
            pitch = ambientPitch + clamp(shakePitch, -MAX_IMPACT_PITCH_DEGREES, MAX_IMPACT_PITCH_DEGREES);
            roll = ambientRoll + clamp(shakeRoll, -MAX_IMPACT_ROLL_DEGREES, MAX_IMPACT_ROLL_DEGREES);
            GL11.glRotatef(pitch, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(roll, 0.0F, 0.0F, 1.0F);
        }
    }

    public void applyHandTransforms(float partialTicks) {
        if (!CombativesConfig.enableCombativesCamera || !CombativesConfig.enableProceduralBob) return;
        applyVanillaStyleBob();
    }

    private void applyVanillaStyleBob() {
        float xOffset = clamp(bobSway, -MAX_AMBIENT_X_OFFSET, MAX_AMBIENT_X_OFFSET);
        float yOffset = clamp(bobVertical, -MAX_AMBIENT_Y_OFFSET, MAX_AMBIENT_Y_OFFSET);
        float zOffset = clamp(0.0F, -MAX_IMPACT_Z_OFFSET, MAX_IMPACT_Z_OFFSET);
        GL11.glTranslatef(xOffset, yOffset, zOffset);
        if (!CombativesConfig.enableCameraRotations) return;
        GL11.glRotatef(clamp(bobPitch, -MAX_CAMERA_PITCH_DEGREES, MAX_CAMERA_PITCH_DEGREES), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(clamp(bobRoll, -MAX_CAMERA_ROLL_DEGREES, MAX_CAMERA_ROLL_DEGREES), 0.0F, 0.0F, 1.0F);
    }

    private boolean hasNonzeroFeedback() {
        return Math.abs(shakeVertical) > 0.0001F || Math.abs(shakeForward) > 0.0001F || Math.abs(shakeLateral) > 0.0001F || Math.abs(shakePitch) > 0.0001F || Math.abs(shakeRoll) > 0.0001F;
    }

    private static float clamp(float value, float min, float max) {
        return value < min ? min : value > max ? max : value;
    }

    public void reset() { lean.reset(); bob.reset(); fov.reset(); shake.reset(); leanRoll = leanPitch = bobVertical = bobSway = bobPitch = bobRoll = shakeVertical = shakeForward = shakeLateral = shakePitch = shakeRoll = fovModifier = 0.0F; }

    public void addExplosionFeedback(EntityPlayerSP player, double x, double y, double z, float strength) {
        if (!CombativesConfig.enableCombativesCamera || !CombativesConfig.enableCameraShake || !CombativesConfig.enableExplosionCameraFeedback || player == null) {
            if (Combatives.logger != null && CombativesConfig.debugCamera) Combatives.logger.info("Combatives explosion feedback rejected: enableCombativesCamera={}, enableCameraShake={}, enableExplosionCameraFeedback={}, hasPlayer={}", CombativesConfig.enableCombativesCamera, CombativesConfig.enableCameraShake, CombativesConfig.enableExplosionCameraFeedback, player != null);
            return;
        }
        double dx = player.posX - x;
        double dy = player.posY + player.getEyeHeight() - y;
        double dz = player.posZ - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float radius = Math.max(8.0F, strength * 4.0F);
        float distanceFalloff = clamp(1.0F - (float) (distance / radius), 0.0F, 1.0F);
        float normalizedStrength = clamp(strength / 4.0F, 0.0F, 2.0F);
        float response = (float) Math.sqrt(normalizedStrength * distanceFalloff);
        if (response <= 0.0F) {
            if (Combatives.logger != null && CombativesConfig.debugCamera) Combatives.logger.info("Combatives explosion impulse rejected: reason=outside_radius_or_zero_response, distance={}, radius={}, strength={}", distance, radius, strength);
            return;
        }
        float yawRad = (float) Math.toRadians(player.rotationYaw);
        double lookX = -Math.sin(yawRad);
        double lookZ = Math.cos(yawRad);
        double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        float sideBias = clamp((float) ((lookZ * dx - lookX * dz) / length), -1.0F, 1.0F);
        float directionBias = clamp((float) ((lookX * dx + lookZ * dz) / length), -1.0F, 1.0F);
        shake.addExplosionImpulse(response, sideBias, directionBias);
    }
    public float getFovModifier() { return fovModifier; }
}
