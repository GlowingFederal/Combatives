package com.glowingfederal.combatives.compat.mcheli;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.util.AxisAlignedBB;

/**
 * Optional boundary between MCHeli's composite hit boxes and physical
 * movement collision.  MCHeli deliberately uses the same top-level AABB for
 * damage/ray intersection and movement offset resolution; those operations do
 * not have equivalent semantics for ordinary aircraft.
 */
public final class MCHeliCollisionCompat {
    private static final String VEHICLE_BOX = "mcheli.aircraft.MCH_BaseVehicleBoundingBox";
    private static final String SHIP = "mcheli.ship.MCH_EntityShip";
    private static final Map<Class<?>, VehicleBoxAccess> ACCESS_BY_CLASS =
            new ConcurrentHashMap<Class<?>, VehicleBoxAccess>();

    private MCHeliCollisionCompat() { }

    /** Returns true when any entry is a physical obstacle for pose expansion. */
    public static boolean hasSolidCollision(List<?> collisions) {
        for (Object collision : collisions) {
            if (!(collision instanceof AxisAlignedBB) || isSolid((AxisAlignedBB) collision)) {
                return true;
            }
        }
        return false;
    }

    /** Block and unrelated entity boxes remain solid; only known MCHeli boxes are classified. */
    public static boolean isSolid(AxisAlignedBB box) {
        Class<?> boxClass = box.getClass();
        if (!hasClassName(boxClass, VEHICLE_BOX)) {
            return true;
        }

        VehicleBoxAccess access = ACCESS_BY_CLASS.get(boxClass);
        if (access == null) {
            access = createAccess(boxClass);
            ACCESS_BY_CLASS.put(boxClass, access);
        }
        return access.isSolid(box);
    }

    private static VehicleBoxAccess createAccess(Class<?> boxClass) {
        Class<?> cursor = boxClass;
        while (cursor != null) {
            try {
                Field vehicle = cursor.getDeclaredField("ac");
                vehicle.setAccessible(true);
                return new VehicleBoxAccess(vehicle);
            } catch (NoSuchFieldException ignored) {
                cursor = cursor.getSuperclass();
            } catch (SecurityException denied) {
                return VehicleBoxAccess.FAIL_CLOSED;
            }
        }
        return VehicleBoxAccess.FAIL_CLOSED;
    }

    private static boolean hasClassName(Class<?> type, String name) {
        for (Class<?> cursor = type; cursor != null; cursor = cursor.getSuperclass()) {
            if (name.equals(cursor.getName())) {
                return true;
            }
        }
        return false;
    }

    private static final class VehicleBoxAccess {
        private static final VehicleBoxAccess FAIL_CLOSED = new VehicleBoxAccess(null);
        private final Field vehicle;

        private VehicleBoxAccess(Field vehicle) {
            this.vehicle = vehicle;
        }

        private boolean isSolid(AxisAlignedBB box) {
            if (this.vehicle == null) {
                return true;
            }
            try {
                Object owner = this.vehicle.get(box);
                return owner == null || hasClassName(owner.getClass(), SHIP);
            } catch (IllegalAccessException inaccessible) {
                return true;
            }
        }
    }
}
