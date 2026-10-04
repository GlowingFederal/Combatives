package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.config.CameraVisualSetting;
import cpw.mods.fml.common.Loader;
import java.lang.reflect.Field;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;

/** Read-only optional ADS boundary. Never changes a weapon's aim, zoom or animation. */
public final class WeaponCameraPresentation {
    private static boolean resolved;
    private static Class<?> hmgGun, flansGun;
    private static Field hmgAds, flansScope;
    private static float previousAds, ads;
    private WeaponCameraPresentation() { }

    public static void tick(EntityPlayerSP player) {
        if (!resolved) {
            resolved = true;
            if (Loader.isModLoaded("HandmadeGuns")) {
                try {
                    hmgGun = Class.forName("handmadeguns.items.guns.HMGItem_Unified_Guns");
                    hmgAds = Class.forName("handmadeguns.client.render.HMGRenderItemGun_U_NEW").getField("firstPerson_ADSState");
                } catch (ReflectiveOperationException e) { hmgAds = null; }
            }
            if (Loader.isModLoaded("flansmod")) {
                try {
                    flansGun = Class.forName("com.flansmod.common.guns.ItemGun");
                    flansScope = Class.forName("com.flansmod.client.FlansModClient").getField("currentScope");
                } catch (ReflectiveOperationException e) { flansScope = null; }
            }
        }
        boolean aiming = false;
        ItemStack held = player.getHeldItem();
        if (held != null) {
            try {
                aiming = hmgAds != null && hmgGun.isInstance(held.getItem()) && hmgAds.getBoolean(null)
                        || flansScope != null && flansGun.isInstance(held.getItem()) && flansScope.get(null) != null;
            } catch (IllegalAccessException e) { hmgAds = flansScope = null; }
        }
        previousAds = ads;
        ads += ((aiming ? 1F : 0F) - ads) * 0.5F;
    }

    public static float ambientScale(float partialTicks) {
        float renderAds = previousAds + (ads - previousAds) * Math.max(0, Math.min(1, partialTicks));
        return 1F + (CameraVisualSetting.ADS.get() - 1F) * renderAds;
    }
    public static void reset() { previousAds = ads = 0; }
}
