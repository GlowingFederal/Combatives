package com.glowingfederal.combatives.client.camera;

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

    private float leanRoll, leanPitch, bobVertical, bobSway, bobPitch, bobRoll, shakeVertical, shakePitch, shakeRoll, fovModifier;

    private CameraController() {}

    public void update(Minecraft mc, EntityPlayerSP player, float partialTicks) {
        if (!CombativesConfig.enableCombativesCamera || mc == null || player == null) { reset(); return; }
        movement.update(player);
        if (CombativesConfig.enableCameraShake && movement.hasLanded()) shake.addLandingImpulse(movement.getLandingStrength());
        if (CombativesConfig.enableMovementLean) lean.update(movement); else lean.reset();
        if (CombativesConfig.enableProceduralBob) bob.update(movement); else bob.reset();
        if (CombativesConfig.enableMovementFov) fov.update(movement); else fov.reset();
        if (CombativesConfig.enableCameraShake) shake.update(movement); else shake.reset();
        leanRoll = lean.getRoll(); leanPitch = lean.getPitch(); bobVertical = bob.getVertical(); bobSway = bob.getSway(); bobPitch = bob.getPitch(); bobRoll = bob.getRoll();
        shakeVertical = shake.getVertical(); shakePitch = shake.getPitch(); shakeRoll = shake.getRoll(); fovModifier = fov.getModifier();
    }

    public void applyTransforms(float partialTicks) {
        if (!CombativesConfig.enableCombativesCamera) return;
        GL11.glTranslatef(bobSway, bobVertical + shakeVertical, 0.0F);
        GL11.glRotatef(leanPitch + bobPitch + shakePitch, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(leanRoll + bobRoll + shakeRoll, 0.0F, 0.0F, 1.0F);
    }

    public void reset() { lean.reset(); bob.reset(); fov.reset(); shake.reset(); leanRoll = leanPitch = bobVertical = bobSway = bobPitch = bobRoll = shakeVertical = shakePitch = shakeRoll = fovModifier = 0.0F; }
    public float getFovModifier() { return fovModifier; }
}
