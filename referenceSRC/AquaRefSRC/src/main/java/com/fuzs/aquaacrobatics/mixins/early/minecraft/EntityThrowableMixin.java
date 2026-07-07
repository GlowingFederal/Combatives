package com.fuzs.aquaacrobatics.mixins.early.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fuzs.aquaacrobatics.config.ConfigHandler;

@Mixin(EntityThrowable.class)
public abstract class EntityThrowableMixin extends Entity {

    public EntityThrowableMixin(World worldIn) {
        super(worldIn);
    }

    private final boolean aqua$isNewProjectile = aqua$checkEntityEligibleForProjectile();

    private boolean aqua$checkEntityEligibleForProjectile() {
        return ConfigHandler.MovementConfig.newProjectileBehavior && getClass().getName()
            .startsWith("net.minecraft.");
    }

    @Redirect(
        method = "onUpdate",
        at = @At(
            value = "INVOKE",
            target = "net/minecraft/world/World.rayTraceBlocks(Lnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;)Lnet/minecraft/util/MovingObjectPosition;"))
    private MovingObjectPosition rayTraceThroughLiquid(World world, Vec3 start, Vec3 end) {
        if (aqua$isNewProjectile) return world.func_147447_a(start, end, false, true, false);
        else return world.rayTraceBlocks(start, end);
    }

    @Inject(
        method = "onUpdate",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/projectile/EntityThrowable;posX:D",
            opcode = Opcodes.PUTFIELD,
            ordinal = 0))
    private void doCheckBlockCollision(CallbackInfo ci) {
        if (aqua$isNewProjectile) this.func_145775_I();
    }
}
