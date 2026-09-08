package com.glowingfederal.combatives.movement;

import net.minecraft.util.Vec3;

/** Standalone regression checks, runnable without launching Minecraft or adding a test dependency. */
public final class PlayerLocalBasisTest {
    public static void main(String[] args) {
        double q = Math.sqrt(0.5D);
        // Expected world direction of LOCAL RIGHT, ordered N,S,E,W,NE,SE,SW,NW.
        double[][] headings = {{180,1,0}, {0,-1,0}, {-90,0,1}, {90,0,-1},
                {-135,q,q}, {-45,-q,q}, {45,-q,-q}, {135,q,-q}};
        for (double[] h : headings) {
            PlayerLocalBasis basis = PlayerLocalBasis.fromYaw((float) h[0]);
            Vec3 right = basis.lateralOffset(1.0D);
            Vec3 left = basis.lateralOffset(-1.0D);
            near(right.xCoord, h[1]); near(right.zCoord, h[2]);
            near(left.xCoord, -h[1]); near(left.zCoord, -h[2]);
        }
        int count = 0;
        for (float yaw = -720.0F; yaw <= 720.0F; yaw += 0.25F) {
            PlayerLocalBasis basis = PlayerLocalBasis.fromYaw(yaw);
            near(basis.forwardX * basis.rightX + basis.forwardZ * basis.rightZ, 0);
            near(basis.rightX * basis.rightX + basis.rightZ * basis.rightZ, 1);
            // right = forward cross world-up, independently fixing handedness.
            near(basis.rightX, -basis.forwardZ); near(basis.rightZ, basis.forwardX);
            for (double distance : new double[] {-0.5D, -0.05D, 0.0D, 0.05D, 0.5D}) {
                Vec3 offset = basis.lateralOffset(distance);
                near(basis.projectRight(offset.xCoord, offset.zCoord), distance);
                // Vanilla view rotation is yaw+180. At camera TAIL the inverse
                // WORLD offset must land on view X=-distance and view Z=0.
                double viewYaw = Math.toRadians(yaw + 180.0D);
                double tx = -offset.xCoord, tz = -offset.zCoord;
                near(Math.cos(viewYaw) * tx + Math.sin(viewYaw) * tz, -distance);
                near(-Math.sin(viewYaw) * tx + Math.cos(viewYaw) * tz, 0);
            }
            count++;
        }
        near(PlayerLocalBasis.interpolateYaw(179, -179, 0.5F), 180);
        near(PlayerLocalBasis.interpolateYaw(-179, 179, 0.5F), -180);
        near(PlayerLocalBasis.interpolateYaw(359, 1, 0.5F), 360);
        System.out.println("PASS: 8 headings, both sides; " + count
                + " yaw samples, orthogonality/handedness, 5 distances, inverse camera transform, wrap seams.");
    }

    private static void near(double actual, double expected) {
        if (Math.abs(actual - expected) > 1.0E-9D) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }
}
