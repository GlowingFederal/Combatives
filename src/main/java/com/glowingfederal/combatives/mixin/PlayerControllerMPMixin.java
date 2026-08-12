package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.client.ICombativesClientPlayerSwimming;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.glowingfederal.combatives.client.InteractionDiagnostics;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

@Mixin(PlayerControllerMP.class)
public abstract class PlayerControllerMPMixin {
    @Inject(method = "clickBlock", at = @At("HEAD"))
    private void combatives$traceClickBlock(int x, int y, int z, int face,
            CallbackInfoReturnable<Boolean> cir) {
        InteractionDiagnostics.logDigRequest("START_DESTROY_BLOCK", x, y, z, face);
    }

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"))
    private void combatives$traceDamageBlock(int x, int y, int z, int face,
            CallbackInfoReturnable<Boolean> cir) {
        InteractionDiagnostics.logDigRequest("CONTINUE_DESTROY_BLOCK", x, y, z, face);
    }

    @Inject(method = "onPlayerRightClick", at = @At("HEAD"))
    private void combatives$traceRightClick(EntityPlayer player, net.minecraft.world.World world,
            ItemStack stack, int x, int y, int z, int face, Vec3 hit,
            CallbackInfoReturnable<Boolean> cir) {
        InteractionDiagnostics.logInteraction("RIGHT_CLICK_BLOCK", x, y, z, face, hit);
    }

    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void combatives$traceAttack(EntityPlayer player, Entity target,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        InteractionDiagnostics.logEntityAttack(target);
    }

    @Redirect(method = "onPlayerRightClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isSneaking()Z"))
    private boolean combatives$useActualSneakForRightClick(EntityPlayer player) {
        return player instanceof ICombativesClientPlayerSwimming
            ? ((ICombativesClientPlayerSwimming) player).isActuallySneaking()
            : player.isSneaking();
    }
}
