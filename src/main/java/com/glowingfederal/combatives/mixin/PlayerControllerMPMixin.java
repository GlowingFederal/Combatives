package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.client.ICombativesClientPlayerSwimming;
import com.glowingfederal.combatives.client.InteractionDiagnostics;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public abstract class PlayerControllerMPMixin {
    @Inject(method = "clickBlock", at = @At("HEAD"))
    private void combatives$traceInitialLeftClick(int x, int y, int z, int face,
            CallbackInfoReturnable<Boolean> cir) {
        InteractionDiagnostics.logLeftController(x, y, z, face, false);
    }

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"))
    private void combatives$traceHeldLeftClick(int x, int y, int z, int face,
            CallbackInfoReturnable<Boolean> cir) {
        InteractionDiagnostics.logLeftController(x, y, z, face, true);
    }

    @Inject(method = "onPlayerRightClick", at = @At("HEAD"))
    private void combatives$traceRightClick(EntityPlayer player, net.minecraft.world.World world,
            ItemStack stack, int x, int y, int z, int face, Vec3 hit,
            CallbackInfoReturnable<Boolean> cir) {
        InteractionDiagnostics.logRightController(x, y, z, face,
                (float) (hit.xCoord - x), (float) (hit.yCoord - y), (float) (hit.zCoord - z));
    }

    @Redirect(method = "onPlayerRightClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isSneaking()Z"))
    private boolean combatives$useActualSneakForRightClick(EntityPlayer player) {
        return player instanceof ICombativesClientPlayerSwimming
            ? ((ICombativesClientPlayerSwimming) player).isActuallySneaking()
            : player.isSneaking();
    }
}
