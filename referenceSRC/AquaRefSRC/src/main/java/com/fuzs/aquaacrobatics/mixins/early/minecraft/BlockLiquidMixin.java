package com.fuzs.aquaacrobatics.mixins.early.minecraft;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;

import com.fuzs.aquaacrobatics.config.ConfigHandler;

@Mixin(BlockLiquid.class)
public abstract class BlockLiquidMixin extends Block {

    public BlockLiquidMixin(Material p_i45394_1_) {
        super(p_i45394_1_);
    }

    @Override
    public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
        if (ConfigHandler.BlocksConfig.brighterWater && this.getMaterial() == Material.water) return 1;
        else return super.getLightOpacity(world, x, y, z);
    }
}
