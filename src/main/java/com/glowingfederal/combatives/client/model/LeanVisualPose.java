package com.glowingfederal.combatives.client.model;

/** The single player-local visual lean definition consumed by biped render paths. */
public final class LeanVisualPose {
    public final float bodyRoll;
    public final float headRoll;
    public final float armRoll;
    public final float legRoll;
    public final float leftLegBrace;
    public final float rightLegBrace;
    public final float legPivotOffset;

    private LeanVisualPose(float bodyRoll, float headRoll, float armRoll, float legRoll,
            float leftLegBrace, float rightLegBrace, float legPivotOffset) {
        this.bodyRoll = bodyRoll;
        this.headRoll = headRoll;
        this.armRoll = armRoll;
        this.legRoll = legRoll;
        this.leftLegBrace = leftLegBrace;
        this.rightLegBrace = rightLegBrace;
        this.legPivotOffset = legPivotOffset;
    }

    public static LeanVisualPose fromSemanticLean(float lean) {
        float roll = -lean * 0.16F;
        float brace = Math.abs(lean) * 0.01F;
        return new LeanVisualPose(roll, roll, roll, roll * 0.75F,
                brace, -brace, lean * 0.45F);
    }
}
