package com.glowingfederal.combatives.combat;

import net.minecraft.util.Vec3;

public final class CombatHitResult {
    public final CombatHitPart part;
    public final Vec3 hitVec;
    /** Parametric distance along the supplied segment, in the inclusive range [0, 1]. */
    public final double fraction;

    CombatHitResult(CombatHitPart part, Vec3 hitVec, double fraction) {
        this.part = part;
        this.hitVec = hitVec;
        this.fraction = fraction;
    }
}
