package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.client.model.ICombativesModelBipedSwimming;
import com.glowingfederal.combatives.client.model.ICombativesLeanModel;
import com.glowingfederal.combatives.client.model.LeanVisualPose;
import com.glowingfederal.combatives.client.render.CombativesVisualPoseHelper;
import com.glowingfederal.combatives.client.render.CrawlPoseAnimator;
import com.glowingfederal.combatives.client.render.SlidePoseAnimator;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.util.math.MathHelperNew;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBiped.class)
public abstract class ModelBipedMixin extends ModelBase implements ICombativesModelBipedSwimming, ICombativesLeanModel {
    @Inject(method = "setRotationAngles", at = @At("HEAD"))
    private void combatives$beginAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scaleFactor, Entity entity, CallbackInfo ci) {
        // Keep the pose through arm.postRender, then remove it before vanilla recomputes angles.
        this.combatives$restoreLeanBase();
        this.combatives$restoreCrawlLegBase();
    }
    @Shadow public ModelRenderer bipedHead;
    @Shadow public ModelRenderer bipedHeadwear;
    @Shadow public ModelRenderer bipedBody;
    @Shadow public ModelRenderer bipedRightArm;
    @Shadow public ModelRenderer bipedLeftArm;
    @Shadow public ModelRenderer bipedRightLeg;
    @Shadow public ModelRenderer bipedLeftLeg;

    @Unique private float combatives$swimAnimation;
    @Unique private boolean combatives$crawlLegBaseCaptured;
    @Unique private float combatives$leftLegBaseX;
    @Unique private float combatives$leftLegBaseY;
    @Unique private float combatives$leftLegBaseZ;
    @Unique private float combatives$rightLegBaseX;
    @Unique private float combatives$rightLegBaseY;
    @Unique private float combatives$rightLegBaseZ;
    @Unique private boolean combatives$leanBaseCaptured;
    @Unique private float combatives$bodyBaseZ;
    @Unique private float combatives$headBaseZ;
    @Unique private float combatives$headwearBaseZ;
    @Unique private float combatives$leftArmBaseZ;
    @Unique private float combatives$rightArmBaseZ;
    @Unique private float combatives$leftLegLeanBaseZ;
    @Unique private float combatives$rightLegLeanBaseZ;
    @Unique private float combatives$bodyLeanBasePointX;
    @Unique private float combatives$bodyLeanBasePointY;
    @Unique private float combatives$headLeanBasePointX;
    @Unique private float combatives$headLeanBasePointY;
    @Unique private float combatives$headwearLeanBasePointX;
    @Unique private float combatives$headwearLeanBasePointY;
    @Unique private float combatives$leftArmLeanBasePointX;
    @Unique private float combatives$leftArmLeanBasePointY;
    @Unique private float combatives$rightArmLeanBasePointX;
    @Unique private float combatives$rightArmLeanBasePointY;
    @Unique private float combatives$leftLegLeanBasePointX;
    @Unique private float combatives$leftLegLeanBasePointY;
    @Unique private float combatives$rightLegLeanBasePointX;
    @Unique private float combatives$rightLegLeanBasePointY;

    // Custom armour calls setRotationAngles directly, bypassing ModelBiped.render.
    @ModifyVariable(method = "setRotationAngles", at = @At("HEAD"), argsOnly = true, ordinal = 4)
    private float combatives$resolveHeadPitch(float headPitch, float limbSwing, float limbSwingAmount, float ageInTicks,
        float netHeadYaw, float originalHeadPitch, float scaleFactor, Entity entity) {
        if (entity instanceof ICombativesPlayerPose && this.combatives$getSwimAnimationFor(entity) > 0.0F) {
            ICombativesPlayerPose pose = (ICombativesPlayerPose) entity;
            float swimAnimation = this.combatives$getSwimAnimationFor(entity);
            boolean landCrawl = entity instanceof EntityPlayer
                && CombativesVisualPoseHelper.isLandCrawling((EntityPlayer) entity);
            if (pose.isActuallySwimming() && !landCrawl) {
                headPitch = this.combatives$rotLerpRad(swimAnimation, this.bipedHead.rotateAngleX, -((float) Math.PI / 4.0F)) / 0.017453292F;
            } else {
                headPitch = this.combatives$rotLerpRad(swimAnimation, this.bipedHead.rotateAngleX, headPitch * ((float) Math.PI / 180.0F)) / 0.017453292F;
            }
        }
        return headPitch;
    }

    @Inject(method = "setRotationAngles", at = @At(value = "FIELD",
        target = "Lnet/minecraft/client/model/ModelBiped;aimedBow:Z", ordinal = 0))
    private void combatives$setRotationAnglesPost(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
        float headPitch, float scaleFactor, Entity entity, CallbackInfo ci) {
        float swimAnimation = this.combatives$getSwimAnimationFor(entity);
        if (swimAnimation <= 0.0F) {
            return;
        }
        if (entity instanceof EntityPlayer && entity instanceof ICombativesPlayerPose) {
            ICombativesPlayerPose pose = (ICombativesPlayerPose) entity;
            if (CombativesVisualPoseHelper.isLandCrawling((EntityPlayer) entity)) {
                this.combatives$captureCrawlLegBase();
                ICombativesPlayerPose playerPose = (ICombativesPlayerPose) entity;
                if (playerPose.isSliding()) SlidePoseAnimator.apply((ModelBiped) (Object) this, swimAnimation);
                else CrawlPoseAnimator.apply((ModelBiped) (Object) this, limbSwing, limbSwingAmount, swimAnimation);
                return;
            }
        }
        float cycle = limbSwing % 26.0F;
        float armBlend = this.onGround > 0.0F ? 0.0F : swimAnimation;
        if (cycle < 14.0F) {
            this.bipedLeftArm.rotateAngleX = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleX, 0.0F);
            this.bipedRightArm.rotateAngleX = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleX, 0.0F);
            this.bipedLeftArm.rotateAngleY = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleY, (float) Math.PI);
            this.bipedRightArm.rotateAngleY = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleY, (float) Math.PI);
            this.bipedLeftArm.rotateAngleZ = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleZ, (float) Math.PI + 1.8707964F * this.combatives$getArmAngleSq(cycle) / this.combatives$getArmAngleSq(14.0F));
            this.bipedRightArm.rotateAngleZ = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleZ, (float) Math.PI - 1.8707964F * this.combatives$getArmAngleSq(cycle) / this.combatives$getArmAngleSq(14.0F));
        } else if (cycle < 22.0F) {
            float progress = (cycle - 14.0F) / 8.0F;
            this.bipedLeftArm.rotateAngleX = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleX, ((float) Math.PI / 2.0F) * progress);
            this.bipedRightArm.rotateAngleX = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleX, ((float) Math.PI / 2.0F) * progress);
            this.bipedLeftArm.rotateAngleY = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleY, (float) Math.PI);
            this.bipedRightArm.rotateAngleY = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleY, (float) Math.PI);
            this.bipedLeftArm.rotateAngleZ = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleZ, 5.012389F - 1.8707964F * progress);
            this.bipedRightArm.rotateAngleZ = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleZ, 1.2707963F + 1.8707964F * progress);
        } else {
            float progress = (cycle - 22.0F) / 4.0F;
            this.bipedLeftArm.rotateAngleX = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleX, ((float) Math.PI / 2.0F) - ((float) Math.PI / 2.0F) * progress);
            this.bipedRightArm.rotateAngleX = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleX, ((float) Math.PI / 2.0F) - ((float) Math.PI / 2.0F) * progress);
            this.bipedLeftArm.rotateAngleY = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleY, (float) Math.PI);
            this.bipedRightArm.rotateAngleY = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleY, (float) Math.PI);
            this.bipedLeftArm.rotateAngleZ = this.combatives$rotLerpRad(armBlend, this.bipedLeftArm.rotateAngleZ, (float) Math.PI);
            this.bipedRightArm.rotateAngleZ = MathHelperNew.lerp(armBlend, this.bipedRightArm.rotateAngleZ, (float) Math.PI);
        }
        this.bipedLeftLeg.rotateAngleX = MathHelperNew.lerp(swimAnimation, this.bipedLeftLeg.rotateAngleX, 0.3F * MathHelper.cos(limbSwing * 0.33333334F + (float) Math.PI));
        this.bipedRightLeg.rotateAngleX = MathHelperNew.lerp(swimAnimation, this.bipedRightLeg.rotateAngleX, 0.3F * MathHelper.cos(limbSwing * 0.33333334F));
    }

    // Vanilla's aimedBow block resolves weapon arms after movement, before consumers copy them.
    @Inject(method = "setRotationAngles", at = @At("TAIL"))
    private void combatives$finishAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scaleFactor, Entity entity, CallbackInfo ci) {
        if (this.combatives$getSwimAnimationFor(entity) <= 0.0F
                || entity instanceof EntityPlayer
                && CombativesVisualPoseHelper.isLandCrawling((EntityPlayer) entity)) {
            this.combatives$applyVisualLean(entity);
        }
    }

    @Unique private void combatives$restoreCrawlLegBase() {
        if (!this.combatives$crawlLegBaseCaptured) return;
        this.bipedLeftLeg.rotateAngleX = this.combatives$leftLegBaseX;
        this.bipedLeftLeg.rotateAngleY = this.combatives$leftLegBaseY;
        this.bipedLeftLeg.rotateAngleZ = this.combatives$leftLegBaseZ;
        this.bipedRightLeg.rotateAngleX = this.combatives$rightLegBaseX;
        this.bipedRightLeg.rotateAngleY = this.combatives$rightLegBaseY;
        this.bipedRightLeg.rotateAngleZ = this.combatives$rightLegBaseZ;
        this.combatives$crawlLegBaseCaptured = false;
    }

    @Unique
    private void combatives$captureCrawlLegBase() {
        this.combatives$leftLegBaseX = this.bipedLeftLeg.rotateAngleX;
        this.combatives$leftLegBaseY = this.bipedLeftLeg.rotateAngleY;
        this.combatives$leftLegBaseZ = this.bipedLeftLeg.rotateAngleZ;
        this.combatives$rightLegBaseX = this.bipedRightLeg.rotateAngleX;
        this.combatives$rightLegBaseY = this.bipedRightLeg.rotateAngleY;
        this.combatives$rightLegBaseZ = this.bipedRightLeg.rotateAngleZ;
        this.combatives$crawlLegBaseCaptured = true;
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks) {
        if (entity instanceof ICombativesPlayerPose) {
            this.combatives$swimAnimation = entity instanceof EntityPlayer ? CombativesVisualPoseHelper.getVisualSwimAnimation((EntityPlayer) entity, partialTicks) : ((ICombativesPlayerPose) entity).getSwimAnimation(partialTicks);
        }
        super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
    }

    @Unique private float combatives$getSwimAnimationFor(Entity entity) {
        if (entity instanceof EntityPlayer) {
            return CombativesVisualPoseHelper.getVisualSwimAnimation((EntityPlayer) entity, 1.0F);
        }
        return this.combatives$swimAnimation;
    }

    @Unique private float combatives$getArmAngleSq(float limbSwing) {
        return -65.0F * limbSwing + limbSwing * limbSwing;
    }

    @Unique private void combatives$applyVisualLean(Entity entity) {
        if (com.glowingfederal.combatives.client.camera.TacticalLeanCamera.isRenderingHand()) return;
        if (entity instanceof com.glowingfederal.combatives.movement.ICombativesLocomotion) {
            this.combatives$captureLeanBase();
            LeanVisualPose pose = LeanVisualPose.fromSemanticLean(
                    entity instanceof EntityPlayer
                        ? com.glowingfederal.combatives.movement.LeanGeometry.acceptedLean((EntityPlayer) entity)
                        : ((com.glowingfederal.combatives.movement.ICombativesLocomotion) entity).getLean());
            float originX = this.bipedBody.rotationPointX;
            float originY = this.bipedBody.rotationPointY;
            float pelvisY = (this.bipedLeftLeg.rotationPointY + this.bipedRightLeg.rotationPointY) * 0.5F - originY;
            this.combatives$leanPivot(this.bipedBody, pose, originX, originY, pelvisY);
            this.combatives$leanPivot(this.bipedHead, pose, originX, originY, pelvisY);
            this.combatives$leanPivot(this.bipedHeadwear, pose, originX, originY, pelvisY);
            this.combatives$leanPivot(this.bipedLeftArm, pose, originX, originY, pelvisY);
            this.combatives$leanPivot(this.bipedRightArm, pose, originX, originY, pelvisY);
            this.combatives$leanPivot(this.bipedLeftLeg, pose, originX, originY, pelvisY);
            this.combatives$leanPivot(this.bipedRightLeg, pose, originX, originY, pelvisY);
            // Overlap the differently rolled rigid cubes slightly to seal the hip seam.
            this.bipedLeftLeg.rotationPointY -= pose.hipInset;
            this.bipedRightLeg.rotationPointY -= pose.hipInset;
            this.bipedBody.rotateAngleZ += pose.bodyRoll;
            this.bipedHead.rotateAngleZ += pose.headRoll;
            this.bipedHeadwear.rotateAngleZ += pose.headRoll;
            this.bipedLeftArm.rotateAngleZ += pose.armRoll;
            this.bipedRightArm.rotateAngleZ += pose.armRoll;
            this.bipedLeftLeg.rotateAngleZ += pose.leftLegVisualRoll;
            this.bipedRightLeg.rotateAngleZ += pose.rightLegVisualRoll;
        }
    }

    @Unique private void combatives$leanPivot(ModelRenderer part, LeanVisualPose pose,
            float originX, float originY, float pelvisY) {
        float x = part.rotationPointX - originX;
        float y = part.rotationPointY - originY;
        part.rotationPointX += pose.pivotOffsetX(x, y, pelvisY);
        part.rotationPointY += pose.pivotOffsetY(x, y, pelvisY);
    }

    @Unique private void combatives$captureLeanBase() {
        if (this.combatives$leanBaseCaptured) return;
        this.combatives$bodyBaseZ = this.bipedBody.rotateAngleZ;
        this.combatives$headBaseZ = this.bipedHead.rotateAngleZ;
        this.combatives$headwearBaseZ = this.bipedHeadwear.rotateAngleZ;
        this.combatives$leftArmBaseZ = this.bipedLeftArm.rotateAngleZ;
        this.combatives$rightArmBaseZ = this.bipedRightArm.rotateAngleZ;
        this.combatives$leftLegLeanBaseZ = this.bipedLeftLeg.rotateAngleZ;
        this.combatives$rightLegLeanBaseZ = this.bipedRightLeg.rotateAngleZ;
        this.combatives$bodyLeanBasePointX = this.bipedBody.rotationPointX;
        this.combatives$bodyLeanBasePointY = this.bipedBody.rotationPointY;
        this.combatives$headLeanBasePointX = this.bipedHead.rotationPointX;
        this.combatives$headLeanBasePointY = this.bipedHead.rotationPointY;
        this.combatives$headwearLeanBasePointX = this.bipedHeadwear.rotationPointX;
        this.combatives$headwearLeanBasePointY = this.bipedHeadwear.rotationPointY;
        this.combatives$leftArmLeanBasePointX = this.bipedLeftArm.rotationPointX;
        this.combatives$leftArmLeanBasePointY = this.bipedLeftArm.rotationPointY;
        this.combatives$rightArmLeanBasePointX = this.bipedRightArm.rotationPointX;
        this.combatives$rightArmLeanBasePointY = this.bipedRightArm.rotationPointY;
        this.combatives$leftLegLeanBasePointX = this.bipedLeftLeg.rotationPointX;
        this.combatives$leftLegLeanBasePointY = this.bipedLeftLeg.rotationPointY;
        this.combatives$rightLegLeanBasePointX = this.bipedRightLeg.rotationPointX;
        this.combatives$rightLegLeanBasePointY = this.bipedRightLeg.rotationPointY;
        this.combatives$leanBaseCaptured = true;
    }

    @Override
    public void combatives$restoreVisualLean() {
        this.combatives$restoreLeanBase();
        this.combatives$restoreCrawlLegBase();
    }

    @Unique private void combatives$restoreLeanBase() {
        if (!this.combatives$leanBaseCaptured) return;
        this.bipedBody.rotateAngleZ = this.combatives$bodyBaseZ;
        this.bipedHead.rotateAngleZ = this.combatives$headBaseZ;
        this.bipedHeadwear.rotateAngleZ = this.combatives$headwearBaseZ;
        this.bipedLeftArm.rotateAngleZ = this.combatives$leftArmBaseZ;
        this.bipedRightArm.rotateAngleZ = this.combatives$rightArmBaseZ;
        this.bipedLeftLeg.rotateAngleZ = this.combatives$leftLegLeanBaseZ;
        this.bipedRightLeg.rotateAngleZ = this.combatives$rightLegLeanBaseZ;
        this.bipedBody.rotationPointX = this.combatives$bodyLeanBasePointX;
        this.bipedBody.rotationPointY = this.combatives$bodyLeanBasePointY;
        this.bipedHead.rotationPointX = this.combatives$headLeanBasePointX;
        this.bipedHead.rotationPointY = this.combatives$headLeanBasePointY;
        this.bipedHeadwear.rotationPointX = this.combatives$headwearLeanBasePointX;
        this.bipedHeadwear.rotationPointY = this.combatives$headwearLeanBasePointY;
        this.bipedLeftArm.rotationPointX = this.combatives$leftArmLeanBasePointX;
        this.bipedLeftArm.rotationPointY = this.combatives$leftArmLeanBasePointY;
        this.bipedRightArm.rotationPointX = this.combatives$rightArmLeanBasePointX;
        this.bipedRightArm.rotationPointY = this.combatives$rightArmLeanBasePointY;
        this.bipedLeftLeg.rotationPointX = this.combatives$leftLegLeanBasePointX;
        this.bipedLeftLeg.rotationPointY = this.combatives$leftLegLeanBasePointY;
        this.bipedRightLeg.rotationPointX = this.combatives$rightLegLeanBasePointX;
        this.bipedRightLeg.rotationPointY = this.combatives$rightLegLeanBasePointY;
        this.combatives$leanBaseCaptured = false;
    }

    @Unique private float combatives$rotLerpRad(float angle, float maxAngle, float target) {
        float f = (target - maxAngle) % ((float) Math.PI * 2.0F);
        if (f < -(float) Math.PI) f += ((float) Math.PI * 2.0F);
        if (f >= (float) Math.PI) f -= ((float) Math.PI * 2.0F);
        return maxAngle + angle * f;
    }

    @Override
    public void setSwimAnimation(float swimAnimation) {
        this.combatives$swimAnimation = swimAnimation;
    }
}
