package com.glowingfederal.combatives.combat;

import net.minecraft.util.Vec3;

/** Immutable oriented box. Its axes are unit world vectors and its bounds are local. */
public final class CombatHitVolume {
    public final CombatHitPart part;
    public final Vec3 center;
    public final Vec3 axisX;
    public final Vec3 axisY;
    public final Vec3 axisZ;
    public final double halfX;
    public final double halfY;
    public final double halfZ;

    CombatHitVolume(CombatHitPart part, Transform transform,
            double halfX, double halfY, double halfZ) {
        this.part = part;
        this.center = transform.point(0.0D, 0.0D, 0.0D);
        this.axisX = transform.vector(1.0D, 0.0D, 0.0D);
        this.axisY = transform.vector(0.0D, 1.0D, 0.0D);
        this.axisZ = transform.vector(0.0D, 0.0D, 1.0D);
        this.halfX = halfX;
        this.halfY = halfY;
        this.halfZ = halfZ;
    }

    public boolean contains(Vec3 worldPoint, double inflation) {
        Vec3 local = this.toLocal(worldPoint);
        return Math.abs(local.xCoord) <= this.halfX + inflation
                && Math.abs(local.yCoord) <= this.halfY + inflation
                && Math.abs(local.zCoord) <= this.halfZ + inflation;
    }

    public CombatHitResult intersect(Vec3 start, Vec3 end, double inflation) {
        Vec3 localStart = this.toLocal(start);
        Vec3 worldMotion = Vec3.createVectorHelper(end.xCoord - start.xCoord,
                end.yCoord - start.yCoord, end.zCoord - start.zCoord);
        double dx = dot(worldMotion, this.axisX);
        double dy = dot(worldMotion, this.axisY);
        double dz = dot(worldMotion, this.axisZ);
        double[] interval = new double[] { 0.0D, 1.0D };
        if (!clip(localStart.xCoord, dx, this.halfX + inflation, interval)
                || !clip(localStart.yCoord, dy, this.halfY + inflation, interval)
                || !clip(localStart.zCoord, dz, this.halfZ + inflation, interval)) {
            return null;
        }
        double t = interval[0];
        Vec3 hit = start.addVector(worldMotion.xCoord * t, worldMotion.yCoord * t, worldMotion.zCoord * t);
        return new CombatHitResult(this.part, hit, t);
    }

    private Vec3 toLocal(Vec3 point) {
        Vec3 offset = Vec3.createVectorHelper(point.xCoord - this.center.xCoord,
                point.yCoord - this.center.yCoord, point.zCoord - this.center.zCoord);
        return Vec3.createVectorHelper(dot(offset, this.axisX), dot(offset, this.axisY), dot(offset, this.axisZ));
    }

    private static boolean clip(double origin, double motion, double half, double[] interval) {
        if (Math.abs(motion) < 1.0E-12D) return origin >= -half && origin <= half;
        double a = (-half - origin) / motion;
        double b = (half - origin) / motion;
        if (a > b) {
            double swap = a;
            a = b;
            b = swap;
        }
        if (a > interval[0]) interval[0] = a;
        if (b < interval[1]) interval[1] = b;
        return interval[0] <= interval[1] && interval[1] >= 0.0D && interval[0] <= 1.0D;
    }

    private static double dot(Vec3 left, Vec3 right) {
        return left.xCoord * right.xCoord + left.yCoord * right.yCoord + left.zCoord * right.zCoord;
    }
}
