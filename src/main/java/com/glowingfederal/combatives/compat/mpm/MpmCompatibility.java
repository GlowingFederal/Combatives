package com.glowingfederal.combatives.compat.mpm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.glowingfederal.combatives.config.CombativesConfig;
import cpw.mods.fml.common.Loader;
import net.minecraft.entity.player.EntityPlayer;

/** Optional, common-side boundary around MPM+. No MPM type appears in a signature. */
public final class MpmCompatibility {
    public static final int DEFAULT_RAW_SIZE = 5;
    private static final String MOD_ID = "moreplayermodels";
    private static boolean initialized;
    private static boolean available;
    private static Method getData;
    private static Field size;

    private MpmCompatibility() { }

    public static Scale resolve(EntityPlayer player) {
        if (!CombativesConfig.enableMpmHitboxScaling || player == null || !isAvailable()) {
            return Scale.DEFAULT;
        }
        try {
            Object data = getData.invoke(null, player);
            if (data == null) return Scale.DEFAULT;
            int raw = size.getInt(data);
            // MPM's NBT reader and /size command define 1..10; its renderer uses size / 5 uniformly.
            if (raw < 1 || raw > 10) return new Scale(raw, 1.0F, false);
            float resolved = raw / (float) DEFAULT_RAW_SIZE;
            return Float.isNaN(resolved) || Float.isInfinite(resolved) || resolved <= 0.0F
                    ? new Scale(raw, 1.0F, false) : new Scale(raw, resolved, true);
        } catch (Throwable ignored) {
            // Optional compatibility must degrade normally if an incompatible MPM revision is present.
            return Scale.DEFAULT;
        }
    }

    private static synchronized boolean isAvailable() {
        if (initialized) return available;
        initialized = true;
        if (!Loader.isModLoaded(MOD_ID)) return false;
        try {
            Class<?> modelData = Class.forName("noppes.mpm.ModelData", false,
                    MpmCompatibility.class.getClassLoader());
            getData = modelData.getMethod("getData", EntityPlayer.class);
            size = modelData.getField("size");
            available = true;
        } catch (Throwable ignored) {
            available = false;
        }
        return available;
    }

    public static final class Scale {
        public static final Scale DEFAULT = new Scale(DEFAULT_RAW_SIZE, 1.0F, false);
        public final int rawSize;
        public final float value;
        public final boolean fromMpm;

        private Scale(int rawSize, float value, boolean fromMpm) {
            this.rawSize = rawSize;
            this.value = value;
            this.fromMpm = fromMpm;
        }
    }
}
