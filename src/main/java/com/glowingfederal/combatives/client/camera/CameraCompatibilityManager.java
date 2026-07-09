package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import cpw.mods.fml.common.Loader;

public final class CameraCompatibilityManager {
    private static boolean initialized;
    private static boolean angelicaLoaded;

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

    public static boolean ownsViewBobbing() {
        init();
        if (!CombativesConfig.enableCombativesCamera || !CombativesConfig.enableProceduralBob) return false;
        return !angelicaLoaded || (CombativesConfig.enableAngelicaCameraCompat && CombativesConfig.disableAngelicaViewBobbingCompat);
    }

    public static boolean ownsDynamicFov() {
        init();
        if (!CombativesConfig.enableCombativesCamera || !CombativesConfig.enableMovementFov) return false;
        return !angelicaLoaded || (CombativesConfig.enableAngelicaCameraCompat && CombativesConfig.disableAngelicaDynamicFovCompat);
    }
}
