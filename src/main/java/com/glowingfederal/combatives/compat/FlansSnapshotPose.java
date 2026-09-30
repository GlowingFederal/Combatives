package com.glowingfederal.combatives.compat;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.client.model.LeanVisualPose;
import com.glowingfederal.combatives.movement.LeanGeometry;
import com.glowingfederal.combatives.movement.PlayerLocalBasis;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;

/** Optional structural bridge. LeanVisualPose is pure data and has no client class dependencies. */
public final class FlansSnapshotPose {
    private static boolean failed;
    private static Field position, hitboxes, axes, pivot, type, x, y, z;
    private static Constructor<?> vector;
    private static Method cloneAxes, rotateGlobal;

    private FlansSnapshotPose() { }

    public static void apply(Object snapshot, EntityPlayer player) {
        if (failed) return;
        try {
            if (position == null) initialize(snapshot.getClass());
            Object pos = position.get(snapshot);
            // Flan's local-client-only posY correction is not a physical floor definition.
            y.setFloat(pos, (float) player.boundingBox.minY);
            LeanVisualPose pose = LeanVisualPose.fromSemanticLean(LeanGeometry.acceptedLean(player));
            PlayerLocalBasis basis = PlayerLocalBasis.fromYaw(player.rotationYaw);
            Object forward = vector.newInstance((float) basis.forwardX, 0.0F, (float) basis.forwardZ);
            for (Object box : (List<?>) hitboxes.get(snapshot)) {
                String part = ((Enum<?>) type.get(box)).name();
                float roll;
                if ("HEAD".equals(part)) roll = pose.headRoll;
                else if ("BODY".equals(part)) roll = pose.bodyRoll;
                else if ("LEGS".equals(part)) roll = pose.legRoll;
                else roll = pose.armRoll; // Arms and the shields attached to them.
                // Vanilla model +Z maps to world -forward (model Y points down).
                Object rotated = cloneAxes.invoke(axes.get(box));
                rotateGlobal.invoke(rotated, (float) -Math.toDegrees(roll), forward);
                axes.set(box, rotated); // Never rotate shared bodyAxes in place.
                if ("LEGS".equals(part)) {
                    Object point = pivot.get(box);
                    x.setFloat(point, x.getFloat(point) - (float) basis.rightX * pose.legPivotOffset / 16.0F);
                    z.setFloat(point, z.getFloat(point) - (float) basis.rightZ * pose.legPivotOffset / 16.0F);
                }
            }
        } catch (ReflectiveOperationException | ClassCastException ex) {
            failed = true;
            if (Combatives.logger != null) Combatives.logger.warn("Flan's snapshot layout is unsupported; disabling lean snapshot adapter", ex);
        }
    }

    private static void initialize(Class<?> snapshot) throws ReflectiveOperationException {
        Field positionField = snapshot.getField("pos");
        hitboxes = snapshot.getField("hitboxes");
        ClassLoader loader = snapshot.getClassLoader();
        Class<?> box = Class.forName("com.flansmod.common.guns.raytracing.PlayerHitbox", false, loader);
        axes = box.getField("axes");
        pivot = box.getField("rP");
        type = box.getField("type");
        Class<?> vec = positionField.getType();
        vector = vec.getConstructor(float.class, float.class, float.class);
        x = vec.getField("x"); y = vec.getField("y"); z = vec.getField("z");
        cloneAxes = axes.getType().getMethod("clone");
        rotateGlobal = axes.getType().getMethod("rotateGlobal", float.class, vec);
        position = positionField;
    }
}
