package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.entity.player.EntityPlayer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** One heavily throttled, optional-linkage-safe sample of MPM's paired POV transform. */
public final class MpmPovDiagnostics {
    private MpmPovDiagnostics() { }

    public static void logSample(EntityPlayer player, float partialTicks,
            double originalPosY, double originalPrevPosY, double originalLastTickPosY,
            double mpmPosY, double mpmPrevPosY, double mpmLastTickPosY,
            boolean targetRestorationExecuted) {
        if (!CombativesConfig.debugMpmPov || Combatives.logger == null
                || player == null || player.ticksExisted % 100 != 0
                || !(player instanceof ICombativesPlayerPose)) {
            return;
        }

        ICombativesPlayerPose state = (ICombativesPlayerPose) player;
        ModelValues model = readModelValues(player);
        double mutation = mpmPosY - originalPosY;
        double a = model.available ? model.offsetY + (-1.615D + model.size * 0.315D) : mutation;

        Combatives.logger.info("MPM POV MUTATION framePhase=before-vanilla-target pose={} partialTicks={} boundingBox.minY={} posY={} yOffset={} ySize={} eyeAboveMinY={} consumedGetEyeHeight={} MPM_A={} MPM_model.size={} MPM_model.offsetY={} rawTargetingBefore=[{},{},{}] rawTargetingMutated=[{},{},{}] rawTargetingPresentedToVanilla=[{},{},{}] targetRestorationExecuted={}",
                state.getPose(), partialTicks, player.boundingBox.minY, originalPosY, player.yOffset, player.ySize,
                state.getEffectiveGeometry().eyeAboveMinY, player.getEyeHeight(), a, model.size, model.offsetY,
                originalPosY, originalPrevPosY, originalLastTickPosY,
                mpmPosY, mpmPrevPosY, mpmLastTickPosY,
                targetRestorationExecuted ? originalPosY : mpmPosY,
                targetRestorationExecuted ? originalPrevPosY : mpmPrevPosY,
                targetRestorationExecuted ? originalLastTickPosY : mpmLastTickPosY,
                targetRestorationExecuted);
    }

    private static ModelValues readModelValues(EntityPlayer player) {
        try {
            Class<?> controllerClass = Class.forName("noppes.mpm.client.controller.ClientDataController");
            Object controller = controllerClass.getMethod("Instance").invoke(null);
            Method getter = controllerClass.getMethod("getPlayerData", EntityPlayer.class);
            Object data = getter.invoke(controller, player);
            Field size = data.getClass().getField("size");
            Method offsetY = data.getClass().getMethod("offsetY");
            return new ModelValues(size.getInt(data), ((Number) offsetY.invoke(data)).doubleValue(), true);
        } catch (ReflectiveOperationException ignored) {
            return new ModelValues(Integer.MIN_VALUE, Double.NaN, false);
        }
    }

    private static final class ModelValues {
        private final int size;
        private final double offsetY;
        private final boolean available;

        private ModelValues(int size, double offsetY, boolean available) {
            this.size = size;
            this.offsetY = offsetY;
            this.available = available;
        }
    }
}
