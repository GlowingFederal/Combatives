package com.fuzs.aquaacrobatics.mixins.early.minecraft.client;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.fuzs.aquaacrobatics.client.entity.IPlayerSPSwimming;

@SuppressWarnings("unused")
@Mixin(PlayerControllerMP.class)
public abstract class PlayerControllerMPMixin {

    @Redirect(
        method = "onPlayerRightClick",
        at = @At(value = "INVOKE", target = "net/minecraft/entity/player/EntityPlayer.isSneaking()Z"))
    public boolean isSneaking(EntityPlayer playerIn) {

        return ((IPlayerSPSwimming) playerIn).isActuallySneaking();
    }

}
