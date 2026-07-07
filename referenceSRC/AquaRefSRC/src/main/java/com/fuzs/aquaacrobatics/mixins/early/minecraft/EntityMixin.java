package com.fuzs.aquaacrobatics.mixins.early.minecraft;

import net.minecraft.block.Block;
import net.minecraft.block.BlockVine;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.fuzs.aquaacrobatics.config.ConfigHandler;
import com.fuzs.aquaacrobatics.entity.IBubbleColumnInteractable;
import com.fuzs.aquaacrobatics.entity.Pose;
import com.fuzs.aquaacrobatics.entity.player.IPlayerResizeable;

@SuppressWarnings("unused")
@Mixin(Entity.class)
public abstract class EntityMixin implements IBubbleColumnInteractable {

    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    public double motionY;

    @Shadow
    public float fallDistance;

    @Shadow
    public World worldObj;

    @Redirect(method = "moveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
    public boolean isSneaking(Entity entity) {

        // patches two calls to allow falling off blocks when not pressing sneak key but being in crouching pose
        if (entity instanceof IPlayerResizeable) {

            return ((IPlayerResizeable) entity).isActuallySneaking();
        }

        return this.isSneaking();
    }

    /*
     * This is a bit of a hack, but it works
     * for some reason, the orignal value will make IsInWater return false even when the entity is in water
     */
    @ModifyConstant(method = "handleWaterMovement", constant = @Constant(doubleValue = -0.4000000059604645D))
    private double adjustWaterMovementY(double original) {
        return (this instanceof IPlayerResizeable && ((IPlayerResizeable) this).getPose() == Pose.SWIMMING)
            ? -0.2500000059604645D
            : original;
    }

    @Override
    public void onEnterBubbleColumn(boolean downwards) {
        if (!downwards) {
            this.motionY = Math.min(0.7, this.motionY + 0.06);
        } else this.motionY = Math.max(-0.3, this.motionY - 0.03);
        this.fallDistance = 0.0F;
    }

    @Override
    public void onEnterBubbleColumnWithAirAbove(boolean downwards) {
        if (!downwards) {
            this.motionY = Math.min(1.8, this.motionY + 0.1);
        } else this.motionY = Math.max(-0.9, this.motionY - 0.03);
    }

    @ModifyVariable(method = "moveEntity", ordinal = 0, name = "block", at = @At("LOAD"))
    private Block getFakeClimbingBlock(Block original) {
        if (ConfigHandler.MovementConfig.newClimbingBehavior && original instanceof BlockVine) return Blocks.ladder;
        return original;
    }
}
