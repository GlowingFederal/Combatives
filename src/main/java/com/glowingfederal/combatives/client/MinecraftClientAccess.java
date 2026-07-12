package com.glowingfederal.combatives.client;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;

public final class MinecraftClientAccess {
    private static Method getMinecraftMethod;
    private static Field playerField;
    private static Field worldField;
    private static Field renderViewEntityField;
    private static Field gameSettingsField;
    private static Field sprintKeyBindingField;

    private MinecraftClientAccess() {
    }

    public static Minecraft getMinecraft() {
        Method method = getGetMinecraftMethod();
        try {
            return (Minecraft) method.invoke(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access Minecraft singleton method", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Minecraft singleton method failed", cause);
        }
    }

    public static EntityPlayerSP getPlayer() {
        return (EntityPlayerSP) getFieldValue(getMinecraft(), getPlayerField());
    }

    public static WorldClient getWorld() {
        return (WorldClient) getFieldValue(getMinecraft(), getWorldField());
    }

    public static Entity getRenderViewEntity() {
        return (Entity) getFieldValue(getMinecraft(), getRenderViewEntityField());
    }

    public static GameSettings getGameSettings() {
        return (GameSettings) getFieldValue(getMinecraft(), getGameSettingsField());
    }

    public static KeyBinding getSprintKeyBinding() {
        GameSettings settings = getGameSettings();
        return settings == null ? null : (KeyBinding) getFieldValue(settings, getSprintKeyBindingField());
    }

    private static Method getGetMinecraftMethod() {
        if (getMinecraftMethod != null) {
            return getMinecraftMethod;
        }
        getMinecraftMethod = findMethod("func_71410_x");
        if (getMinecraftMethod == null) {
            getMinecraftMethod = findMethod("getMinecraft");
        }
        if (getMinecraftMethod == null) {
            throw new IllegalStateException("Unable to locate Minecraft singleton method");
        }
        getMinecraftMethod.setAccessible(true);
        return getMinecraftMethod;
    }

    private static Field getPlayerField() {
        if (playerField == null) {
            playerField = getMinecraftField("field_71439_g", "thePlayer");
        }
        return playerField;
    }

    private static Field getWorldField() {
        if (worldField == null) {
            worldField = getMinecraftField("field_71441_e", "theWorld");
        }
        return worldField;
    }

    private static Field getRenderViewEntityField() {
        if (renderViewEntityField == null) {
            renderViewEntityField = getMinecraftField("field_71451_h", "renderViewEntity");
        }
        return renderViewEntityField;
    }

    private static Field getGameSettingsField() {
        if (gameSettingsField == null) {
            gameSettingsField = getMinecraftField("field_71474_y", "gameSettings");
        }
        return gameSettingsField;
    }

    private static Field getSprintKeyBindingField() {
        if (sprintKeyBindingField == null) {
            sprintKeyBindingField = getGameSettingsField("field_151444_V", "keyBindSprint");
        }
        return sprintKeyBindingField;
    }

    private static Object getFieldValue(Object owner, Field field) {
        try {
            return field.get(owner);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access Minecraft field " + field.getName(), e);
        }
    }

    private static Method findMethod(String name) {
        try {
            return Minecraft.class.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Field getMinecraftField(String srgName, String deobfuscatedName) {
        return getField(Minecraft.class, srgName, deobfuscatedName);
    }

    private static Field getGameSettingsField(String srgName, String deobfuscatedName) {
        return getField(GameSettings.class, srgName, deobfuscatedName);
    }

    private static Field getField(Class<?> owner, String srgName, String deobfuscatedName) {
        Field field = findField(owner, srgName);
        if (field == null) {
            field = findField(owner, deobfuscatedName);
        }
        if (field == null) {
            throw new IllegalStateException("Unable to locate " + owner.getName() + " field " + srgName + "/" + deobfuscatedName);
        }
        field.setAccessible(true);
        return field;
    }

    private static Field findField(Class<?> owner, String name) {
        try {
            return owner.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
