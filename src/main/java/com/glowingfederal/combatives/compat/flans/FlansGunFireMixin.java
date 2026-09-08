package com.glowingfederal.combatives.compat.flans;

import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Gun-button packets do not temporarily replace the server's accepted view orientation. */
@Pseudo
@Mixin(targets = "com.flansmod.common.network.PacketGunFire", remap = false)
public abstract class FlansGunFireMixin {
    @Redirect(method = "handleServerSide", at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/player/EntityPlayerMP;rotationYaw:F", opcode = 181, remap = true))
    private void combatives$keepAcceptedYaw(EntityPlayerMP player, float ignored) { }

    @Redirect(method = "handleServerSide", at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/player/EntityPlayerMP;rotationPitch:F", opcode = 181, remap = true))
    private void combatives$keepAcceptedPitch(EntityPlayerMP player, float ignored) { }
}
