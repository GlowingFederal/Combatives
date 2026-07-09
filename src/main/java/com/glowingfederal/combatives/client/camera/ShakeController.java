package com.glowingfederal.combatives.client.camera;

public final class ShakeController {
    private float pitch, roll, vertical, velocity;
    public void update(MovementCameraState state) {
        velocity *= 0.82F; vertical = velocity * 0.018F; pitch = velocity * 0.35F; roll = velocity * -0.18F;
        if (Math.abs(velocity) < 0.001F) velocity = 0.0F;
    }
    public void addLandingImpulse(float strength) { velocity += clamp(strength, 0.0F, 1.0F); }
    public void addDamageImpulse(float strength) { velocity += clamp(strength, 0.0F, 1.0F); }
    public void addExplosionImpulse(float strength) { velocity += clamp(strength, 0.0F, 1.0F); }
    private static float clamp(float v, float min, float max) { return v < min ? min : v > max ? max : v; }
    public void reset() { pitch = roll = vertical = velocity = 0.0F; }
    public float getPitch() { return pitch; }
    public float getRoll() { return roll; }
    public float getVertical() { return vertical; }
}
