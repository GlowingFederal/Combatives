package com.glowingfederal.combatives.config;

import net.minecraftforge.common.config.Configuration;

/** Cosmetic controls only. Legacy keys keep their original category and public field. */
public enum CameraVisualSetting {
    MASTER("cameraEffectsIntensity", "Master intensity", 1F, 0F, 2F, "Scales cosmetic effects; excludes tactical lean eye displacement and roll."),
    WALK("walkBobIntensity", "Walk bob", 1.25F, 0F, 3F, "Movement bob while walking, sneaking or swimming."),
    SPRINT("sprintBobIntensity", "Sprint bob", 1.4F, 0F, 3F, "Movement bob while sprinting."),
    MOVEMENT_LEAN("movementLeanIntensity", "Movement lean", 1.35F, 0F, 3F, "Directional movement pitch and roll."),
    JUMP("jumpCameraIntensity", "Jump", 0.65F, 0F, 3F, "Bounded takeoff pulse, independent of vanilla vertical-velocity pitch."),
    LANDING("landingFeedbackStrength", "Landing", 1F, 0F, 4F, "Short compression and critically damped settle."),
    FREEFALL("playerFreefallCameraStrength", "Freefall", 1F, 0F, 4F, "Sustained airborne anticipation; ends on support."),
    RECOIL("recoilCameraIntensity", "Recoil camera", 1.25F, 0F, 3F, "Camera API weapon-fire and HMG recoil only; never aiming recoil."),
    EXPLOSION("explosionFeedbackStrength", "Explosion shake", 1F, 0F, 4F, "Explosion camera response; never damage or knockback."),
    INERTIA("playerInertiaCameraStrength", "Inertia / camera sway", 1F, 0F, 4F, "Acceleration and turning camera motion; weapon model sway belongs to the weapon mod."),
    COLLISION("playerCollisionCameraStrength", "Collision feedback", 1F, 0F, 4F, "Cosmetic momentum-loss pulse."),
    CRAWL("crawlCameraAmplitude", "Crawl motion", 1F, 0F, 3F, "Crawl posture, cycle and pull; requires enableCrawlCamera."),
    HORSE("horseCameraAmplitude", "Horse gait", 1F, 0F, 3F, "Horse stride and acceleration motion."),
    HORSE_TERRAIN("horseTerrainImpulse", "Horse terrain", 1F, 0F, 3F, "Horse terrain compression."),
    HORSE_LANDING("horseLanding", "Horse landing", 1F, 0F, 3F, "Horse landing pulse."),
    HORSE_TURN("horseTurningRoll", "Horse turning roll", 1F, 0F, 3F, "Cosmetic horse turning roll."),
    LEAN_RESPONSE("leanInterpolation", "Lean response", 0.24F, 0.05F, 1F, "Presentation response per game tick; higher is faster.", "movement"),
    ADS("adsCameraMotion", "ADS ambient motion", 0.5F, 0F, 1F, "Ambient camera motion retained while HMG or Flan's aims. Recoil and impacts remain separately controlled."),
    FOV("movementFovIntensity", "Movement FOV", 1.25F, 0F, 3F, "Additional sprint/swim FOV only; never weapon zoom."),
    SLIDE("slideCameraIntensity", "Slide camera dip", 1F, 0F, 3F, "Cosmetic slide dip only; does not enable or alter sliding."),
    RESPONSE("cameraResponse", "Movement response", 1F, 0.5F, 2F, "Tick-based movement input/FOV response; higher is faster. Impact lifetimes remain fixed.");

    public final String key, label, description, category;
    public final float defaultValue, min, max;
    private float value;

    CameraVisualSetting(String key, String label, float value, float min, float max, String description) {
        this(key, label, value, min, max, description, "camera");
    }

    CameraVisualSetting(String key, String label, float value, float min, float max, String description, String category) {
        this.key = key; this.label = label; this.description = description; this.category = category;
        this.value = this.defaultValue = value; this.min = min; this.max = max;
    }

    public float get() {
        switch (this) {
            case LANDING: return (float) CombativesConfig.landingFeedbackStrength;
            case FREEFALL: return (float) CombativesConfig.playerFreefallCameraStrength;
            case EXPLOSION: return (float) CombativesConfig.explosionFeedbackStrength;
            case INERTIA: return (float) CombativesConfig.playerInertiaCameraStrength;
            case COLLISION: return (float) CombativesConfig.playerCollisionCameraStrength;
            case CRAWL: return (float) CombativesConfig.crawlCameraAmplitude;
            case HORSE: return (float) CombativesConfig.horseCameraAmplitude;
            case HORSE_TERRAIN: return (float) CombativesConfig.horseTerrainImpulse;
            case HORSE_LANDING: return (float) CombativesConfig.horseLanding;
            case HORSE_TURN: return (float) CombativesConfig.horseTurningRoll;
            case LEAN_RESPONSE: return (float) CombativesConfig.leanInterpolation;
            default: return value;
        }
    }

    public void set(float input) {
        float v = Float.isNaN(input) || Float.isInfinite(input) ? defaultValue : Math.max(min, Math.min(max, input));
        switch (this) {
            case LANDING: CombativesConfig.landingFeedbackStrength = v; break;
            case FREEFALL: CombativesConfig.playerFreefallCameraStrength = v; break;
            case EXPLOSION: CombativesConfig.explosionFeedbackStrength = v; break;
            case INERTIA: CombativesConfig.playerInertiaCameraStrength = v; break;
            case COLLISION: CombativesConfig.playerCollisionCameraStrength = v; break;
            case CRAWL: CombativesConfig.crawlCameraAmplitude = v; break;
            case HORSE: CombativesConfig.horseCameraAmplitude = v; break;
            case HORSE_TERRAIN: CombativesConfig.horseTerrainImpulse = v; break;
            case HORSE_LANDING: CombativesConfig.horseLanding = v; break;
            case HORSE_TURN: CombativesConfig.horseTurningRoll = v; break;
            case LEAN_RESPONSE: CombativesConfig.leanInterpolation = v; break;
            default: value = v;
        }
    }

    static void load(Configuration config) {
        for (CameraVisualSetting setting : values()) {
            setting.set(config.getFloat(setting.key, setting.category, setting.defaultValue,
                    setting.min, setting.max, "Client visual only. " + setting.description));
        }
    }

    static void save(Configuration config) {
        for (CameraVisualSetting setting : values()) {
            config.get(setting.category, setting.key, (double) setting.defaultValue).set((double) setting.get());
        }
        config.save();
    }

    /** Convert a tick filter to a configurable time constant, never a render-count factor. */
    public static float response(float alpha) {
        return 1F - (float) Math.pow(1F - alpha, RESPONSE.get());
    }

    /** Scale at sampling, so active impulses respond immediately and zero never halts their age. */
    public static float effectScale(String id) {
        if (id.startsWith("hmg_overdrive:hmg_recoil_") || id.equals("combatives:weapon_fire")) return RECOIL.get();
        if (id.equals("combatives:player_jump")) return JUMP.get();
        if (id.equals("combatives:player_landing") || id.equals("combatives:landing")) return LANDING.get();
        if (id.equals("combatives:player_freefall")) return FREEFALL.get();
        if (id.equals("combatives:player_inertia")) return INERTIA.get();
        if (id.equals("combatives:player_collision")) return COLLISION.get();
        if (id.equals("combatives:explosion")) return EXPLOSION.get();
        if (id.startsWith("combatives:crawl_")) return CRAWL.get();
        if (id.equals("combatives:horse_terrain")) return HORSE_TERRAIN.get();
        if (id.equals("combatives:horse_landing")) return HORSE_LANDING.get();
        if (id.startsWith("combatives:horse_turn_")) return HORSE_TURN.get();
        if (id.startsWith("combatives:horse_")) return HORSE.get();
        return 1F;
    }

    public static boolean isAmbient(String id) {
        return id.equals("combatives:player_inertia") || id.startsWith("combatives:crawl_")
                || id.startsWith("combatives:horse_stride_") || id.equals("combatives:horse_accelerate")
                || id.equals("combatives:horse_decelerate") || id.startsWith("combatives:horse_turn_");
    }
}
