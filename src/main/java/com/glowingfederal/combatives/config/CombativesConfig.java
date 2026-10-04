package com.glowingfederal.combatives.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

public final class CombativesConfig {
    private static final String CATEGORY_DEBUG = "debug";
    private static final String CATEGORY_CAMERA = "camera";
    private static final String CATEGORY_COMPATIBILITY = "compatibility";
    private static final String CATEGORY_MOVEMENT = "movement";

    public static boolean enableCombativesCamera = CombativesConfigDefaults.ENABLE_COMBATIVES_CAMERA;
    public static boolean enableProceduralBob = CombativesConfigDefaults.ENABLE_PROCEDURAL_BOB;
    public static boolean enableMovementLean = CombativesConfigDefaults.ENABLE_MOVEMENT_LEAN;
    public static boolean enableMovementFov = CombativesConfigDefaults.ENABLE_MOVEMENT_FOV;
    public static boolean enableCameraRotations = CombativesConfigDefaults.ENABLE_CAMERA_ROTATIONS;
    public static boolean enableCameraShake = CombativesConfigDefaults.ENABLE_CAMERA_SHAKE;
    public static float maxCameraYawDegrees = CombativesConfigDefaults.MAX_CAMERA_YAW_DEGREES;
    public static boolean enableMouseDeltaClamp = CombativesConfigDefaults.ENABLE_MOUSE_DELTA_CLAMP;
    public static int maxMouseDelta = CombativesConfigDefaults.MAX_MOUSE_DELTA;
    public static boolean enableLandingCameraFeedback = CombativesConfigDefaults.ENABLE_LANDING_CAMERA_FEEDBACK;
    public static double landingFeedbackStrength = CombativesConfigDefaults.LANDING_FEEDBACK_STRENGTH;
    public static boolean enablePlayerFreefallCamera = CombativesConfigDefaults.ENABLE_PLAYER_FREEFALL_CAMERA;
    public static double playerFreefallCameraStrength = CombativesConfigDefaults.PLAYER_FREEFALL_CAMERA_STRENGTH;
    public static boolean enablePlayerInertiaCamera = CombativesConfigDefaults.ENABLE_PLAYER_INERTIA_CAMERA;
    public static double playerInertiaCameraStrength = CombativesConfigDefaults.PLAYER_INERTIA_CAMERA_STRENGTH;
    public static boolean enablePlayerCollisionCamera = CombativesConfigDefaults.ENABLE_PLAYER_COLLISION_CAMERA;
    public static double playerCollisionCameraStrength = CombativesConfigDefaults.PLAYER_COLLISION_CAMERA_STRENGTH;
    public static boolean enableExplosionCameraFeedback = CombativesConfigDefaults.ENABLE_EXPLOSION_CAMERA_FEEDBACK;
    public static double explosionFeedbackStrength = CombativesConfigDefaults.EXPLOSION_FEEDBACK_STRENGTH;
    public static boolean enableHorseCamera = CombativesConfigDefaults.ENABLE_HORSE_CAMERA;
    public static double horseCameraAmplitude = CombativesConfigDefaults.HORSE_CAMERA_AMPLITUDE;
    public static double horseTerrainImpulse = CombativesConfigDefaults.HORSE_TERRAIN_IMPULSE;
    public static double horseLanding = CombativesConfigDefaults.HORSE_LANDING;
    public static double horseTurningRoll = CombativesConfigDefaults.HORSE_TURNING_ROLL;
    public static boolean enableCrawlCamera = CombativesConfigDefaults.ENABLE_CRAWL_CAMERA;
    public static double crawlCameraAmplitude = CombativesConfigDefaults.CRAWL_CAMERA_AMPLITUDE;
    public static int crawlTransitionMillis = CombativesConfigDefaults.CRAWL_TRANSITION_MILLIS;
    public static boolean enableLeaning = CombativesConfigDefaults.ENABLE_LEANING;
    public static double maxLeanDistance = CombativesConfigDefaults.MAX_LEAN_DISTANCE;
    public static double maxLeanRoll = CombativesConfigDefaults.MAX_LEAN_ROLL;
    public static double leanInterpolation = CombativesConfigDefaults.LEAN_INTERPOLATION;
    public static boolean enableSliding = CombativesConfigDefaults.ENABLE_SLIDING;
    public static double slideMinimumEntrySpeed = CombativesConfigDefaults.SLIDE_MINIMUM_ENTRY_SPEED;
    public static double slideExitSpeed = CombativesConfigDefaults.SLIDE_EXIT_SPEED;
    public static double slideDeceleration = CombativesConfigDefaults.SLIDE_DECELERATION;
    public static double slideSteeringInfluence = CombativesConfigDefaults.SLIDE_STEERING_INFLUENCE;
    public static int slideMaximumTicks = CombativesConfigDefaults.SLIDE_MAXIMUM_TICKS;
    public static boolean debugMovement = CombativesConfigDefaults.DEBUG;
    public static boolean verboseMovementDebug = CombativesConfigDefaults.VERBOSE_DEBUG;
    public static boolean debugCamera = CombativesConfigDefaults.DEBUG;
    public static boolean verboseCameraDebug = CombativesConfigDefaults.VERBOSE_DEBUG;
    public static boolean debugMpmPov = CombativesConfigDefaults.DEBUG;
    public static boolean enableMpmHitboxScaling = CombativesConfigDefaults.ENABLE_MPM_HITBOX_SCALING;

    private static Configuration clientConfiguration;

    /** Persist only visual controls; gameplay configuration is never edited by the GUI. */
    public static void saveCameraSettings() {
        if (clientConfiguration == null) return;
        clientConfiguration.get(CATEGORY_CAMERA, "enableCombativesCamera", CombativesConfigDefaults.ENABLE_COMBATIVES_CAMERA).set(enableCombativesCamera);
        clientConfiguration.get(CATEGORY_CAMERA, "enableProceduralBob", CombativesConfigDefaults.ENABLE_PROCEDURAL_BOB).set(enableProceduralBob);
        clientConfiguration.get(CATEGORY_CAMERA, "enableMovementLean", CombativesConfigDefaults.ENABLE_MOVEMENT_LEAN).set(enableMovementLean);
        clientConfiguration.get(CATEGORY_CAMERA, "enableMovementFov", CombativesConfigDefaults.ENABLE_MOVEMENT_FOV).set(enableMovementFov);
        clientConfiguration.get(CATEGORY_CAMERA, "enableCameraRotations", CombativesConfigDefaults.ENABLE_CAMERA_ROTATIONS).set(enableCameraRotations);
        clientConfiguration.get(CATEGORY_CAMERA, "enableCameraShake", CombativesConfigDefaults.ENABLE_CAMERA_SHAKE).set(enableCameraShake);
        clientConfiguration.get(CATEGORY_CAMERA, "enableExplosionCameraFeedback", CombativesConfigDefaults.ENABLE_EXPLOSION_CAMERA_FEEDBACK).set(enableExplosionCameraFeedback);
        clientConfiguration.get(CATEGORY_CAMERA, "enableLandingCameraFeedback", CombativesConfigDefaults.ENABLE_LANDING_CAMERA_FEEDBACK).set(enableLandingCameraFeedback);
        clientConfiguration.get(CATEGORY_CAMERA, "enablePlayerFreefallCamera", CombativesConfigDefaults.ENABLE_PLAYER_FREEFALL_CAMERA).set(enablePlayerFreefallCamera);
        clientConfiguration.get(CATEGORY_CAMERA, "enablePlayerInertiaCamera", CombativesConfigDefaults.ENABLE_PLAYER_INERTIA_CAMERA).set(enablePlayerInertiaCamera);
        clientConfiguration.get(CATEGORY_CAMERA, "enablePlayerCollisionCamera", CombativesConfigDefaults.ENABLE_PLAYER_COLLISION_CAMERA).set(enablePlayerCollisionCamera);
        clientConfiguration.get(CATEGORY_CAMERA, "enableCrawlCamera", CombativesConfigDefaults.ENABLE_CRAWL_CAMERA).set(enableCrawlCamera);
        clientConfiguration.get(CATEGORY_CAMERA, "enableHorseCamera", CombativesConfigDefaults.ENABLE_HORSE_CAMERA).set(enableHorseCamera);
        CameraVisualSetting.save(clientConfiguration);
    }

    private CombativesConfig() {
    }

    public static void load(File configFile) {
        Configuration config = new Configuration(configFile);
        config.load();
        clientConfiguration = config;

        enableCombativesCamera = config.getBoolean("enableCombativesCamera", CATEGORY_CAMERA, enableCombativesCamera, "Enable the client-only Combatives first-person camera controller.");
        enableProceduralBob = config.getBoolean("enableProceduralBob", CATEGORY_CAMERA, enableProceduralBob, "Enable subtle procedural Combatives movement bobbing.");
        enableMovementLean = config.getBoolean("enableMovementLean", CATEGORY_CAMERA, enableMovementLean, "Enable subtle movement-driven camera lean.");
        enableMovementFov = config.getBoolean("enableMovementFov", CATEGORY_CAMERA, enableMovementFov, "Enable subtle movement-driven FOV changes.");
        enableCameraRotations = config.getBoolean("enableCameraRotations", CATEGORY_CAMERA, enableCameraRotations, "Emergency diagnostic toggle: when false, Combatives applies only camera translations and FOV, never pitch or roll rotations.");
        enableCameraShake = config.getBoolean("enableCameraShake", CATEGORY_CAMERA, enableCameraShake, "Enable the Combatives camera shake framework for movement impulses.");
        maxCameraYawDegrees = config.getFloat("maxCameraYawDegrees", CATEGORY_CAMERA, maxCameraYawDegrees, 0.0F, 12.0F, "Hard clamp in degrees for visual-only Combatives yaw offsets. Tuned independently from pitch and roll.");
        enableMouseDeltaClamp = config.getBoolean("enableMouseDeltaClamp", CATEGORY_CAMERA, enableMouseDeltaClamp, "Clamp pathological raw LWJGL mouse deltas before vanilla camera sensitivity scaling consumes them.");
        maxMouseDelta = config.getInt("maxMouseDelta", CATEGORY_CAMERA, maxMouseDelta, 1, 10000, "Maximum absolute raw mouse delta accepted from LWJGL per mouseXYChange call.");
        enableLandingCameraFeedback = config.getBoolean("enableLandingCameraFeedback", CATEGORY_CAMERA, enableLandingCameraFeedback, "Enable visual-only landing camera dip and recovery impulses.");
        enablePlayerFreefallCamera = config.getBoolean("enablePlayerFreefallCamera", CATEGORY_CAMERA, enablePlayerFreefallCamera, "Enable subtle sustained player freefall anticipation.");
        enablePlayerInertiaCamera = config.getBoolean("enablePlayerInertiaCamera", CATEGORY_CAMERA, enablePlayerInertiaCamera, "Enable conservative motion-sampled player inertia.");
        enablePlayerCollisionCamera = config.getBoolean("enablePlayerCollisionCamera", CATEGORY_CAMERA, enablePlayerCollisionCamera, "Enable meaningful player momentum-loss impacts.");
        enableExplosionCameraFeedback = config.getBoolean("enableExplosionCameraFeedback", CATEGORY_CAMERA, enableExplosionCameraFeedback, "Enable visual-only low-frequency explosion camera feedback near client explosions.");
        enableHorseCamera = config.getBoolean("enableHorseCamera", CATEGORY_CAMERA, enableHorseCamera, "Enable continuous, motion-sampled first-person riding feedback for registered horse mounts.");
        enableCrawlCamera = config.getBoolean("enableCrawlCamera", CATEGORY_CAMERA, enableCrawlCamera, "Enable restrained continuous crawling motion and crawl transitions.");
        crawlTransitionMillis = config.getInt("crawlTransitionMillis", CATEGORY_CAMERA, crawlTransitionMillis, 150, 250, "Monotonic crawl enter/exit camera blend duration in milliseconds.");
        enableLeaning = config.getBoolean("enableLeaning", CATEGORY_MOVEMENT, enableLeaning, "Enable authoritative tactical leaning.");
        maxLeanDistance = config.getFloat("maxLeanDistance", CATEGORY_MOVEMENT, (float) maxLeanDistance, 0.0F, 0.5F, "Maximum first-person lean displacement in blocks.");
        maxLeanRoll = config.getFloat("maxLeanRoll", CATEGORY_MOVEMENT, (float) CombativesConfigDefaults.MAX_LEAN_ROLL, 0.0F, 15.0F, "Legacy tactical lean roll angle in degrees. Excluded from live cosmetic intensity controls; does not alter lean reach.");
        enableSliding = config.getBoolean("enableSliding", CATEGORY_MOVEMENT, enableSliding, "Enable sprint-to-crawl sliding.");
        slideMinimumEntrySpeed = config.getFloat("slideMinimumEntrySpeed", CATEGORY_MOVEMENT, (float) slideMinimumEntrySpeed, 0.05F, 1.0F, "Minimum horizontal blocks per tick required to enter a slide.");
        slideExitSpeed = config.getFloat("slideExitSpeed", CATEGORY_MOVEMENT, (float) slideExitSpeed, 0.01F, 0.5F, "Horizontal speed at which a slide ends.");
        slideDeceleration = config.getFloat("slideDeceleration", CATEGORY_MOVEMENT, (float) slideDeceleration, 0.001F, 0.1F, "Horizontal speed removed each slide tick.");
        slideSteeringInfluence = config.getFloat("slideSteeringInfluence", CATEGORY_MOVEMENT, (float) slideSteeringInfluence, 0.0F, 0.25F, "Maximum fraction of slide direction adjusted toward movement input each tick.");
        slideMaximumTicks = config.getInt("slideMaximumTicks", CATEGORY_MOVEMENT, slideMaximumTicks, 5, 100, "Maximum authoritative slide duration in ticks.");
        enableMpmHitboxScaling = config.getBoolean("enableMpmHitboxScaling", CATEGORY_COMPATIBILITY,
                enableMpmHitboxScaling, "Scale player collision width and height by MorePlayerModels+'s synchronized whole-model size. Independent of camera compatibility.");
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
        debugMpmPov = config.getBoolean(
            "debugMpmPov",
            CATEGORY_DEBUG,
            debugMpmPov,
            "Enable one focused MPM camera/targeting ownership sample every five seconds."
        );

        CameraVisualSetting.load(config);

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
        logger.info("Combatives config: maxCameraYawDegrees={}", maxCameraYawDegrees);
        logger.info("Combatives config: enableMouseDeltaClamp={}", enableMouseDeltaClamp);
        logger.info("Combatives config: maxMouseDelta={}", maxMouseDelta);
        logger.info("Combatives config: enableLandingCameraFeedback={}", enableLandingCameraFeedback);
        logger.info("Combatives config: landingFeedbackStrength={}", landingFeedbackStrength);
        logger.info("Combatives config: player motion camera freefall={}/{}, inertia={}/{}, collision={}/{}", enablePlayerFreefallCamera, playerFreefallCameraStrength, enablePlayerInertiaCamera, playerInertiaCameraStrength, enablePlayerCollisionCamera, playerCollisionCameraStrength);
        logger.info("Combatives config: enableExplosionCameraFeedback={}", enableExplosionCameraFeedback);
        logger.info("Combatives config: explosionFeedbackStrength={}", explosionFeedbackStrength);
        logger.info("Combatives config: horse camera={}/{}, terrain={}, landing={}, turning={}", enableHorseCamera, horseCameraAmplitude, horseTerrainImpulse, horseLanding, horseTurningRoll);
        logger.info("Combatives config: crawl camera={}/{}, transitionMillis={}", enableCrawlCamera, crawlCameraAmplitude, crawlTransitionMillis);
        logger.info("Combatives config: debugMovement={}", debugMovement);
        logger.info("Combatives config: verboseMovementDebug={}", verboseMovementDebug);
        logger.info("Combatives config: debugCamera={}", debugCamera);
        logger.info("Combatives config: verboseCameraDebug={}", verboseCameraDebug);
        logger.info("Combatives config: debugMpmPov={}", debugMpmPov);
        logger.info("Combatives config: enableMpmHitboxScaling={}", enableMpmHitboxScaling);
    }
}
