package com.glowingfederal.combatives.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

public final class CombativesConfig {
    private static final String CATEGORY_DEBUG = "debug";
    private static final String CATEGORY_CAMERA = "camera";

    public static boolean enableCombativesCamera = true;
    public static boolean enableProceduralBob = true;
    public static boolean enableMovementLean = true;
    public static boolean enableMovementFov = true;
    public static boolean enableCameraRotations = true;
    public static boolean enableCameraShake = true;
    public static boolean enableMouseDeltaClamp = true;
    public static int maxMouseDelta = 80;
    public static boolean enableLandingCameraFeedback = true;
    public static double landingFeedbackStrength = 1.0D;
    public static boolean enableExplosionCameraFeedback = true;
    public static double explosionFeedbackStrength = 1.0D;
    public static boolean debugMovement = false;
    public static boolean verboseMovementDebug = false;
    public static boolean debugCamera = false;
    public static boolean verboseCameraDebug = false;

    private CombativesConfig() {
    }

    public static void load(File configFile) {
        Configuration config = new Configuration(configFile);
        config.load();

        enableCombativesCamera = config.getBoolean("enableCombativesCamera", CATEGORY_CAMERA, enableCombativesCamera, "Enable the client-only Combatives first-person camera controller.");
        enableProceduralBob = config.getBoolean("enableProceduralBob", CATEGORY_CAMERA, enableProceduralBob, "Enable subtle procedural Combatives movement bobbing.");
        enableMovementLean = config.getBoolean("enableMovementLean", CATEGORY_CAMERA, enableMovementLean, "Enable subtle movement-driven camera lean.");
        enableMovementFov = config.getBoolean("enableMovementFov", CATEGORY_CAMERA, enableMovementFov, "Enable subtle movement-driven FOV changes.");
        enableCameraRotations = config.getBoolean("enableCameraRotations", CATEGORY_CAMERA, enableCameraRotations, "Emergency diagnostic toggle: when false, Combatives applies only camera translations and FOV, never pitch or roll rotations.");
        enableCameraShake = config.getBoolean("enableCameraShake", CATEGORY_CAMERA, enableCameraShake, "Enable the Combatives camera shake framework for movement impulses.");
        enableMouseDeltaClamp = config.getBoolean("enableMouseDeltaClamp", CATEGORY_CAMERA, enableMouseDeltaClamp, "Clamp pathological raw LWJGL mouse deltas before vanilla camera sensitivity scaling consumes them.");
        maxMouseDelta = config.getInt("maxMouseDelta", CATEGORY_CAMERA, maxMouseDelta, 1, 10000, "Maximum absolute raw mouse delta accepted from LWJGL per mouseXYChange call.");
        enableLandingCameraFeedback = config.getBoolean("enableLandingCameraFeedback", CATEGORY_CAMERA, enableLandingCameraFeedback, "Enable visual-only landing camera dip and recovery impulses.");
        landingFeedbackStrength = config.getFloat("landingFeedbackStrength", CATEGORY_CAMERA, (float) landingFeedbackStrength, 0.0F, 4.0F, "Multiplier for visual-only landing camera feedback strength.");
        enableExplosionCameraFeedback = config.getBoolean("enableExplosionCameraFeedback", CATEGORY_CAMERA, enableExplosionCameraFeedback, "Enable visual-only low-frequency explosion camera feedback near client explosions.");
        explosionFeedbackStrength = config.getFloat("explosionFeedbackStrength", CATEGORY_CAMERA, (float) explosionFeedbackStrength, 0.0F, 4.0F, "Multiplier for visual-only explosion camera feedback strength.");
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
        logger.info("Combatives config: enableCameraRotations={}", enableCameraRotations);
        logger.info("Combatives config: enableCameraShake={}", enableCameraShake);
        logger.info("Combatives config: enableMouseDeltaClamp={}", enableMouseDeltaClamp);
        logger.info("Combatives config: maxMouseDelta={}", maxMouseDelta);
        logger.info("Combatives config: enableLandingCameraFeedback={}", enableLandingCameraFeedback);
        logger.info("Combatives config: landingFeedbackStrength={}", landingFeedbackStrength);
        logger.info("Combatives config: enableExplosionCameraFeedback={}", enableExplosionCameraFeedback);
        logger.info("Combatives config: explosionFeedbackStrength={}", explosionFeedbackStrength);
        logger.info("Combatives config: debugMovement={}", debugMovement);
        logger.info("Combatives config: verboseMovementDebug={}", verboseMovementDebug);
        logger.info("Combatives config: debugCamera={}", debugCamera);
        logger.info("Combatives config: verboseCameraDebug={}", verboseCameraDebug);
    }
}
