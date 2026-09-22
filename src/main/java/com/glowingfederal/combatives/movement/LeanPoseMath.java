package com.glowingfederal.combatives.movement;

/** Shared model-space lean constants used by rendering and combat geometry. */
public final class LeanPoseMath {
    public static final float ROLL_RADIANS_PER_LEAN = -0.16F;
    public static final float PELVIS_SHIFT_PIXELS_PER_LEAN = 0.45F;

    private LeanPoseMath() { }

    public static float roll(float semanticLean) {
        return semanticLean * ROLL_RADIANS_PER_LEAN;
    }

    public static float pelvisShiftPixels(float semanticLean) {
        return semanticLean * PELVIS_SHIFT_PIXELS_PER_LEAN;
    }
}
