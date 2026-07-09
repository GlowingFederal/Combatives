package com.glowingfederal.combatives.client.camera;

public final class BobController {
    private float phase, vertical, sway, pitch, roll;
    public void update(MovementCameraState state) {
        float speed = state.getSpeed();
        phase += 0.35F + Math.min(speed * 9.0F, 0.9F);
        float intensity = state.isSwimming() ? 0.35F : state.isCrawling() || state.isSneaking() ? 0.42F : state.isSprinting() ? 1.2F : 0.75F;
        float amount = Math.min(speed * 4.0F, 1.0F) * intensity;
        vertical = (float) Math.sin(phase) * 0.018F * amount;
        sway = (float) Math.cos(phase * 0.5F) * 0.010F * amount;
        pitch = (float) Math.sin(phase) * 0.22F * amount;
        roll = (float) Math.cos(phase * 0.5F) * 0.28F * amount;
    }
    public void reset() { phase = vertical = sway = pitch = roll = 0.0F; }
    public float getVertical() { return vertical; }
    public float getSway() { return sway; }
    public float getPitch() { return pitch; }
    public float getRoll() { return roll; }
}
