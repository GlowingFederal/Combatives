package com.glowingfederal.combatives.compat;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.combat.CombatHitPart;
import com.glowingfederal.combatives.combat.CombatHitVolume;
import com.glowingfederal.combatives.combat.CombatHitVolumes;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

/** Optional structural bridge that gives Flan's snapshot the shared combat OBBs. */
public final class FlansSnapshotPose {
    private static boolean failed;
    private static Field position, hitboxes, axes, pivot, origin, dimensions, type, x, y, z;
    private static Constructor<?> vector, matrix, rotatedAxes;
    private static Field m00, m01, m02, m10, m11, m12, m20, m21, m22;

    private FlansSnapshotPose() { }

    public static void apply(Object snapshot, EntityPlayer player) {
        if (failed) return;
        try {
            if (position == null) initialize(snapshot.getClass());
            Object pos = position.get(snapshot);
            x.setFloat(pos, (float) player.posX);
            y.setFloat(pos, (float) player.boundingBox.minY);
            z.setFloat(pos, (float) player.posZ);

            List<?> boxes = (List<?>) hitboxes.get(snapshot);
            CombatHitVolume head = null, torso = null, core = null;
            for (CombatHitVolume volume : CombatHitVolumes.volumes(player)) {
                if (volume.part == CombatHitPart.HEAD) head = volume;
                else if (volume.part == CombatHitPart.TORSO) torso = volume;
                else if (volume.part == CombatHitPart.CORE) core = volume;
            }

            for (Iterator<?> iterator = boxes.iterator(); iterator.hasNext();) {
                Object box = iterator.next();
                String part = ((Enum<?>) type.get(box)).name();
                CombatHitVolume volume = "HEAD".equals(part) ? head
                        : "BODY".equals(part) ? torso : "LEGS".equals(part) ? core : null;
                if (volume != null) {
                    applyVolume(box, volume, player);
                } else if ("LEFTARM".equals(part) || "RIGHTARM".equals(part)) {
                    // Animated arm matrices are client-model state and cannot be reconstructed
                    // authoritatively in a historical server snapshot. Avoid phantom upright arms.
                    iterator.remove();
                }
            }
        } catch (ReflectiveOperationException | ClassCastException ex) {
            failed = true;
            if (Combatives.logger != null) Combatives.logger.warn(
                    "Flan's snapshot layout is unsupported; disabling pose combat-volume adapter", ex);
        }
    }

    private static void applyVolume(Object box, CombatHitVolume volume, EntityPlayer player)
            throws ReflectiveOperationException {
        axes.set(box, createAxes(volume));
        pivot.set(box, newVector((float) (volume.center.xCoord - player.posX),
                (float) (volume.center.yCoord - player.boundingBox.minY),
                (float) (volume.center.zCoord - player.posZ)));
        origin.set(box, newVector((float) -volume.halfX, (float) -volume.halfY, (float) -volume.halfZ));
        dimensions.set(box, newVector((float) (volume.halfX * 2.0D),
                (float) (volume.halfY * 2.0D), (float) (volume.halfZ * 2.0D)));
    }

    private static Object createAxes(CombatHitVolume volume) throws ReflectiveOperationException {
        Vec3 axisZ = volume.axisZ;
        if (determinant(volume.axisX, volume.axisY, axisZ) < 0.0D) {
            axisZ = Vec3.createVectorHelper(-axisZ.xCoord, -axisZ.yCoord, -axisZ.zCoord);
        }
        Object value = matrix.newInstance();
        setColumn(value, 0, volume.axisX);
        setColumn(value, 1, volume.axisY);
        setColumn(value, 2, axisZ);
        return rotatedAxes.newInstance(value);
    }

    private static void setColumn(Object value, int column, Vec3 axis) throws IllegalAccessException {
        Field a = column == 0 ? m00 : column == 1 ? m01 : m02;
        Field b = column == 0 ? m10 : column == 1 ? m11 : m12;
        Field c = column == 0 ? m20 : column == 1 ? m21 : m22;
        a.setFloat(value, (float) axis.xCoord);
        b.setFloat(value, (float) axis.yCoord);
        c.setFloat(value, (float) axis.zCoord);
    }

    private static double determinant(Vec3 a, Vec3 b, Vec3 c) {
        return a.xCoord * (b.yCoord * c.zCoord - b.zCoord * c.yCoord)
                - b.xCoord * (a.yCoord * c.zCoord - a.zCoord * c.yCoord)
                + c.xCoord * (a.yCoord * b.zCoord - a.zCoord * b.yCoord);
    }

    private static Object newVector(float vx, float vy, float vz) throws ReflectiveOperationException {
        return vector.newInstance(vx, vy, vz);
    }

    private static void initialize(Class<?> snapshot) throws ReflectiveOperationException {
        position = snapshot.getField("pos");
        hitboxes = snapshot.getField("hitboxes");
        ClassLoader loader = snapshot.getClassLoader();
        Class<?> box = Class.forName("com.flansmod.common.guns.raytracing.PlayerHitbox", false, loader);
        axes = box.getField("axes");
        pivot = box.getField("rP");
        origin = box.getField("o");
        dimensions = box.getField("d");
        type = box.getField("type");
        Class<?> vec = position.getType();
        vector = vec.getConstructor(float.class, float.class, float.class);
        x = vec.getField("x"); y = vec.getField("y"); z = vec.getField("z");

        Class<?> axesClass = axes.getType();
        Class<?> matrixClass = Class.forName("com.flansmod.common.vector.Matrix4f", false, loader);
        matrix = matrixClass.getConstructor();
        rotatedAxes = axesClass.getConstructor(matrixClass);
        m00 = matrixClass.getField("m00"); m01 = matrixClass.getField("m01"); m02 = matrixClass.getField("m02");
        m10 = matrixClass.getField("m10"); m11 = matrixClass.getField("m11"); m12 = matrixClass.getField("m12");
        m20 = matrixClass.getField("m20"); m21 = matrixClass.getField("m21"); m22 = matrixClass.getField("m22");
    }
}
