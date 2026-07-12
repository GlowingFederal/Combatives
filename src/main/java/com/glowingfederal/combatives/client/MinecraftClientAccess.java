package com.glowingfederal.combatives.client;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;

public final class MinecraftClientAccess {
    private static Method getMinecraftMethod;
    private static Field playerField;
    private static Field worldField;
    private static Field renderViewEntityField;

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
            playerField = getField("field_71439_g", "thePlayer");
        }
        return playerField;
    }

    private static Field getWorldField() {
        if (worldField == null) {
            worldField = getField("field_71441_e", "theWorld");
        }
        return worldField;
    }

    private static Field getRenderViewEntityField() {
        if (renderViewEntityField == null) {
            renderViewEntityField = getField("field_71451_h", "renderViewEntity");
        }
        return renderViewEntityField;
    }

    private static Object getFieldValue(Minecraft minecraft, Field field) {
        try {
            return field.get(minecraft);
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

    private static Field getField(String srgName, String deobfuscatedName) {
        Field field = findField(srgName);
        if (field == null) {
            field = findField(deobfuscatedName);
        }
        if (field == null) {
            throw new IllegalStateException("Unable to locate Minecraft field " + srgName + "/" + deobfuscatedName);
        }
        field.setAccessible(true);
        return field;
    }

    private static Field findField(String name) {
        try {
            return Minecraft.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
