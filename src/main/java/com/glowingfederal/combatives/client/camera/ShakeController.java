package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;

public final class ShakeController {
    private static final float LANDING_MAX_DIP = 0.06F;
    private static final float LANDING_MAX_PITCH = 3.0F;
    private static final float LANDING_MAX_FORWARD = 0.018F;
    private static final float LANDING_MAX_ROLL = 0.35F;
    private static final float EXPLOSION_MAX_PITCH = 2.8F;
    private static final float EXPLOSION_MAX_ROLL = 1.5F;
    private static final float EXPLOSION_MAX_VERTICAL = 0.04F;
    private static final float EXPLOSION_MAX_FORWARD = 0.04F;
    private static final float EXPLOSION_MAX_LATERAL = 0.035F;

    private float pitch, roll, vertical, forward, lateral, bobSuppression;
    private float landingPosition, landingVelocity, landingRollBias;
    private float explosionX, explosionY, explosionZ, explosionPitch, explosionRoll;
    private float explosionVelX, explosionVelY, explosionVelZ, explosionVelPitch, explosionVelRoll;
    private float shockX, shockY, shockZ, shockPitch, shockRoll, shockAge = 1.0F;
    private long lastUpdateNanos;

    public void update(MovementCameraState state, float partialTicks) {
        float dt = getFrameDeltaSeconds();

        updateLandingSpring(dt);
        updateExplosionSpring(dt);
        updateShock(dt);

        float landingCompression = clamp(landingPosition, -0.18F, 1.0F);
        float landingVelocityShape = clamp(landingVelocity * 0.055F, -0.35F, 0.35F);
        float landingY = -LANDING_MAX_DIP * landingCompression;
        float landingPitchOut = LANDING_MAX_PITCH * landingCompression * 0.78F;
        float landingZ = -LANDING_MAX_FORWARD * landingVelocityShape;
        float landingRollOut = LANDING_MAX_ROLL * landingCompression * landingRollBias;

        pitch = clamp(landingPitchOut + explosionPitch + shockPitch, -3.0F, 3.0F);
        roll = clamp(landingRollOut + explosionRoll + shockRoll, -1.5F, 1.5F);
        vertical = clamp(landingY + explosionY + shockY, -0.065F, 0.065F);
        forward = clamp(landingZ + explosionZ + shockZ, -0.04F, 0.04F);
        lateral = clamp(explosionX + shockX, -0.035F, 0.035F);

        float landingEnergy = Math.min(1.0F, Math.abs(landingPosition) + Math.abs(landingVelocity) * 0.08F);
        float explosionEnergy = Math.min(1.0F, Math.abs(explosionX) * 18.0F + Math.abs(explosionY) * 14.0F + Math.abs(explosionZ) * 18.0F + Math.abs(shockPitch) * 0.2F);
        bobSuppression += ((Math.max(landingEnergy, explosionEnergy) * 0.55F) - bobSuppression) * Math.min(1.0F, dt * 8.0F);
    }

    private void updateLandingSpring(float dt) {
        float acceleration = -190.0F * landingPosition - 20.0F * landingVelocity;
        landingVelocity += acceleration * dt;
        landingPosition += landingVelocity * dt;
        landingPosition = clamp(landingPosition, -0.18F, 1.0F);
        if (Math.abs(landingPosition) < 0.00001F && Math.abs(landingVelocity) < 0.00001F) {
            landingPosition = 0.0F;
            landingVelocity = 0.0F;
        }
    }

    private void updateExplosionSpring(float dt) {
        explosionVelX += (-55.0F * explosionX - 9.0F * explosionVelX) * dt;
        explosionVelY += (-65.0F * explosionY - 10.0F * explosionVelY) * dt;
        explosionVelZ += (-55.0F * explosionZ - 9.0F * explosionVelZ) * dt;
        explosionVelPitch += (-70.0F * explosionPitch - 11.0F * explosionVelPitch) * dt;
        explosionVelRoll += (-60.0F * explosionRoll - 9.5F * explosionVelRoll) * dt;
        explosionX = clamp(explosionX + explosionVelX * dt, -0.035F, 0.035F);
        explosionY = clamp(explosionY + explosionVelY * dt, -0.04F, 0.04F);
        explosionZ = clamp(explosionZ + explosionVelZ * dt, -0.04F, 0.04F);
        explosionPitch = clamp(explosionPitch + explosionVelPitch * dt, -3.0F, 3.0F);
        explosionRoll = clamp(explosionRoll + explosionVelRoll * dt, -1.5F, 1.5F);
        if (Math.abs(explosionX) < 0.00001F && Math.abs(explosionVelX) < 0.00001F) { explosionX = 0.0F; explosionVelX = 0.0F; }
        if (Math.abs(explosionY) < 0.00001F && Math.abs(explosionVelY) < 0.00001F) { explosionY = 0.0F; explosionVelY = 0.0F; }
        if (Math.abs(explosionZ) < 0.00001F && Math.abs(explosionVelZ) < 0.00001F) { explosionZ = 0.0F; explosionVelZ = 0.0F; }
        if (Math.abs(explosionPitch) < 0.0001F && Math.abs(explosionVelPitch) < 0.0001F) { explosionPitch = 0.0F; explosionVelPitch = 0.0F; }
        if (Math.abs(explosionRoll) < 0.0001F && Math.abs(explosionVelRoll) < 0.0001F) { explosionRoll = 0.0F; explosionVelRoll = 0.0F; }
    }

    private void updateShock(float dt) {
        shockAge += dt;
        float decay = (float) Math.exp(-dt * 32.0F);
        shockX *= decay;
        shockY *= decay;
        shockZ *= decay;
        shockPitch *= decay;
        shockRoll *= decay;
        if (shockAge > 0.09F) {
            if (Math.abs(shockX) < 0.00001F) shockX = 0.0F;
            if (Math.abs(shockY) < 0.00001F) shockY = 0.0F;
            if (Math.abs(shockZ) < 0.00001F) shockZ = 0.0F;
            if (Math.abs(shockPitch) < 0.0001F) shockPitch = 0.0F;
            if (Math.abs(shockRoll) < 0.0001F) shockRoll = 0.0F;
        }
    }

    public void addLandingImpulse(float severity, float strafe, float speed) {
        if (!CombativesConfig.enableLandingCameraFeedback) return;
        float scaledSeverity = clamp(severity * (float) CombativesConfig.landingFeedbackStrength, 0.0F, 1.0F);
        if (scaledSeverity <= 0.0F) {
            if (Combatives.logger != null && CombativesConfig.verboseCameraDebug) Combatives.logger.info("Combatives landing impulse rejected: reason=zero_severity, severity={}", severity);
            return;
        }
        float response = (float) Math.pow(scaledSeverity, 0.65F);
        landingVelocity += 13.0F * response;
        landingVelocity = clamp(landingVelocity, -3.0F, 16.0F);
        float movementRoll = clamp(strafe * speed * 2.0F, -1.0F, 1.0F);
        landingRollBias = Math.abs(movementRoll) > 0.12F ? -movementRoll : 0.0F;
        if (Combatives.logger != null && (CombativesConfig.verboseCameraDebug || (CombativesConfig.debugCamera && response >= 0.35F))) {
            Combatives.logger.info("Combatives landing impulse created: severity={}, response={}, targetDip={}, targetPitch={}, rollBias={}", scaledSeverity, response, LANDING_MAX_DIP * response, LANDING_MAX_PITCH * response, landingRollBias);
        }
    }

    public void addDamageImpulse(float strength) { addLandingImpulse(clamp(strength, 0.0F, 0.5F), 0.0F, 0.0F); }

    public void addExplosionImpulse(float response, float localForward, float localRight, float localVertical) {
        if (!CombativesConfig.enableExplosionCameraFeedback) return;
        float scaled = clamp(response * (float) CombativesConfig.explosionFeedbackStrength, 0.0F, 1.0F);
        if (scaled <= 0.0F) {
            if (Combatives.logger != null && CombativesConfig.verboseCameraDebug) Combatives.logger.info("Combatives explosion impulse rejected: reason=zero_response, response={}", response);
            return;
        }

        float sharpness = smoothstep(scaled);
        shockX = clamp(shockX + localRight * EXPLOSION_MAX_LATERAL * scaled * 0.85F, -0.035F, 0.035F);
        shockY = clamp(shockY + localVertical * EXPLOSION_MAX_VERTICAL * scaled * 0.75F, -0.04F, 0.04F);
        shockZ = clamp(shockZ - localForward * EXPLOSION_MAX_FORWARD * scaled * 0.85F, -0.04F, 0.04F);
        shockPitch = clamp(shockPitch + (0.35F + Math.abs(localForward) * 0.65F + Math.max(0.0F, localVertical) * 0.25F) * EXPLOSION_MAX_PITCH * scaled * 0.55F, -3.0F, 3.0F);
        shockRoll = clamp(shockRoll - localRight * EXPLOSION_MAX_ROLL * scaled * 0.75F, -1.5F, 1.5F);
        shockAge = 0.0F;

        explosionVelX += localRight * EXPLOSION_MAX_LATERAL * scaled * 18.0F;
        explosionVelY += localVertical * EXPLOSION_MAX_VERTICAL * scaled * 16.0F;
        explosionVelZ += -localForward * EXPLOSION_MAX_FORWARD * scaled * 18.0F;
        explosionVelPitch += (0.4F + Math.abs(localForward) * 0.6F) * EXPLOSION_MAX_PITCH * sharpness * 7.0F;
        explosionVelRoll += -localRight * EXPLOSION_MAX_ROLL * scaled * 8.0F;
        explosionVelX = clamp(explosionVelX, -0.8F, 0.8F);
        explosionVelY = clamp(explosionVelY, -0.8F, 0.8F);
        explosionVelZ = clamp(explosionVelZ, -0.8F, 0.8F);
        explosionVelPitch = clamp(explosionVelPitch, -22.0F, 22.0F);
        explosionVelRoll = clamp(explosionVelRoll, -14.0F, 14.0F);

        if (Combatives.logger != null && (CombativesConfig.verboseCameraDebug || (CombativesConfig.debugCamera && scaled >= 0.35F))) {
            Combatives.logger.info("Combatives explosion impulse created: response={}, localForward={}, localRight={}, localVertical={}", scaled, localForward, localRight, localVertical);
        }
    }

    private float getFrameDeltaSeconds() {
        long now = System.nanoTime();
        if (lastUpdateNanos == 0L) {
            lastUpdateNanos = now;
            return 1.0F / 60.0F;
        }
        float dt = (now - lastUpdateNanos) / 1000000000.0F;
        lastUpdateNanos = now;
        return clamp(dt, 1.0F / 240.0F, 0.05F);
    }

    private static float smoothstep(float t) { t = clamp(t, 0.0F, 1.0F); return t * t * (3.0F - 2.0F * t); }
    private static float clamp(float v, float min, float max) { return v < min ? min : v > max ? max : v; }
    public void reset() { pitch = roll = vertical = forward = lateral = bobSuppression = landingPosition = landingVelocity = landingRollBias = explosionX = explosionY = explosionZ = explosionPitch = explosionRoll = explosionVelX = explosionVelY = explosionVelZ = explosionVelPitch = explosionVelRoll = shockX = shockY = shockZ = shockPitch = shockRoll = 0.0F; shockAge = 1.0F; lastUpdateNanos = 0L; }
    public float getPitch() { return pitch; }
    public float getRoll() { return roll; }
    public float getVertical() { return vertical; }
    public float getForward() { return forward; }
    public float getLateral() { return lateral; }
    public float getBobSuppression() { return bobSuppression; }
}
