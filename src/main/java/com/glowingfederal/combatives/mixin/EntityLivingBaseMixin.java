package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.ICombativesMovementState;
import com.glowingfederal.combatives.movement.MovementController;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin extends Entity {
    @Unique private boolean combatives$deferMovementShape;
    @Unique private float combatives$shapeStrafe;
    @Unique private float combatives$shapeForward;
    @Unique private double combatives$preMoveFlyingX;
    @Unique private double combatives$preMoveFlyingZ;
    @Unique private double combatives$postMoveFlyingX;
    @Unique private double combatives$postMoveFlyingZ;
    @Unique private double combatives$travelStartX;
    @Unique private double combatives$travelStartY;
    @Unique private double combatives$travelStartZ;
    @Unique private double combatives$travelStartMotionX;
    @Unique private double combatives$travelStartMotionY;
    @Unique private double combatives$travelStartMotionZ;
    @Unique private boolean combatives$travelStartInWater;
    @Unique private boolean combatives$travelStartOnGround;

    public EntityLivingBaseMixin(World world) {
        super(world);
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void combatives$cancelCrawlJump(CallbackInfo ci) {
        EntityLivingBase self = (EntityLivingBase) (Object) this;
        if (!(self instanceof ICombativesPlayerPose)) {
            return;
        }
        ICombativesPlayerPose pose = (ICombativesPlayerPose) self;
        if (!pose.isCrawlKeyDown()) {
            return;
        }
        if (pose.isPoseClear(Pose.STANDING)) {
            pose.setCrawlKeyDown(false);
        }
        ci.cancel();
    }

    @Redirect(method = "moveEntityWithHeading", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;isSneaking()Z"))
    private boolean combatives$useActualSneakForTravel(EntityLivingBase entity) {
        if (entity instanceof ICombativesPlayerPose) {
            return ((ICombativesPlayerPose) entity).isActuallySneaking();
        }
        return entity.isSneaking();
    }

    @Inject(method = "moveEntityWithHeading", at = @At("HEAD"))
    private void combatives$captureTravelStart(float strafe, float forward, CallbackInfo ci) {
        this.combatives$deferMovementShape = false;
        EntityLivingBase self = (EntityLivingBase) (Object) this;
        if (self instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) self;
            this.combatives$travelStartX = player.posX;
            this.combatives$travelStartY = player.posY;
            this.combatives$travelStartZ = player.posZ;
            this.combatives$travelStartMotionX = player.motionX;
            this.combatives$travelStartMotionY = player.motionY;
            this.combatives$travelStartMotionZ = player.motionZ;
            this.combatives$travelStartInWater = player.isInWater();
            this.combatives$travelStartOnGround = player.onGround;
        }
    }

    @Redirect(method = "moveEntityWithHeading", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;moveFlying(FFF)V"))
    private void combatives$captureMoveFlying(EntityLivingBase entity, float strafe, float forward, float friction) {
        if (!(entity instanceof EntityPlayer) || MovementController.shouldBypass((EntityPlayer) entity)) {
            entity.moveFlying(strafe, forward, friction);
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        this.combatives$shapeStrafe = strafe;
        this.combatives$shapeForward = forward;
        this.combatives$preMoveFlyingX = player.motionX;
        this.combatives$preMoveFlyingZ = player.motionZ;
        entity.moveFlying(strafe, forward, friction);
        this.combatives$postMoveFlyingX = player.motionX;
        this.combatives$postMoveFlyingZ = player.motionZ;
        this.combatives$deferMovementShape = true;
    }

    @Inject(method = "moveEntityWithHeading", at = @At("RETURN"))
    private void combatives$shapeAfterVanillaTravel(float strafe, float forward, CallbackInfo ci) {
        EntityLivingBase self = (EntityLivingBase) (Object) this;
        if (!(self instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) self;
        double postVanillaX = player.motionX;
        double postVanillaY = player.motionY;
        double postVanillaZ = player.motionZ;
        boolean exitedWater = this.combatives$travelStartInWater && !player.isInWater();
        double stepUp = player.posY - this.combatives$travelStartY;
        boolean acceptedVanillaStep = this.combatives$deferMovementShape && player.onGround && stepUp > 1.0E-4D && stepUp <= player.stepHeight + 0.001D;

        if (this.combatives$deferMovementShape && !acceptedVanillaStep && !MovementController.shouldBypass(player)) {
            MovementController.MovementResult result = MovementController.shape(player, this.combatives$shapeStrafe, this.combatives$shapeForward, player.rotationYaw, postVanillaX, postVanillaZ, this.combatives$postMoveFlyingX, this.combatives$postMoveFlyingZ);
            player.motionX = result.motionX;
            player.motionZ = result.motionZ;
            if (player instanceof ICombativesMovementState) {
                ((ICombativesMovementState) player).setCombativesMovementSnapshot(result.snapshot);
            }
            this.combatives$logTravelDiagnostics(player, postVanillaX, postVanillaY, postVanillaZ, player.motionX, player.motionZ, stepUp, acceptedVanillaStep, exitedWater, true, result);
        } else {
            this.combatives$logTravelDiagnostics(player, postVanillaX, postVanillaY, postVanillaZ, postVanillaX, postVanillaZ, stepUp, acceptedVanillaStep, exitedWater, false, null);
        }
        this.combatives$deferMovementShape = false;
    }

    @Unique
    private void combatives$logTravelDiagnostics(EntityPlayer player, double postVanillaX, double postVanillaY, double postVanillaZ, double finalX, double finalZ, double stepUp, boolean acceptedVanillaStep, boolean exitedWater, boolean shaped, MovementController.MovementResult result) {
        if (!MovementDiagnostics.isVerboseEnabled()) {
            return;
        }
        boolean relevantStep = acceptedVanillaStep || (stepUp > 1.0E-4D && stepUp <= player.stepHeight + 0.001D) || player.isCollidedHorizontally;
        boolean relevantWater = this.combatives$travelStartInWater || player.isInWater() || exitedWater;
        if (!relevantStep && !relevantWater) {
            return;
        }
        boolean swimming = player instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) player).isSwimming();
        boolean crawling = player instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) player).getPose() == Pose.SWIMMING && !swimming;
        MovementDiagnostics.verbose(player, "travel transition startPos=(" + this.combatives$travelStartX + "," + this.combatives$travelStartY + "," + this.combatives$travelStartZ + ") endPos=(" + player.posX + "," + player.posY + "," + player.posZ + ") stepUp=" + stepUp + " inWater=" + this.combatives$travelStartInWater + "->" + player.isInWater() + " exitedWater=" + exitedWater + " crawl=" + crawling + " swim=" + swimming + " onGround=" + this.combatives$travelStartOnGround + "->" + player.onGround + " collidedH=" + player.isCollidedHorizontally + " collidedV=" + player.isCollidedVertically + " motionStart=(" + this.combatives$travelStartMotionX + "," + this.combatives$travelStartMotionY + "," + this.combatives$travelStartMotionZ + ") motionAfterVanilla=(" + postVanillaX + "," + postVanillaY + "," + postVanillaZ + ") motionFinal=(" + finalX + "," + player.motionY + "," + finalZ + ") profile=" + (result == null ? MovementController.selectProfile(player) : result.profile) + " targetHorizontalSpeed=" + (result == null ? 0.0D : result.targetSpeed) + " accelerationLimit=" + (result == null ? 0.0D : result.accelerationLimit) + " appliedHorizontalDelta=" + (result == null ? 0.0D : result.appliedHorizontalDelta) + " drag=" + (result == null ? 1.0D : result.appliedDrag) + " clampRan=" + (result != null && result.speedClampRan) + " inputMag=" + Math.sqrt(this.combatives$shapeStrafe * this.combatives$shapeStrafe + this.combatives$shapeForward * this.combatives$shapeForward) + " acceptedVanillaStep=" + acceptedVanillaStep + " shaped=" + shaped);
    }
}
