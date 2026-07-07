package com.glowingfederal.combatives.movement;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import net.minecraft.entity.player.EntityPlayer;

public final class MovementDiagnostics {
    private MovementDiagnostics() {
    }

    public static void logFeatureState() {
        Combatives.logger.info("Combatives modern swimming behavior enabled");
        Combatives.logger.info("Combatives crawling behavior enabled");
    }

    public static void debug(EntityPlayer player, String message) {
        if (!CombativesConfig.debugMovement || Combatives.logger == null) {
            return;
        }
        Combatives.logger.info("[movement] {} for {}", message, player.getCommandSenderName());
    }
}
