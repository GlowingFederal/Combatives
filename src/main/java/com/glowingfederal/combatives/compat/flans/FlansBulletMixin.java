package com.glowingfederal.combatives.compat.flans;

import com.glowingfederal.combatives.combat.CombatHitResult;
import com.glowingfederal.combatives.combat.CombatHitVolumes;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.interaction.InteractionRay;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Only the handheld constructor: mounted/explicit-origin shots retain their own origin. */
@Pseudo
@Mixin(targets = "com.flansmod.common.guns.EntityBullet", remap = false)
public abstract class FlansBulletMixin {
    @Unique private EntityPlayer combatives$fallbackPlayer;

    @Redirect(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;FFLcom/flansmod/common/guns/BulletType;FZLcom/flansmod/common/types/InfoType;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Vec3;createVectorHelper(DDD)Lnet/minecraft/util/Vec3;", remap = true))
    private static Vec3 combatives$handheldOrigin(double x, double y, double z, World world,
            EntityLivingBase shooter, float spread, float damage, @Coerce Object bulletType,
            float speed, boolean shotgun, @Coerce Object shotFrom) {
        if (shooter instanceof EntityPlayer && shooter instanceof ICombativesPlayerPose && !shooter.isRiding()) {
            return InteractionRay.authoritative((EntityPlayer) shooter).origin;
        }
        return Vec3.createVectorHelper(x, y, z);
    }

    @Redirect(method = {"onUpdate", "func_70071_h_"}, require = 0,
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;boundingBox:Lnet/minecraft/util/AxisAlignedBB;", remap = true))
    private AxisAlignedBB combatives$captureFallbackPlayer(EntityPlayer player) {
        this.combatives$fallbackPlayer = player;
        return player.boundingBox;
    }

    @Redirect(method = {"onUpdate", "func_70071_h_"}, require = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/AxisAlignedBB;calculateIntercept(Lnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;)Lnet/minecraft/util/MovingObjectPosition;",
                    remap = true))
    private MovingObjectPosition combatives$fallbackCombatVolumes(AxisAlignedBB box, Vec3 start, Vec3 end) {
        EntityPlayer player = this.combatives$fallbackPlayer;
        this.combatives$fallbackPlayer = null;
        if (!CombatHitVolumes.supports(player)) return box.calculateIntercept(start, end);
        double inflation = Math.max(0.0D,
                ((box.maxX - box.minX) - (player.boundingBox.maxX - player.boundingBox.minX)) * 0.5D);
        CombatHitResult hit = CombatHitVolumes.intersect(player, start, end, inflation);
        return hit == null ? null : new MovingObjectPosition(player, hit.hitVec);
    }
}
