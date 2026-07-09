package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;

public final class ShakeController {
    private static final float LANDING_IMPACT_TICKS = 3.0F;
    private static final float LANDING_RECOVERY_TICKS = 12.0F;
    private static final float EXPLOSION_DECAY_PER_TICK = 0.82F;

    private float pitch, roll, vertical, forward, lateral;
    private float landingDip, landingPitch, landingRecovery, landingRoll;
    private float landingAge = 999.0F;
    private float explosionPunch, explosionRoll, explosionVertical, explosionForward, explosionLateral, explosionPhase;

    public void update(MovementCameraState state, float partialTicks) {
        float tickDelta = 1.0F;
        float tickScale = clamp(partialTicks, 0.0F, 1.0F);

        landingAge += tickDelta;
        float landingY = 0.0F;
        float landingZ = 0.0F;
        float landingPitchOut = 0.0F;
        float landingRollOut = 0.0F;
        if (landingAge < LANDING_IMPACT_TICKS + LANDING_RECOVERY_TICKS) {
            if (landingAge <= LANDING_IMPACT_TICKS) {
                float impact = easeOutCubic(landingAge / LANDING_IMPACT_TICKS);
                landingY = -landingDip * impact;
                landingZ = -landingRecovery * 0.65F * impact;
                landingPitchOut = landingPitch * impact;
                landingRollOut = landingRoll * impact;
            } else {
                float t = (landingAge - LANDING_IMPACT_TICKS) / LANDING_RECOVERY_TICKS;
                float recover = 1.0F - easeOutBack(clamp(t, 0.0F, 1.0F));
                landingY = -landingDip * recover;
                landingZ = -landingRecovery * 0.65F * recover;
                landingPitchOut = landingPitch * recover;
                landingRollOut = landingRoll * recover;
            }
        }

        explosionPhase += 0.45F + tickScale * 0.12F;
        float settle = (float) Math.sin(explosionPhase) * 0.35F;
        float explosionPitchOut = explosionPunch * (1.0F + settle);
        float explosionRollOut = explosionRoll * (0.82F + (float) Math.cos(explosionPhase * 0.85F) * 0.18F);
        float explosionY = explosionVertical * (0.78F + (float) Math.sin(explosionPhase * 0.75F) * 0.22F);
        float explosionZ = explosionForward * (0.82F + (float) Math.cos(explosionPhase * 0.65F) * 0.18F);
        float explosionX = explosionLateral * (0.82F + (float) Math.sin(explosionPhase * 0.7F) * 0.18F);

        pitch = clamp(landingPitchOut + explosionPitchOut, -3.0F, 3.0F);
        roll = clamp(landingRollOut + explosionRollOut, -1.5F, 1.5F);
        vertical = clamp(landingY + explosionY, -0.065F, 0.065F);
        forward = clamp(landingZ + explosionZ, -0.035F, 0.035F);
        lateral = clamp(explosionX, -0.035F, 0.035F);

        explosionPunch *= EXPLOSION_DECAY_PER_TICK;
        explosionRoll *= EXPLOSION_DECAY_PER_TICK;
        explosionVertical *= EXPLOSION_DECAY_PER_TICK;
        explosionForward *= EXPLOSION_DECAY_PER_TICK;
        explosionLateral *= EXPLOSION_DECAY_PER_TICK;
        if (Math.abs(explosionPunch) < 0.001F) explosionPunch = 0.0F;
        if (Math.abs(explosionRoll) < 0.001F) explosionRoll = 0.0F;
        if (Math.abs(explosionVertical) < 0.0005F) explosionVertical = 0.0F;
        if (Math.abs(explosionForward) < 0.0005F) explosionForward = 0.0F;
        if (Math.abs(explosionLateral) < 0.0005F) explosionLateral = 0.0F;
    }

    public void addLandingImpulse(float severity) {
        if (!CombativesConfig.enableLandingCameraFeedback) return;
        float scaled = clamp(severity * (float) CombativesConfig.landingFeedbackStrength, 0.0F, 1.0F);
        if (scaled <= 0.0F) {
            if (Combatives.logger != null && CombativesConfig.debugCamera) Combatives.logger.info("Combatives landing impulse rejected: reason=zero_severity, severity={}", severity);
            return;
        }
        float response = (float) Math.sqrt(scaled);
        landingDip = Math.max(landingDip, 0.018F + response * 0.042F);
        landingPitch = Math.max(landingPitch, 0.8F + response * 2.2F);
        landingRecovery = Math.max(landingRecovery, 0.006F + response * 0.012F);
        landingRoll = clamp((response * 0.35F) * (System.nanoTime() % 2L == 0L ? 1.0F : -1.0F), -0.35F, 0.35F);
        landingAge = 0.0F;
        if (Combatives.logger != null && CombativesConfig.debugCamera) {
            Combatives.logger.info("Combatives landing impulse created: severity={}, response={}, dip={}, pitch={}, recovery={}, roll={}", scaled, response, landingDip, landingPitch, landingRecovery, landingRoll);
        }
    }

    public void addDamageImpulse(float strength) { addLandingImpulse(clamp(strength, 0.0F, 0.5F)); }

    public void addExplosionImpulse(float response, float sideBias, float directionBias) {
        if (!CombativesConfig.enableExplosionCameraFeedback) return;
        float scaled = clamp(response * (float) CombativesConfig.explosionFeedbackStrength, 0.0F, 1.4F);
        if (scaled <= 0.0F) {
            if (Combatives.logger != null && CombativesConfig.debugCamera) Combatives.logger.info("Combatives explosion impulse rejected: reason=zero_response, response={}", response);
            return;
        }
        float rollSign = sideBias == 0.0F ? (System.nanoTime() % 2L == 0L ? 1.0F : -1.0F) : -Math.signum(sideBias);
        explosionPunch = clamp(explosionPunch + scaled * 2.2F, -3.0F, 3.0F);
        explosionRoll = clamp(explosionRoll + rollSign * (0.35F + scaled * 0.85F), -1.5F, 1.5F);
        explosionVertical = clamp(explosionVertical + scaled * 0.026F, -0.035F, 0.035F);
        explosionForward = clamp(explosionForward + directionBias * scaled * 0.024F, -0.035F, 0.035F);
        explosionLateral = clamp(explosionLateral + sideBias * scaled * 0.018F, -0.035F, 0.035F);
        explosionPhase = 0.0F;
        if (Combatives.logger != null && CombativesConfig.debugCamera) {
            Combatives.logger.info("Combatives explosion impulse created: response={}, scaled={}, punch={}, roll={}, vertical={}, forward={}, lateral={}", response, scaled, explosionPunch, explosionRoll, explosionVertical, explosionForward, explosionLateral);
        }
    }

    private static float easeOutCubic(float t) { t = clamp(t, 0.0F, 1.0F); float inv = 1.0F - t; return 1.0F - inv * inv * inv; }
    private static float easeOutBack(float t) { t = clamp(t, 0.0F, 1.0F); float c1 = 1.70158F; float c3 = c1 + 1.0F; return 1.0F + c3 * (t - 1.0F) * (t - 1.0F) * (t - 1.0F) + c1 * (t - 1.0F) * (t - 1.0F); }
    private static float clamp(float v, float min, float max) { return v < min ? min : v > max ? max : v; }
    public void reset() { pitch = roll = vertical = forward = lateral = landingDip = landingPitch = landingRecovery = landingRoll = explosionPunch = explosionRoll = explosionVertical = explosionForward = explosionLateral = explosionPhase = 0.0F; landingAge = 999.0F; }
    public float getPitch() { return pitch; }
    public float getRoll() { return roll; }
    public float getVertical() { return vertical; }
    public float getForward() { return forward; }
    public float getLateral() { return lateral; }
}
