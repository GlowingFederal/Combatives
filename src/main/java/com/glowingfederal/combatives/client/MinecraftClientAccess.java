package com.glowingfederal.combatives.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;

public final class MinecraftClientAccess {
    private static Method getMinecraftMethod;

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

    private static Method findMethod(String name) {
        try {
            return Minecraft.class.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
