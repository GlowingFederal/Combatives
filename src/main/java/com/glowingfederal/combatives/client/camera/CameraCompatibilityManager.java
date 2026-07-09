package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import cpw.mods.fml.common.Loader;

public final class CameraCompatibilityManager {
    private static boolean initialized;
    private static boolean angelicaLoaded;
    private static Boolean lastVanillaBobCancellationState;

    private CameraCompatibilityManager() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        try {
            angelicaLoaded = Loader.isModLoaded("angelica");
            if (CombativesConfig.debugCamera && angelicaLoaded) Combatives.logger.info("Combatives camera detected Angelica client compatibility target");
        } catch (Throwable t) {
            angelicaLoaded = false;
            if (CombativesConfig.verboseCameraDebug) Combatives.logger.warn("Combatives camera compatibility detection failed", t);
        }
    }

    public static boolean shouldCancelVanillaViewBobbing() {
        boolean cancel = CombativesConfig.enableCombativesCamera && CombativesConfig.enableProceduralBob;
        if (CombativesConfig.debugCamera && (lastVanillaBobCancellationState == null || lastVanillaBobCancellationState.booleanValue() != cancel)) {
            Combatives.logger.info("Combatives camera vanilla view bobbing cancellation {}", cancel ? "active" : "inactive");
            lastVanillaBobCancellationState = Boolean.valueOf(cancel);
        }
        return cancel;
    }

    public static boolean ownsViewBobbing() {
        init();
        return shouldCancelVanillaViewBobbing()
            && (!angelicaLoaded || (CombativesConfig.enableAngelicaCameraCompat && CombativesConfig.disableAngelicaViewBobbingCompat));
    }

    public static boolean ownsDynamicFov() {
        init();
        if (!CombativesConfig.enableCombativesCamera || !CombativesConfig.enableMovementFov) return false;
        return !angelicaLoaded || (CombativesConfig.enableAngelicaCameraCompat && CombativesConfig.disableAngelicaDynamicFovCompat);
    }
}
