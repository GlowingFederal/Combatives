package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.entity.EntitySize;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.network.PoseSync;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin extends EntityPlayer {
    public EntityPlayerMPMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void combatives$setDyingPose(DamageSource cause, CallbackInfo ci) {
        ((ICombativesPlayerPose) this).setPose(Pose.DYING);
        MovementDiagnostics.debug(this, "server accepted pose DYING after death");
        PoseSync.broadcastAuthoritativePose((EntityPlayerMP) (Object) this, true);
    }

    @Override
    public float getDefaultEyeHeight() {
        ICombativesPlayerPose pose = (ICombativesPlayerPose) this;
        Pose currentPose = pose.getPose();
        return pose.getStandingEyeHeight(currentPose, pose.getSize(currentPose));
    }

    @Override
    public float getEyeHeight() {
        return this.getDefaultEyeHeight();
    }

    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void combatives$syncServerSize(CallbackInfo ci) {
        ICombativesPlayerPose pose = (ICombativesPlayerPose) this;
        EntitySize size = pose.getSize(pose.getPose());
        if (this.width != size.width || this.height != size.height) {
            this.setSize(size.width, size.height);
            MovementDiagnostics.debug(this, "server enforced size for " + pose.getPose() + " size=" + size.width + "x" + size.height);
        }
    }
}
