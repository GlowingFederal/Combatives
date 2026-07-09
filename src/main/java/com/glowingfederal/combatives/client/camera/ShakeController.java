package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.config.CombativesConfig;

public final class ShakeController {
    private float pitch, roll, vertical, forward, landingVelocity, explosionVelocity, explosionPhase;

    public void update(MovementCameraState state, float partialTicks) {
        float tickScale = Math.max(0.0F, Math.min(1.0F, partialTicks));
        landingVelocity *= 0.72F;
        explosionVelocity *= 0.88F;
        explosionPhase += 0.42F * (0.5F + tickScale * 0.5F);

        float wave = (float) Math.sin(explosionPhase);
        float recovery = (float) Math.cos(explosionPhase * 0.7F);
        vertical = clamp(-landingVelocity * 0.022F + explosionVelocity * wave * 0.018F, -0.05F, 0.04F);
        forward = clamp(-landingVelocity * 0.014F + explosionVelocity * recovery * 0.012F, -0.035F, 0.025F);
        pitch = clamp(landingVelocity * 0.55F + explosionVelocity * wave * 0.9F, -1.5F, 1.5F);
        roll = clamp(landingVelocity * -0.22F + explosionVelocity * recovery * 1.2F, -2.0F, 2.0F);

        if (Math.abs(landingVelocity) < 0.001F) landingVelocity = 0.0F;
        if (Math.abs(explosionVelocity) < 0.001F) explosionVelocity = 0.0F;
    }

    public void addLandingImpulse(float strength) {
        if (!CombativesConfig.enableLandingCameraFeedback) return;
        landingVelocity += clamp(strength * (float) CombativesConfig.landingFeedbackStrength, 0.0F, 1.0F);
        landingVelocity = clamp(landingVelocity, 0.0F, 1.0F);
    }

    public void addDamageImpulse(float strength) { landingVelocity += clamp(strength, 0.0F, 0.5F); }

    public void addExplosionImpulse(float strength) {
        if (!CombativesConfig.enableExplosionCameraFeedback) return;
        explosionVelocity += clamp(strength * (float) CombativesConfig.explosionFeedbackStrength, 0.0F, 1.0F);
        explosionVelocity = clamp(explosionVelocity, 0.0F, 1.0F);
    }

    private static float clamp(float v, float min, float max) { return v < min ? min : v > max ? max : v; }
    public void reset() { pitch = roll = vertical = forward = landingVelocity = explosionVelocity = 0.0F; }
    public float getPitch() { return pitch; }
    public float getRoll() { return roll; }
    public float getVertical() { return vertical; }
    public float getForward() { return forward; }
}
