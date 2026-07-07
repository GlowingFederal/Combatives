package com.fuzs.aquaacrobatics.core;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fuzs.aquaacrobatics.util.BlockPos;

public class UnderwaterGrassLikeHandler {

    public static void handleUnderwaterGrassLikeBlock(World world, int x, int y, int z, Random rand, CallbackInfo ci) {
        BlockPos pos = new BlockPos(x, y, z);
        if (world.isRemote || !world.doChunksNearChunkExist(pos.getX(), pos.getY(), pos.getZ(), 3)) {
            ci.cancel();
            return;
        }
        Block above = world.getBlock(
            pos.up()
                .getX(),
            pos.up()
                .getY(),
            pos.up()
                .getZ());
        if (above.getMaterial()
            .isLiquid()) {
            world.setBlock(pos.getX(), pos.getY(), pos.getZ(), Blocks.dirt);
            ci.cancel();
        }
    }
}
