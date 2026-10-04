package com.glowingfederal.combatives.client.camera;

import net.minecraft.util.MathHelper;
import com.glowingfederal.combatives.config.CameraVisualSetting;

public final class BobController {
    private float vertical;
    private float sway;
    private float pitch;
    private float roll;

    public void update(MovementCameraState state) {
        float intensity = getIntensity(state) * (state.isSprinting()
                ? CameraVisualSetting.SPRINT.get() : CameraVisualSetting.WALK.get());
        float walkPhase = state.getWalkPhase() * (float) Math.PI;
        float bobAmount = state.getCameraYaw() * intensity;

        sway = MathHelper.sin(walkPhase) * bobAmount * 0.5F;
        vertical = -Math.abs(MathHelper.cos(walkPhase) * bobAmount);
        roll = MathHelper.sin(walkPhase) * bobAmount * 3.0F;
        // Vanilla cameraPitch carries vertical-velocity pitch through landing. Takeoff
        // and landing now have separate bounded providers instead of overlapping it.
        pitch = Math.abs(MathHelper.cos(walkPhase - 0.2F) * bobAmount) * 5.0F;
    }

    private static float getIntensity(MovementCameraState state) {
        if (state.isSwimming()) return 0.35F;
        if (state.isCrawling()) return 0.40F;
        if (state.isSneaking()) return 0.55F;
        if (state.isSprinting()) return 1.15F;
        return 0.85F;
    }

    public void reset() { vertical = sway = pitch = roll = 0.0F; }
    public float getVertical() { return vertical; }
    public float getSway() { return sway; }
    public float getPitch() { return pitch; }
    public float getRoll() { return roll; }
}
