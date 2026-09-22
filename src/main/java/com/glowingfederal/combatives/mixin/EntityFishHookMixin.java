package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.combat.CombatHitVolumes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityFishHook.class)
public abstract class EntityFishHookMixin {
    @Unique private Entity combatives$combatCandidate;

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;canBeCollidedWith()Z"))
    private boolean combatives$captureCombatCandidate(Entity entity) {
        this.combatives$combatCandidate = entity;
        return entity.canBeCollidedWith();
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/AxisAlignedBB;calculateIntercept(Lnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;)Lnet/minecraft/util/MovingObjectPosition;"))
    private MovingObjectPosition combatives$intersectCombatVolumes(AxisAlignedBB box, Vec3 start, Vec3 end) {
        return CombatHitVolumes.intercept(this.combatives$combatCandidate, box, start, end, 0.3D);
    }
}
