package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;

public final class ShakeController {
    private static final float EXPLOSION_MAX_PITCH = 8.0F;
    private static final float EXPLOSION_MAX_ROLL = 4.5F;
    private static final float EXPLOSION_MAX_VERTICAL = 0.16F;
    private static final float EXPLOSION_MAX_FORWARD = 0.14F;
    private static final float EXPLOSION_MAX_LATERAL = 0.14F;

    private float pitch, roll, vertical, forward, lateral, bobSuppression;
    private float explosionX, explosionY, explosionZ, explosionPitch, explosionRoll;
    private float explosionVelX, explosionVelY, explosionVelZ, explosionVelPitch, explosionVelRoll;
    private float shockX, shockY, shockZ, shockPitch, shockRoll, shockAge = 1.0F;
    public void tick() {
        // Fixed substeps keep the stiff impact spring stable at 20 TPS. Render count
        // cannot advance this simulation, including stereo/shader camera passes.
        float dt = 1.0F / 60.0F;
        for (int step = 0; step < 3; step++) {
            updateExplosionSpring(dt);
            updateShock(dt);
        }

        pitch = clamp(explosionPitch + shockPitch, -8.0F, 8.0F);
        roll = clamp(explosionRoll + shockRoll, -4.5F, 4.5F);
        vertical = clamp(explosionY + shockY, -0.2F, 0.2F);
        forward = clamp(explosionZ + shockZ, -0.16F, 0.16F);
        lateral = clamp(explosionX + shockX, -0.16F, 0.16F);
        float explosionEnergy = Math.min(1.0F, Math.abs(explosionX) * 18.0F + Math.abs(explosionY) * 14.0F + Math.abs(explosionZ) * 18.0F + Math.abs(shockPitch) * 0.2F);
        bobSuppression += ((explosionEnergy * 0.55F) - bobSuppression)
                * (1.0F - (float) Math.exp(-0.05F * 8.0F));
    }

    private void updateExplosionSpring(float dt) {
        explosionVelX += (-55.0F * explosionX - 9.0F * explosionVelX) * dt;
        explosionVelY += (-65.0F * explosionY - 10.0F * explosionVelY) * dt;
        explosionVelZ += (-55.0F * explosionZ - 9.0F * explosionVelZ) * dt;
        explosionVelPitch += (-70.0F * explosionPitch - 11.0F * explosionVelPitch) * dt;
        explosionVelRoll += (-60.0F * explosionRoll - 9.5F * explosionVelRoll) * dt;
        explosionX = clamp(explosionX + explosionVelX * dt, -0.16F, 0.16F);
        explosionY = clamp(explosionY + explosionVelY * dt, -0.18F, 0.18F);
        explosionZ = clamp(explosionZ + explosionVelZ * dt, -0.16F, 0.16F);
        explosionPitch = clamp(explosionPitch + explosionVelPitch * dt, -8.0F, 8.0F);
        explosionRoll = clamp(explosionRoll + explosionVelRoll * dt, -4.5F, 4.5F);
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

    public void addExplosionImpulse(float response, float localForward, float localRight, float localVertical) {
        if (!CombativesConfig.enableExplosionCameraFeedback) return;
        float scaled = clamp(response, 0.0F, 1.0F);
        if (scaled <= 0.0F) {
            if (Combatives.logger != null && CombativesConfig.verboseCameraDebug) Combatives.logger.info("Combatives explosion impulse rejected: reason=zero_response, response={}", response);
            return;
        }

        float sharpness = smoothstep(scaled);
        shockX = clamp(shockX + localRight * EXPLOSION_MAX_LATERAL * scaled * 1.25F, -0.16F, 0.16F);
        shockY = clamp(shockY + localVertical * EXPLOSION_MAX_VERTICAL * scaled * 1.2F, -0.18F, 0.18F);
        shockZ = clamp(shockZ - localForward * EXPLOSION_MAX_FORWARD * scaled * 1.25F, -0.16F, 0.16F);
        shockPitch = clamp(shockPitch + (0.35F + Math.abs(localForward) * 0.65F + Math.max(0.0F, localVertical) * 0.25F) * EXPLOSION_MAX_PITCH * scaled * 1.0F, -8.0F, 8.0F);
        shockRoll = clamp(shockRoll - localRight * EXPLOSION_MAX_ROLL * scaled * 1.2F, -4.5F, 4.5F);
        shockAge = 0.0F;

        explosionVelX += localRight * EXPLOSION_MAX_LATERAL * scaled * 32.0F;
        explosionVelY += localVertical * EXPLOSION_MAX_VERTICAL * scaled * 30.0F;
        explosionVelZ += -localForward * EXPLOSION_MAX_FORWARD * scaled * 32.0F;
        explosionVelPitch += (0.4F + Math.abs(localForward) * 0.6F) * EXPLOSION_MAX_PITCH * sharpness * 12.0F;
        explosionVelRoll += -localRight * EXPLOSION_MAX_ROLL * scaled * 14.0F;
        explosionVelX = clamp(explosionVelX, -3.5F, 3.5F);
        explosionVelY = clamp(explosionVelY, -4.0F, 4.0F);
        explosionVelZ = clamp(explosionVelZ, -3.5F, 3.5F);
        explosionVelPitch = clamp(explosionVelPitch, -85.0F, 85.0F);
        explosionVelRoll = clamp(explosionVelRoll, -60.0F, 60.0F);

        if (Combatives.logger != null && (CombativesConfig.verboseCameraDebug || (CombativesConfig.debugCamera && scaled >= 0.35F))) {
            Combatives.logger.info("Combatives explosion impulse created: response={}, localForward={}, localRight={}, localVertical={}", scaled, localForward, localRight, localVertical);
        }
    }

    private static float smoothstep(float t) { t = clamp(t, 0.0F, 1.0F); return t * t * (3.0F - 2.0F * t); }
    private static float clamp(float v, float min, float max) { return v < min ? min : v > max ? max : v; }
    public void reset() { pitch = roll = vertical = forward = lateral = bobSuppression = explosionX = explosionY = explosionZ = explosionPitch = explosionRoll = explosionVelX = explosionVelY = explosionVelZ = explosionVelPitch = explosionVelRoll = shockX = shockY = shockZ = shockPitch = shockRoll = 0.0F; shockAge = 1.0F; }
    public float getPitch() { return pitch; }
    public float getRoll() { return roll; }
    public float getVertical() { return vertical; }
    public float getForward() { return forward; }
    public float getLateral() { return lateral; }
    public float getBobSuppression() { return bobSuppression; }
}
