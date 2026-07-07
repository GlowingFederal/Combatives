package com.glowingfederal.combatives.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

public final class CombativesConfig {
    private static final String CATEGORY_COMPAT = "compat";
    private static final String CATEGORY_DEBUG = "debug";

    public static boolean enableAngelicaCompat = true;
    public static boolean debugMovement = false;
    public static boolean debugCamera = false;

    private CombativesConfig() {
    }

    public static void load(File configFile) {
        Configuration config = new Configuration(configFile);
        config.load();

        enableAngelicaCompat = config.getBoolean(
            "enableAngelicaCompat",
            CATEGORY_COMPAT,
            enableAngelicaCompat,
            "Enable optional Angelica compatibility when Angelica is present. This does not make Angelica a dependency."
        );
        debugMovement = config.getBoolean(
            "debugMovement",
            CATEGORY_DEBUG,
            debugMovement,
            "Enable verbose logging for future movement systems."
        );
        debugCamera = config.getBoolean(
            "debugCamera",
            CATEGORY_DEBUG,
            debugCamera,
            "Enable verbose logging for future camera systems."
        );

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void logLoadedValues(Logger logger) {
        logger.info("Combatives config: enableAngelicaCompat={}", enableAngelicaCompat);
        logger.info("Combatives config: debugMovement={}", debugMovement);
        logger.info("Combatives config: debugCamera={}", debugCamera);
    }
}
