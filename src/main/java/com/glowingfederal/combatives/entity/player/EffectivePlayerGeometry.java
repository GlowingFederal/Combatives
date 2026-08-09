package com.glowingfederal.combatives.entity.player;

import com.glowingfederal.combatives.entity.Pose;
import net.minecraft.util.AxisAlignedBB;

/** Immutable gameplay geometry.  Visual model scale is deliberately absent. */
public final class EffectivePlayerGeometry {
    public final Pose pose;
    public final float width;
    public final float height;
    public final float eyeAboveMinY;

    public EffectivePlayerGeometry(Pose pose, float width, float height, float eyeAboveMinY) {
        this.pose = pose;
        this.width = width;
        this.height = height;
        this.eyeAboveMinY = eyeAboveMinY;
    }

    public AxisAlignedBB clearanceBox(double centerX, double minY, double centerZ) {
        double halfWidth = this.width / 2.0D;
        return AxisAlignedBB.getBoundingBox(centerX - halfWidth, minY, centerZ - halfWidth,
                centerX + halfWidth, minY + this.height, centerZ + halfWidth);
    }
}
