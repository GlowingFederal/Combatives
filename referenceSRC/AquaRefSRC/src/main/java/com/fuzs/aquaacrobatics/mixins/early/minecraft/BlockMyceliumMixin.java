package com.fuzs.aquaacrobatics.mixins.early.minecraft;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockMycelium;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fuzs.aquaacrobatics.core.UnderwaterGrassLikeHandler;
import com.fuzs.aquaacrobatics.util.BlockPos;

/* MC-130137 */
@Mixin(BlockMycelium.class)
public abstract class BlockMyceliumMixin {

    @Inject(method = "updateTick", at = @At("HEAD"), cancellable = true)
    private void updateUnderwaterToDirt(World worldIn, int x, int y, int z, Random random, CallbackInfo ci) {
        UnderwaterGrassLikeHandler.handleUnderwaterGrassLikeBlock(worldIn, x, y, z, random, ci);
    }

    @Redirect(
        method = "updateTick",
        at = @At(
            value = "INVOKE",
            target = "net/minecraft/world/World.setBlock(IIILnet/minecraft/block/Block;)Z",
            ordinal = 1),
        require = 0)
    public boolean avoidSettingGrass(World world, int x, int y, int z, Block blockType) {
        BlockPos pos = new BlockPos(x, y, z);
        if (world.getBlock(
            pos.up()
                .getX(),
            pos.up()
                .getY(),
            pos.up()
                .getZ())
            .getMaterial()
            .isLiquid()) return false;
        return world.setBlock(pos.getX(), pos.getY(), pos.getZ(), blockType);
    }
}
