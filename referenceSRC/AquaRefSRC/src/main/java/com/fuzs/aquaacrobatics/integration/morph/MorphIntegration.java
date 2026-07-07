package com.fuzs.aquaacrobatics.integration.morph;

import net.minecraft.entity.player.EntityPlayer;

import morph.api.Api;

public class MorphIntegration {

    public static boolean isMorphing(EntityPlayer player) {

        return Api.morphProgress(player.getDisplayName(), player.worldObj.isRemote ? true : false) < 1.0F;

    }

}
