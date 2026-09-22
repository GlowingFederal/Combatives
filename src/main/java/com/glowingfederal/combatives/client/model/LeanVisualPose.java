package com.glowingfederal.combatives.client.model;

import com.glowingfederal.combatives.movement.LeanPoseMath;

/** The single player-local visual lean definition consumed by biped render paths. */
public final class LeanVisualPose {
    // Established snapshot values are also gameplay inputs; keep them stable.
    public final float bodyRoll;
    public final float headRoll;
    public final float armRoll;
    public final float legRoll;
    public final float leftLegBrace;
    public final float rightLegBrace;
    public final float legPivotOffset;
    public final float leftLegVisualRoll;
    public final float rightLegVisualRoll;
    public final float hipInset;
    private final float sinRoll;
    private final float cosRoll;

    private LeanVisualPose(float bodyRoll, float headRoll, float armRoll, float legRoll,
            float leftLegBrace, float rightLegBrace, float legPivotOffset) {
        this.bodyRoll = bodyRoll;
        this.headRoll = headRoll;
        this.armRoll = armRoll;
        this.legRoll = legRoll;
        this.leftLegBrace = leftLegBrace;
        this.rightLegBrace = rightLegBrace;
        this.legPivotOffset = legPivotOffset;
        float lean = -bodyRoll / 0.16F;
        // The opposite leg braces; the near leg follows the lean slightly.
        this.leftLegVisualRoll = lean * 0.005F + Math.abs(lean) * 0.035F;
        this.rightLegVisualRoll = lean * 0.005F - Math.abs(lean) * 0.035F;
        this.hipInset = Math.abs(lean) * 0.4F;
        this.sinRoll = (float) Math.sin(bodyRoll);
        this.cosRoll = (float) Math.cos(bodyRoll);
    }

    // Rotate animated attachments about the hip centre, then shift the pelvis
    // slightly toward the supporting side. Coordinates are body-relative.
    public float pivotOffsetX(float x, float y, float pelvisY) {
        return x * (cosRoll - 1.0F) - (y - pelvisY) * sinRoll + legPivotOffset;
    }

    public float pivotOffsetY(float x, float y, float pelvisY) {
        return x * sinRoll + (y - pelvisY) * (cosRoll - 1.0F);
    }

    public static LeanVisualPose fromSemanticLean(float lean) {
        float roll = LeanPoseMath.roll(lean);
        float brace = Math.abs(lean) * 0.01F;
        return new LeanVisualPose(roll, roll, roll, roll * 0.75F,
                brace, -brace, LeanPoseMath.pelvisShiftPixels(lean));
    }
}
