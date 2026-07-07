package com.fuzs.aquaacrobatics.integration.efr;

import net.minecraft.entity.player.EntityPlayer;

import com.fuzs.aquaacrobatics.integration.IntegrationManager;

import ganymedes01.etfuturum.configuration.configs.ConfigFunctions;
import ganymedes01.etfuturum.elytra.IElytraPlayer;
import ganymedes01.etfuturum.spectator.SpectatorMode;

public class EFRIntegration {

    public static boolean isElytraFlying(EntityPlayer entityPlayer) {
        if (IntegrationManager.isEFREnabled()) {
            return ((IElytraPlayer) entityPlayer).etfu$isElytraFlying();
        }
        return false;
    }

    public static float getTicksElytraFlying(EntityPlayer entityPlayer) {
        if (IntegrationManager.isEFREnabled()) {
            return ((IElytraPlayer) entityPlayer).etfu$getTicksElytraFlying();
        }
        return 0;
    }

    public static boolean isSpectator(EntityPlayer entityPlayer) {
        if (IntegrationManager.isEFREnabled()) {
            return SpectatorMode.isSpectator(entityPlayer);
        }
        return false;
    }

    public static byte elytraDataWatcherFlag() {
        if (IntegrationManager.isEFREnabled()) {
            return ConfigFunctions.elytraDataWatcherFlag;
        }
        return 7;
    }

}
