package com.glowingfederal.combatives.compat.flans;

import com.glowingfederal.combatives.compat.FlansSnapshotPose;

import com.glowingfederal.combatives.movement.ICombativesLocomotion;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Snapshot geometry uses the same leaning orientation as the player renderer. */
@Pseudo
@Mixin(targets = "com.flansmod.common.guns.raytracing.PlayerSnapshot", remap = false)
public abstract class FlansSnapshotMixin {
    @Unique private static boolean combatives$leaning(EntityPlayer player) {
        return player instanceof ICombativesLocomotion && !player.isRiding()
                && ((ICombativesLocomotion) player).getLean() != 0.0F;
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/player/EntityPlayer;renderYawOffset:F", remap = true))
    private float combatives$bodyYaw(EntityPlayer player) {
        return combatives$leaning(player) ? player.rotationYaw : player.renderYawOffset;
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/player/EntityPlayer;rotationYawHead:F", remap = true))
    private float combatives$headYaw(EntityPlayer player) {
        return combatives$leaning(player) ? player.rotationYaw : player.rotationYawHead;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void combatives$poseSnapshot(EntityPlayer player, CallbackInfo ci) {
        if (combatives$leaning(player)) FlansSnapshotPose.apply(this, player);
    }
}
