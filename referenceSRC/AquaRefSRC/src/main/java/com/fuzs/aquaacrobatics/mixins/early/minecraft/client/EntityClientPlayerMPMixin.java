package com.fuzs.aquaacrobatics.mixins.early.minecraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.Session;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.fuzs.aquaacrobatics.client.entity.IPlayerSPSwimming;
import com.fuzs.aquaacrobatics.config.ConfigHandler;
import com.fuzs.aquaacrobatics.entity.player.IPlayerResizeable;

// Unlike 1.12 where everything is in EntityPlayerSP, 1.7.10 has EntityPlayerSP and EntityClientPlayerMP for some reason
@Mixin(EntityClientPlayerMP.class)
public class EntityClientPlayerMPMixin extends EntityPlayerSP implements IPlayerSPSwimming {

    public EntityClientPlayerMPMixin(Minecraft mc, World world, Session session, NetHandlerPlayClient client,
        StatFileWriter statFileWriter) {

        super(mc, world, session, 0);
    }

    @Redirect(
        method = "sendMotionUpdates",
        at = @At(value = "INVOKE", target = "net/minecraft/client/entity/EntityClientPlayerMP.isSneaking()Z"))
    private boolean onUpdateWalkingPlayerIsSneaking(EntityClientPlayerMP playerIn) {

        // send actual sneak state to server
        return this.isActuallySneaking();
    }

    @Override
    public boolean isActuallySneaking() {

        // switched with #isSneaking
        return this.movementInput != null && this.movementInput.sneak;
    }

    @Override
    public boolean isForcedDown() {

        return ((IPlayerResizeable) this).isResizingAllowed() && !this.capabilities.isFlying
            ? this.isSneaking() || ((IPlayerResizeable) this).isVisuallySwimming()
            : this.isActuallySneaking();
    }

    @Override
    public boolean isUsingSwimmingAnimation() {

        return this.isUsingSwimmingAnimation(this.movementInput.moveForward, this.movementInput.moveStrafe);
    }

    @Override
    public boolean isUsingSwimmingAnimation(float moveForward, float moveStrafe) {

        if (this.canSwim()) {

            return this.isMovingForward(moveForward, moveStrafe);
        }

        if (ConfigHandler.MovementConfig.sidewaysSprinting) {

            return moveForward >= 0.8F || Math.abs(moveStrafe) > 0.8F;
        }

        return moveForward >= 0.8F;
    }

    @Override
    public boolean canSwim() {

        return ((IPlayerResizeable) this).getEyesInWaterPlayer();
    }

    @Override
    public boolean isMovingForward(float moveForward, float moveStrafe) {

        if (moveForward > 1.0E-5F) {

            return true;
        } else if (ConfigHandler.MovementConfig.sidewaysSwimming) {

            return Math.abs(moveStrafe) > 1.0E-5F;
        }

        return false;
    }

    @Override
    public boolean canPerformElytraTakeoff() {
        return false;
    }
}
