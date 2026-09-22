package com.glowingfederal.combatives.compat.flans;

import com.glowingfederal.combatives.interaction.InteractionRay;
import com.glowingfederal.combatives.movement.ICombativesLocomotion;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Optional Ultimate gun paths; pack animation and deployable facing rules are preserved. */
@Pseudo
@Mixin(targets = "com.flansmod.common.guns.ItemGun", remap = false)
public abstract class FlansItemGunMixin {
    @Unique private static boolean combatives$leaning(EntityPlayer player) {
        return player instanceof ICombativesLocomotion && !player.isRiding()
                && ((ICombativesLocomotion) player).getAcceptedLean() != 0.0F;
    }

    @Redirect(method = {"onItemRightClick", "func_77659_a"}, require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;rayTraceBlocks(Lnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;Z)Lnet/minecraft/util/MovingObjectPosition;", remap = true))
    private MovingObjectPosition combatives$placementRay(World world, Vec3 origin, Vec3 end,
            boolean liquids, ItemStack stack, World ignored, EntityPlayer player) {
        if (combatives$leaning(player)) {
            InteractionRay ray = InteractionRay.authoritative(player);
            return world.rayTraceBlocks(ray.origin, ray.end(origin.distanceTo(end)), liquids);
        }
        return world.rayTraceBlocks(origin, end, liquids);
    }

    @Redirect(method = {"checkForLockOn", "checkForMelee"}, require = 0,
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;posX:D", remap = true))
    private double combatives$leanX(EntityPlayer player) {
        return combatives$leaning(player) ? InteractionRay.authoritative(player).origin.xCoord : player.posX;
    }

    @Redirect(method = {"checkForLockOn", "checkForMelee"}, require = 0,
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;posZ:D", remap = true))
    private double combatives$leanZ(EntityPlayer player) {
        return combatives$leaning(player) ? InteractionRay.authoritative(player).origin.zCoord : player.posZ;
    }

    @Redirect(method = "checkForLockOn", require = 0,
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;posY:D", remap = true))
    private double combatives$lockOnEyeY(EntityPlayer player) {
        return combatives$leaning(player) ? InteractionRay.authoritative(player).origin.yCoord : player.posY;
    }

    @Redirect(method = "checkForLockOn", require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;getLookVec()Lnet/minecraft/util/Vec3;", remap = true))
    private Vec3 combatives$lockOnDirection(EntityPlayer player) {
        return combatives$leaning(player) ? InteractionRay.authoritative(player).direction : player.getLookVec();
    }
}
