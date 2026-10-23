package com.glowingfederal.combatives.movement;

import com.glowingfederal.combatives.config.AuthoritativeGameplaySettings;
import com.glowingfederal.combatives.interaction.InteractionRay;
import net.minecraft.init.Blocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/** Invoke on the owning thread of a disposable flat-world player; uses Minecraft's real voxel traversal. */
public final class LeanGeometryRuntimeTest {
    public static String verify(EntityPlayer player) {
        ICombativesLocomotion state = (ICombativesLocomotion) player;
        double x = player.posX, y = player.posY, z = player.posZ;
        double lastX = player.lastTickPosX, lastY = player.lastTickPosY, lastZ = player.lastTickPosZ;
        float yaw = player.rotationYaw, previousYaw = player.prevRotationYaw, lean = state.getLean();
        double max = AuthoritativeGameplaySettings.getMaxLeanDistance(player);
        if (max <= 0.2D || !AuthoritativeGameplaySettings.isLeaningEnabled(player))
            throw new AssertionError("Test requires leaning enabled and maxLeanDistance > 0.2");
        int cases = 0;
        try {
            // Cross voxel boundaries in both axes, including negative coordinates.
            for (double px : new double[] {-0.99D, -0.5D, -0.01D, 0.01D, 0.5D, 0.99D}) {
                for (double pz : new double[] {-0.99D, -0.5D, -0.01D, 0.01D, 0.5D, 0.99D}) {
                    for (float facing = -180; facing <= 180; facing += 5) {
                        for (float side : new float[] {-1, 1}) {
                            checkOpen(player, state, px, y + 8, pz, facing, side, max);
                            cases++;
                        }
                    }
                    for (float facing : new float[] {-135.01F, -134.99F, -45.01F, -44.99F}) {
                        for (float side : new float[] {-1, 1}) {
                            checkOpen(player, state, px, y + 8, pz, facing, side, max);
                            cases++;
                        }
                    }
                }
            }
            // A real stone wall 0.2 blocks away must retain the existing 0.05-block margin.
            for (float facing : new float[] {-90, 90}) {
                double pz = Math.floor(z) + (facing == -90 ? 0.8D : 0.2D);
                position(player, Math.floor(x) + 0.5D, y + 8, pz, facing);
                state.setLean(1);
                InteractionRay base = InteractionRay.interpolatedBase(player, 1);
                int bx = MathHelper.floor_double(player.posX), by = MathHelper.floor_double(base.origin.yCoord);
                int bz = MathHelper.floor_double(pz) + (facing == -90 ? 1 : -1);
                if (player.worldObj.getBlock(bx, by, bz) != Blocks.air)
                    throw new AssertionError("Wall fixture requires empty sky in a disposable world");
                player.worldObj.setBlock(bx, by, bz, Blocks.stone, 0, 2);
                try {
                    double startZ = base.origin.zCoord;
                    Vec3 offset = LeanGeometry.legalOffset(player, base, 1, facing);
                    double expected = facing == -90 ? bz - startZ - 0.05D : startZ - (bz + 1) - 0.05D;
                    near(offset.xCoord, 0);
                    near(offset.zCoord, Math.copySign(expected, facing == -90 ? 1 : -1));
                    near(base.origin.zCoord, startZ);
                    near(LeanGeometry.acceptedLean(player), expected / max);
                } finally {
                    player.worldObj.setBlock(bx, by, bz, Blocks.air, 0, 2);
                }
            }
            return "PASS: " + cases + " native open-air lean cases, repeated/interpolated rays, NE/SE boundaries,"
                    + " negative coordinates and east/west stone-wall margins";
        } finally {
            player.setPosition(x, y, z);
            player.lastTickPosX = lastX; player.lastTickPosY = lastY; player.lastTickPosZ = lastZ;
            player.rotationYaw = yaw; player.prevRotationYaw = previousYaw;
            state.setLean(lean);
        }
    }

    private static void checkOpen(EntityPlayer player, ICombativesLocomotion state,
            double x, double y, double z, float yaw, float lean, double max) {
        position(player, x, y, z, yaw);
        state.setLean(lean);
        Vec3 expected = PlayerLocalBasis.fromYaw(yaw).lateralOffset(max * lean);
        InteractionRay actual = InteractionRay.authoritative(player);
        near(actual.origin.xCoord, x + expected.xCoord);
        near(actual.origin.zCoord, z + expected.zCoord);
        near(LeanGeometry.acceptedLean(player), lean);
        for (float partial : new float[] {0, 0.5F, 1}) {
            InteractionRay base = InteractionRay.interpolatedBase(player, partial);
            double eyeY = base.origin.yCoord;
            for (int repeat = 0; repeat < 2; repeat++) {
                Vec3 offset = LeanGeometry.legalOffset(player, base, lean, yaw);
                near(offset.xCoord, expected.xCoord); near(offset.zCoord, expected.zCoord);
                near(base.origin.xCoord, x); near(base.origin.yCoord, eyeY); near(base.origin.zCoord, z);
            }
            actual = InteractionRay.interpolated(player, partial);
            near(actual.origin.xCoord, x + expected.xCoord);
            near(actual.origin.zCoord, z + expected.zCoord);
        }
    }

    private static void position(EntityPlayer player, double x, double y, double z, float yaw) {
        player.setPosition(x, y, z);
        player.lastTickPosX = x; player.lastTickPosY = y; player.lastTickPosZ = z;
        player.rotationYaw = yaw; player.prevRotationYaw = yaw;
    }

    private static void near(double actual, double expected) {
        if (!Double.isFinite(actual) || Math.abs(actual - expected) > 1.0E-6D)
            throw new AssertionError("Expected " + expected + ", got " + actual);
    }
}
