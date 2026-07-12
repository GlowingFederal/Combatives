package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.ICombativesMovementState;
import com.glowingfederal.combatives.movement.MovementController;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.movement.MovementProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin extends Entity {
    @Shadow protected boolean isJumping;

    @Unique private double combatives$travelStartX;
    @Unique private double combatives$travelStartY;
    @Unique private double combatives$travelStartZ;
    @Unique private double combatives$travelStartMotionX;
    @Unique private double combatives$travelStartMotionY;
    @Unique private double combatives$travelStartMotionZ;
    @Unique private boolean combatives$travelStartOnGround;
    @Unique private boolean combatives$travelStartInWater;
    @Unique private MovementProfile combatives$travelStartProfile = MovementProfile.STANDING;
    @Unique private double combatives$preCollisionIntentX;
    @Unique private double combatives$preCollisionIntentZ;
    @Unique private double combatives$afterMoveFlyingSpeed;
    @Unique private double combatives$afterShapeSpeed;
    @Unique private int combatives$lastAcceptedStepTick = -1000;
    @Unique private double combatives$lastAcceptedStepY;
    @Unique private double combatives$lastAcceptedStepBaselineX;
    @Unique private double combatives$lastAcceptedStepBaselineZ;
    @Unique private MovementProfile combatives$lastAcceptedStepProfile = MovementProfile.STANDING;

    public EntityLivingBaseMixin(World world) {
        super(world);
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void combatives$cancelCrawlJump(CallbackInfo ci) {
        EntityLivingBase self = (EntityLivingBase) (Object) this;
        this.combatives$logVerticalStage("jump:head", self instanceof EntityPlayer ? (EntityPlayer) self : null, false, 0.0D);
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
        MovementDiagnostics.verbose((EntityPlayer) self, "vertical stage=jump:cancel crawlKeyDown=true pose=" + pose.getPose() + " motionY=" + self.motionY + " onGround=" + self.onGround + " inWater=" + self.isInWater() + " bbox=" + self.boundingBox);
        ci.cancel();
    }

    @Inject(method = "jump", at = @At("RETURN"))
    private void combatives$afterJump(CallbackInfo ci) {
        EntityLivingBase self = (EntityLivingBase) (Object) this;
        this.combatives$logVerticalStage("jump:return", self instanceof EntityPlayer ? (EntityPlayer) self : null, false, 0.0D);
    }

    @Inject(method = "moveEntityWithHeading", at = @At("HEAD"))
    private void combatives$captureMoveEntityWithHeadingHead(float strafe, float forward, CallbackInfo ci) {
        EntityLivingBase self = (EntityLivingBase) (Object) this;
        if (!(self instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) self;
        this.combatives$travelStartX = player.posX;
        this.combatives$travelStartY = player.posY;
        this.combatives$travelStartZ = player.posZ;
        this.combatives$travelStartMotionX = player.motionX;
        this.combatives$travelStartMotionY = player.motionY;
        this.combatives$travelStartMotionZ = player.motionZ;
        this.combatives$travelStartOnGround = player.onGround;
        this.combatives$travelStartInWater = player.isInWater();
        this.combatives$travelStartProfile = MovementController.selectProfile(player);
        this.combatives$preCollisionIntentX = player.motionX;
        this.combatives$preCollisionIntentZ = player.motionZ;
        this.combatives$afterMoveFlyingSpeed = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
        this.combatives$afterShapeSpeed = this.combatives$afterMoveFlyingSpeed;
        this.combatives$logVerticalStage("travel:head", player, false, 0.0D);
    }

    @Inject(method = "moveEntityWithHeading", at = @At("RETURN"))
    private void combatives$captureMoveEntityWithHeadingReturn(float strafe, float forward, CallbackInfo ci) {
        EntityLivingBase self = (EntityLivingBase) (Object) this;
        if (self instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) self;
            this.combatives$updateAcceptedStepBaseline(player);
            this.combatives$logVerticalStage("travel:return", player, false, 0.0D);
        }
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
            double beforeY = entity.motionY;
            entity.moveFlying(strafe, forward, friction);
            this.combatives$logVerticalWriteCheck("moveFlying:bypass", entity instanceof EntityPlayer ? (EntityPlayer) entity : null, beforeY, entity.motionY);
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        this.combatives$restoreAcceptedStepBaseline(player, strafe, forward);
        double currentX = player.motionX;
        double currentZ = player.motionZ;
        double entrySpeed = Math.sqrt(currentX * currentX + currentZ * currentZ);
        double beforeMoveFlyingY = player.motionY;
        this.combatives$logVerticalStage("moveFlying:before", player, false, 0.0D);
        entity.moveFlying(strafe, forward, friction);
        this.combatives$afterMoveFlyingSpeed = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
        this.combatives$logVerticalWriteCheck("moveFlying:vanilla", player, beforeMoveFlyingY, player.motionY);
        double beforeShapeY = player.motionY;
        MovementController.MovementResult result = MovementController.shape(player, strafe, forward, player.rotationYaw, currentX, currentZ, player.motionX, player.motionZ);
        player.motionX = result.motionX;
        player.motionZ = result.motionZ;
        this.combatives$preCollisionIntentX = result.motionX;
        this.combatives$preCollisionIntentZ = result.motionZ;
        this.combatives$afterShapeSpeed = Math.sqrt(result.motionX * result.motionX + result.motionZ * result.motionZ);
        this.combatives$logStepTrace("shape", player, entrySpeed, result);
        this.combatives$logVerticalWriteCheck("moveFlying:combatives-horizontal-shape", player, beforeShapeY, player.motionY);
        if (player instanceof ICombativesMovementState) {
            ((ICombativesMovementState) player).setCombativesMovementSnapshot(result.snapshot);
        }
        boolean swimming = player instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) player).isSwimming();
        boolean crawling = player instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) player).getPose() == Pose.SWIMMING && !swimming;
    }

    @Unique
    private void combatives$logVerticalWriteCheck(String stage, EntityPlayer player, double beforeY, double afterY) {
        if (player != null && Math.abs(afterY - beforeY) > 1.0E-9D) {
            MovementDiagnostics.verbose(player, "vertical motion changed during horizontal-only stage=" + stage + " beforeY=" + beforeY + " afterY=" + afterY + " delta=" + (afterY - beforeY));
        }
    }

    @Unique
    private void combatives$logVerticalStage(String stage, EntityPlayer player, boolean force, double previousY) {
        if (player == null || !MovementDiagnostics.isVerboseEnabled()) {
            return;
        }
        boolean swimming = player instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) player).isSwimming();
        boolean crawling = player instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) player).getPose() == Pose.SWIMMING && !swimming;
        boolean relevant = force || player.isInWater() || this.combatives$travelStartInWater || Math.abs(player.motionY) > 1.0E-6D || player.isCollided || player.isCollidedHorizontally || player.isCollidedVertically || player.onGround != this.combatives$travelStartOnGround || Math.abs(player.posY - this.combatives$travelStartY) > 1.0E-4D;
        if (!relevant) {
            return;
        }
        MovementDiagnostics.verbose(player, "vertical stage=" + stage + " pos=(" + player.posX + "," + player.posY + "," + player.posZ + ") deltaPos=(" + (player.posX - this.combatives$travelStartX) + "," + (player.posY - this.combatives$travelStartY) + "," + (player.posZ - this.combatives$travelStartZ) + ") motion=(" + player.motionX + "," + player.motionY + "," + player.motionZ + ") startMotion=(" + this.combatives$travelStartMotionX + "," + this.combatives$travelStartMotionY + "," + this.combatives$travelStartMotionZ + ") onGround=" + this.combatives$travelStartOnGround + "->" + player.onGround + " collided=" + player.isCollided + " collidedH=" + player.isCollidedHorizontally + " collidedV=" + player.isCollidedVertically + " fallDistance=" + player.fallDistance + " stepHeight=" + player.stepHeight + " inWater=" + this.combatives$travelStartInWater + "->" + player.isInWater() + " isJumping=" + this.isJumping + " crawl=" + crawling + " swim=" + swimming + " profile=" + this.combatives$travelStartProfile + "->" + MovementController.selectProfile(player) + " bbox=" + player.boundingBox);
    }

    @Unique
    private void combatives$restoreAcceptedStepBaseline(EntityPlayer player, float strafe, float forward) {
        if (player.ticksExisted != this.combatives$lastAcceptedStepTick + 1 || !player.onGround || MovementController.shouldBypass(player)) {
            return;
        }
        if (strafe * strafe + forward * forward <= 1.0E-4F) {
            return;
        }
        double currentSpeed = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
        double storedSpeed = Math.sqrt(this.combatives$lastAcceptedStepBaselineX * this.combatives$lastAcceptedStepBaselineX + this.combatives$lastAcceptedStepBaselineZ * this.combatives$lastAcceptedStepBaselineZ);
        if (storedSpeed <= 1.0E-6D || currentSpeed >= storedSpeed * 0.75D) {
            return;
        }
        player.motionX = this.combatives$lastAcceptedStepBaselineX;
        player.motionZ = this.combatives$lastAcceptedStepBaselineZ;
        MovementDiagnostics.verbose(player, "stair step baseline restored tick=" + player.ticksExisted + " previousStepTick=" + this.combatives$lastAcceptedStepTick + " stepY=" + this.combatives$lastAcceptedStepY + " currentSpeed=" + currentSpeed + " restoredSpeed=" + storedSpeed + " previousProfile=" + this.combatives$lastAcceptedStepProfile + " currentProfile=" + MovementController.selectProfile(player));
    }

    @Unique
    private void combatives$updateAcceptedStepBaseline(EntityPlayer player) {
        double stepY = player.posY - this.combatives$travelStartY;
        boolean acceptedStep = this.combatives$travelStartOnGround && player.onGround && stepY > 1.0E-4D && stepY <= player.stepHeight + 0.001D && this.combatives$afterShapeSpeed > 1.0E-6D;
        if (!acceptedStep) {
            return;
        }
        float friction = this.worldObj.getBlock(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.boundingBox.minY) - 1, MathHelper.floor_double(player.posZ)).slipperiness * 0.91F;
        this.combatives$lastAcceptedStepTick = player.ticksExisted;
        this.combatives$lastAcceptedStepY = stepY;
        this.combatives$lastAcceptedStepBaselineX = this.combatives$preCollisionIntentX * friction;
        this.combatives$lastAcceptedStepBaselineZ = this.combatives$preCollisionIntentZ * friction;
        this.combatives$lastAcceptedStepProfile = this.combatives$travelStartProfile;
        this.combatives$logStepTrace("accepted-step", player, Math.sqrt(this.combatives$travelStartMotionX * this.combatives$travelStartMotionX + this.combatives$travelStartMotionZ * this.combatives$travelStartMotionZ), null);
    }

    @Unique
    private void combatives$logStepTrace(String stage, EntityPlayer player, double entrySpeed, MovementController.MovementResult result) {
        if (!MovementDiagnostics.isVerboseEnabled()) {
            return;
        }
        double stepY = player.posY - this.combatives$travelStartY;
        boolean nearStep = stepY > 1.0E-4D && stepY <= player.stepHeight + 0.001D;
        boolean profileFlip = this.combatives$travelStartProfile != MovementController.selectProfile(player);
        if (!nearStep && !profileFlip && !player.isCollidedHorizontally) {
            return;
        }
        MovementDiagnostics.verbose(player, "stair trace stage=" + stage + " tick=" + player.ticksExisted + " y=" + player.posY + " dy=" + stepY + " motion=(" + player.motionX + "," + player.motionY + "," + player.motionZ + ") entrySpeed=" + entrySpeed + " afterMoveFlyingSpeed=" + this.combatives$afterMoveFlyingSpeed + " afterShapeSpeed=" + this.combatives$afterShapeSpeed + " onGround=" + this.combatives$travelStartOnGround + "->" + player.onGround + " collidedH=" + player.isCollidedHorizontally + " collidedV=" + player.isCollidedVertically + " profile=" + this.combatives$travelStartProfile + "->" + MovementController.selectProfile(player) + " landing=" + (player instanceof ICombativesMovementState && ((ICombativesMovementState) player).getCombativesMovementSnapshot().landingImpact) + " accel=" + (result == null ? 0.0D : result.accelerationLimit) + " drag=" + (result == null ? 1.0D : result.appliedDrag) + " appliedDelta=" + (result == null ? 0.0D : result.appliedHorizontalDelta) + " clamp=" + (result != null && result.speedClampRan) + " sprint=" + player.isSprinting() + " stepWithinHeight=" + nearStep);
    }

    @Unique
    private void combatives$logVerticalWriteCheck(String stage, EntityPlayer player, double beforeY, double afterY) {
        if (player != null && Math.abs(afterY - beforeY) > 1.0E-9D) {
            MovementDiagnostics.verbose(player, "vertical motion changed during horizontal-only stage=" + stage + " beforeY=" + beforeY + " afterY=" + afterY + " delta=" + (afterY - beforeY));
        }
    }

    @Unique
    private void combatives$logVerticalStage(String stage, EntityPlayer player, boolean force, double previousY) {
        if (player == null || !MovementDiagnostics.isVerboseEnabled()) {
            return;
        }
        boolean swimming = player instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) player).isSwimming();
        boolean crawling = player instanceof ICombativesPlayerPose && ((ICombativesPlayerPose) player).getPose() == Pose.SWIMMING && !swimming;
        boolean relevant = force || player.isInWater() || this.combatives$travelStartInWater || Math.abs(player.motionY) > 1.0E-6D || player.isCollided || player.isCollidedHorizontally || player.isCollidedVertically || player.onGround != this.combatives$travelStartOnGround || Math.abs(player.posY - this.combatives$travelStartY) > 1.0E-4D;
        if (!relevant) {
            return;
        }
        MovementDiagnostics.verbose(player, "vertical stage=" + stage + " pos=(" + player.posX + "," + player.posY + "," + player.posZ + ") deltaPos=(" + (player.posX - this.combatives$travelStartX) + "," + (player.posY - this.combatives$travelStartY) + "," + (player.posZ - this.combatives$travelStartZ) + ") motion=(" + player.motionX + "," + player.motionY + "," + player.motionZ + ") startMotion=(" + this.combatives$travelStartMotionX + "," + this.combatives$travelStartMotionY + "," + this.combatives$travelStartMotionZ + ") onGround=" + this.combatives$travelStartOnGround + "->" + player.onGround + " collided=" + player.isCollided + " collidedH=" + player.isCollidedHorizontally + " collidedV=" + player.isCollidedVertically + " fallDistance=" + player.fallDistance + " stepHeight=" + player.stepHeight + " inWater=" + this.combatives$travelStartInWater + "->" + player.isInWater() + " isJumping=" + this.isJumping + " crawl=" + crawling + " swim=" + swimming + " profile=" + this.combatives$travelStartProfile + "->" + MovementController.selectProfile(player) + " bbox=" + player.boundingBox);
    }
}
