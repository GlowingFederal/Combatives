package com.glowingfederal.combatives.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker("isSneaking")
    boolean combatives$invokeIsSneaking();

    @Invoker("moveFlying")
    void combatives$invokeMoveFlying(float strafe, float forward, float friction);
}
