package com.glowingfederal.combatives.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

public final class CombativesConfig {
    private static final String CATEGORY_COMPAT = "compat";
    private static final String CATEGORY_DEBUG = "debug";
    private static final String CATEGORY_CAMERA = "camera";

    public static boolean enableCombativesCamera = true;
    public static boolean enableProceduralBob = true;
    public static boolean enableMovementLean = true;
    public static boolean enableMovementFov = true;
    public static boolean enableCameraShake = true;
    public static boolean enableAngelicaCameraCompat = true;
    public static boolean disableAngelicaViewBobbingCompat = true;
    public static boolean disableAngelicaDynamicFovCompat = true;
    public static boolean debugMovement = false;
    public static boolean verboseMovementDebug = false;
    public static boolean debugCamera = false;
    public static boolean verboseCameraDebug = false;

    private CombativesConfig() {
    }

    public static void load(File configFile) {
        Configuration config = new Configuration(configFile);
        config.load();

        enableAngelicaCameraCompat = config.getBoolean(
            "enableAngelicaCameraCompat",
            CATEGORY_COMPAT,
            enableAngelicaCameraCompat,
            "Enable optional Angelica camera compatibility when Angelica is present. This does not make Angelica a dependency."
        );
        disableAngelicaViewBobbingCompat = config.getBoolean(
            "disableAngelicaViewBobbingCompat",
            CATEGORY_COMPAT,
            disableAngelicaViewBobbingCompat,
            "When camera compatibility is enabled, let Combatives be the single owner of Angelica view bobbing."
        );
        disableAngelicaDynamicFovCompat = config.getBoolean(
            "disableAngelicaDynamicFovCompat",
            CATEGORY_COMPAT,
            disableAngelicaDynamicFovCompat,
            "When camera compatibility is enabled, let Combatives be the single owner of Angelica dynamic FOV."
        );
        enableCombativesCamera = config.getBoolean("enableCombativesCamera", CATEGORY_CAMERA, enableCombativesCamera, "Enable the client-only Combatives first-person camera controller.");
        enableProceduralBob = config.getBoolean("enableProceduralBob", CATEGORY_CAMERA, enableProceduralBob, "Enable subtle procedural Combatives movement bobbing.");
        enableMovementLean = config.getBoolean("enableMovementLean", CATEGORY_CAMERA, enableMovementLean, "Enable subtle movement-driven camera lean.");
        enableMovementFov = config.getBoolean("enableMovementFov", CATEGORY_CAMERA, enableMovementFov, "Enable subtle movement-driven FOV changes.");
        enableCameraShake = config.getBoolean("enableCameraShake", CATEGORY_CAMERA, enableCameraShake, "Enable the Combatives camera shake framework for movement impulses.");
        debugMovement = config.getBoolean(
            "debugMovement",
            CATEGORY_DEBUG,
            debugMovement,
            "Enable general Combatives movement diagnostics for lifecycle events and rejected actions. Per-frame diagnostics remain disabled unless verboseMovementDebug is also enabled."
        );
        verboseMovementDebug = config.getBoolean(
            "verboseMovementDebug",
            CATEGORY_DEBUG,
            verboseMovementDebug,
            "Enable per-frame/per-tick Combatives movement diagnostics. This implies debugMovement output for movement diagnostics."
        );
        debugCamera = config.getBoolean(
            "debugCamera",
            CATEGORY_DEBUG,
            debugCamera,
            "Enable major Combatives camera ownership and state-change diagnostics."
        );
        verboseCameraDebug = config.getBoolean(
            "verboseCameraDebug",
            CATEGORY_DEBUG,
            verboseCameraDebug,
            "Enable throttled per-frame Combatives camera diagnostics."
        );

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void logLoadedValues(Logger logger) {
        logger.info("Combatives config: enableCombativesCamera={}", enableCombativesCamera);
        logger.info("Combatives config: enableProceduralBob={}", enableProceduralBob);
        logger.info("Combatives config: enableMovementLean={}", enableMovementLean);
        logger.info("Combatives config: enableMovementFov={}", enableMovementFov);
        logger.info("Combatives config: enableCameraShake={}", enableCameraShake);
        logger.info("Combatives config: enableAngelicaCameraCompat={}", enableAngelicaCameraCompat);
        logger.info("Combatives config: disableAngelicaViewBobbingCompat={}", disableAngelicaViewBobbingCompat);
        logger.info("Combatives config: disableAngelicaDynamicFovCompat={}", disableAngelicaDynamicFovCompat);
        logger.info("Combatives config: debugMovement={}", debugMovement);
        logger.info("Combatives config: verboseMovementDebug={}", verboseMovementDebug);
        logger.info("Combatives config: debugCamera={}", debugCamera);
        logger.info("Combatives config: verboseCameraDebug={}", verboseCameraDebug);
    }
}
