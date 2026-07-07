package com.fuzs.aquaacrobatics.mixins.early.minecraft;

import net.minecraft.world.biome.BiomeGenBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.fuzs.aquaacrobatics.biome.BiomeWaterFogColors;
import com.fuzs.aquaacrobatics.config.ConfigHandler;

@Mixin(BiomeGenBase.class)
public abstract class BiomeMixin {

    @Shadow(remap = false)
    public abstract int getWaterColorMultiplier();

    /* For OptiFine */
    @SuppressWarnings("unused")
    public int aqua$waterColorMultiplier() {
        if (ConfigHandler.BlocksConfig.newWaterColors) {
            /* We might call getWaterColorForBiome twice, but it's fine because it caches after the first call */
            return BiomeWaterFogColors.getWaterColorForBiome((BiomeGenBase) (Object) this, getWaterColorMultiplier());
        } else return getWaterColorMultiplier();
    }

    @Inject(method = "getWaterColorMultiplier", at = @At("TAIL"), remap = false, cancellable = true)
    private void forceNewColor(CallbackInfoReturnable<Integer> cir) {
        if (ConfigHandler.BlocksConfig.newWaterColors) {
            cir.setReturnValue(
                BiomeWaterFogColors.getWaterColorForBiome((BiomeGenBase) (Object) this, cir.getReturnValue()));
        }
    }
}
