package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityOtherPlayerMP.class)
public abstract class EntityOtherPlayerMPMixin {
    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void combatives$applyRemoteYOffset(CallbackInfo ci) {
        EntityOtherPlayerMP player = (EntityOtherPlayerMP) (Object) this;
        if (player instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) player).getPose() == Pose.SWIMMING) {
            player.yOffset = 0.28F;
        } else {
            player.yOffset = 0.0F;
        }
    }
}
