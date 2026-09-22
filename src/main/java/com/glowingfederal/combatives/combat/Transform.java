package com.glowingfederal.combatives.combat;

import net.minecraft.util.Vec3;

/** Small rigid-transform implementation; multiplication follows OpenGL call order. */
final class Transform {
    private final double m00, m01, m02, m10, m11, m12, m20, m21, m22;
    private final double tx, ty, tz;

    private Transform(double m00, double m01, double m02, double m10, double m11, double m12,
            double m20, double m21, double m22, double tx, double ty, double tz) {
        this.m00 = m00; this.m01 = m01; this.m02 = m02;
        this.m10 = m10; this.m11 = m11; this.m12 = m12;
        this.m20 = m20; this.m21 = m21; this.m22 = m22;
        this.tx = tx; this.ty = ty; this.tz = tz;
    }

    static Transform identity() {
        return new Transform(1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0);
    }

    static Transform basis(Vec3 x, Vec3 y, Vec3 z, double tx, double ty, double tz) {
        return new Transform(x.xCoord, y.xCoord, z.xCoord, x.yCoord, y.yCoord, z.yCoord,
                x.zCoord, y.zCoord, z.zCoord, tx, ty, tz);
    }

    static Transform translation(double x, double y, double z) {
        return new Transform(1, 0, 0, 0, 1, 0, 0, 0, 1, x, y, z);
    }

    static Transform rotateX(double angle) {
        double sin = Math.sin(angle), cos = Math.cos(angle);
        return new Transform(1, 0, 0, 0, cos, -sin, 0, sin, cos, 0, 0, 0);
    }

    static Transform rotateY(double angle) {
        double sin = Math.sin(angle), cos = Math.cos(angle);
        return new Transform(cos, 0, sin, 0, 1, 0, -sin, 0, cos, 0, 0, 0);
    }

    static Transform rotateZ(double angle) {
        double sin = Math.sin(angle), cos = Math.cos(angle);
        return new Transform(cos, -sin, 0, sin, cos, 0, 0, 0, 1, 0, 0, 0);
    }

    Transform then(Transform child) {
        return new Transform(
            m00 * child.m00 + m01 * child.m10 + m02 * child.m20,
            m00 * child.m01 + m01 * child.m11 + m02 * child.m21,
            m00 * child.m02 + m01 * child.m12 + m02 * child.m22,
            m10 * child.m00 + m11 * child.m10 + m12 * child.m20,
            m10 * child.m01 + m11 * child.m11 + m12 * child.m21,
            m10 * child.m02 + m11 * child.m12 + m12 * child.m22,
            m20 * child.m00 + m21 * child.m10 + m22 * child.m20,
            m20 * child.m01 + m21 * child.m11 + m22 * child.m21,
            m20 * child.m02 + m21 * child.m12 + m22 * child.m22,
            m00 * child.tx + m01 * child.ty + m02 * child.tz + tx,
            m10 * child.tx + m11 * child.ty + m12 * child.tz + ty,
            m20 * child.tx + m21 * child.ty + m22 * child.tz + tz);
    }

    Vec3 point(double x, double y, double z) {
        return Vec3.createVectorHelper(m00 * x + m01 * y + m02 * z + tx,
                m10 * x + m11 * y + m12 * z + ty,
                m20 * x + m21 * y + m22 * z + tz);
    }

    Vec3 vector(double x, double y, double z) {
        return Vec3.createVectorHelper(m00 * x + m01 * y + m02 * z,
                m10 * x + m11 * y + m12 * z,
                m20 * x + m21 * y + m22 * z);
    }
}
