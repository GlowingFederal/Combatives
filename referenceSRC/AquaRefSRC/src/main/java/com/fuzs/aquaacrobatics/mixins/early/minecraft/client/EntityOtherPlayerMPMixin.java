package com.fuzs.aquaacrobatics.mixins.early.minecraft.client;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

@Mixin(EntityOtherPlayerMP.class)
public abstract class EntityOtherPlayerMPMixin extends AbstractClientPlayer {

    public EntityOtherPlayerMPMixin(World p_i45074_1_, GameProfile p_i45074_2_) {
        super(p_i45074_1_, p_i45074_2_);
    }

    /**
     * Resets yOffset for remote players to 0.0F after AbstractClientPlayer#onUpdate is called.
     *
     * In EntityPlayerMixin, the local player's yOffset is modified (e.g., 0.4F for swimming,
     * 1.62F for standing). Without this correction, remote players may visually appear at
     * incorrect heights due to shared pose logic.
     */
    @Inject(
        method = "onUpdate",
        at = @At(
            value = "INVOKE",
            target = "net/minecraft/client/entity/AbstractClientPlayer.onUpdate ()V",
            shift = At.Shift.AFTER))
    private void onUpdated(CallbackInfo ci) {
        this.yOffset = 0.0F; // Prevent height desync for remote players
    }
}
