package com.glowingfederal.combatives.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.settings.KeyBinding;

public final class ClientKeyBindingAccess {
    private static Method getIsKeyPressedMethod;

    private ClientKeyBindingAccess() {
    }

    public static boolean isKeyDown(KeyBinding keyBinding) {
        if (keyBinding == null) {
            return false;
        }
        Method method = getGetIsKeyPressedMethod();
        try {
            return (Boolean) method.invoke(keyBinding);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access KeyBinding pressed method", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("KeyBinding pressed method failed", cause);
        }
    }

    private static Method getGetIsKeyPressedMethod() {
        if (getIsKeyPressedMethod != null) {
            return getIsKeyPressedMethod;
        }
        getIsKeyPressedMethod = findMethod("func_151470_d");
        if (getIsKeyPressedMethod == null) {
            getIsKeyPressedMethod = findMethod("getIsKeyPressed");
        }
        if (getIsKeyPressedMethod == null) {
            throw new IllegalStateException("Unable to locate KeyBinding pressed method");
        }
        getIsKeyPressedMethod.setAccessible(true);
        return getIsKeyPressedMethod;
    }

    private static Method findMethod(String name) {
        try {
            return KeyBinding.class.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
