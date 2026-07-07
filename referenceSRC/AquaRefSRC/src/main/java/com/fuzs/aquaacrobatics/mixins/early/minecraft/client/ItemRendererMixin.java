package com.fuzs.aquaacrobatics.mixins.early.minecraft.client;

import net.minecraft.client.renderer.ItemRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.fuzs.aquaacrobatics.config.ConfigHandler;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @ModifyArg(
        method = "renderWarpedTextureOverlay",
        at = @At(value = "INVOKE", target = "org/lwjgl/opengl/GL11.glColor4f(FFFF)V", ordinal = 0, remap = false),
        index = 3)
    private float replaceOpacity(float originalOpacity) {
        if (ConfigHandler.BlocksConfig.newWaterColors) return 0.1f;
        else return originalOpacity;
    }
}
