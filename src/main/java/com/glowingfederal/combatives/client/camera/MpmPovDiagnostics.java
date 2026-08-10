package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.EffectivePlayerGeometry;
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
        EffectivePlayerGeometry geometry = state.getEffectiveGeometry();
        Pose pose = state.getPose();
        double mutation = mpmPosY - originalPosY;
        ModelValues model = readModelValues(player);
        double a = model.available ? model.offsetY + (-1.615D + model.size * 0.315D) : mutation;
        double vanillaPositionY = lerp(originalLastTickPosY, originalPosY, partialTicks);
        double mpmPositionY = lerp(mpmLastTickPosY, mpmPosY, partialTicks);
        double originalYOffset = player.yOffset + a;
        double unmodifiedVanillaCameraY = lerp(originalPrevPosY, originalPosY, partialTicks)
                - (originalYOffset - 1.62D);
        double mpmExpectedCameraY = unmodifiedVanillaCameraY + a;
        boolean cameraOverrideExecuted = isLowPose(state);
        double combativesCameraY = cameraOverrideExecuted
                ? lerp(originalPrevPosY, originalPosY, partialTicks)
                    + (player.boundingBox.minY - originalPosY) + geometry.eyeAboveMinY
                : mpmExpectedCameraY;
        double legacyEye = player.getEyeHeight();
        double unmodifiedTargetY = vanillaPositionY + legacyEye;
        double mpmExpectedTargetY = mpmPositionY + legacyEye;
        double combativesTargetY = targetRestorationExecuted ? unmodifiedTargetY : mpmExpectedTargetY;

        Combatives.logger.info("MPM POV SAMPLE: pose={} boundingBox.minY={} posY={} yOffset={} ySize={} eyeAboveMinY={} legacyGetEyeHeight={} MPM_A={} MPM_model.size={} MPM_model.offsetY={} unmodifiedVanillaCameraY={} MPMExpectedCameraY={} combativesCameraY={} unmodifiedTargetY={} MPMExpectedTargetY={} combativesTargetY={} cameraMinusTargetY={} targetingBefore=[{},{},{}] targetingMutated=[{},{},{}] targetingRestored=[{},{},{}] cameraOverrideExecuted={} targetRestorationExecuted={} partialTicks={}",
                pose, player.boundingBox.minY, originalPosY, player.yOffset, player.ySize,
                geometry.eyeAboveMinY, legacyEye, a, model.size, model.offsetY,
                unmodifiedVanillaCameraY, mpmExpectedCameraY, combativesCameraY,
                unmodifiedTargetY, mpmExpectedTargetY, combativesTargetY, combativesCameraY - combativesTargetY,
                originalPosY, originalPrevPosY, originalLastTickPosY,
                mpmPosY, mpmPrevPosY, mpmLastTickPosY,
                targetRestorationExecuted ? originalPosY : mpmPosY,
                targetRestorationExecuted ? originalPrevPosY : mpmPrevPosY,
                targetRestorationExecuted ? originalLastTickPosY : mpmLastTickPosY,
                cameraOverrideExecuted, targetRestorationExecuted, partialTicks);
    }

    private static boolean isLowPose(ICombativesPlayerPose state) {
        return state.getPose() != Pose.STANDING || state.isSwimming()
                || state.isCrawlKeyDown() || state.isActuallySwimming();
    }

    private static double lerp(double from, double to, float partialTicks) {
        return from + (to - from) * partialTicks;
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
