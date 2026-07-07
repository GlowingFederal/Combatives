package com.fuzs.aquaacrobatics.mixins.early.minecraft.accessor;

import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Fluid.class)
public interface FluidAccessor {

    @Accessor(value = "stillIcon", remap = false)
    @Mutable
    void setStillTexture(IIcon rl);

    @Accessor(value = "flowingIcon", remap = false)
    @Mutable
    void setFlowingTexture(IIcon rl);
}
