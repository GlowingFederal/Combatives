package com.glowingfederal.combatives.compat.flans;

import com.glowingfederal.combatives.client.model.ICombativesLeanModel;
import com.glowingfederal.combatives.client.render.CombativesVisualPoseHelper;
import com.glowingfederal.combatives.movement.ICombativesLocomotion;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Closes the lean lifecycle for Flan's ModelCustomArmour, whose render override bypasses ModelBiped.render. */
@Pseudo
@Mixin(targets = "com.flansmod.client.model.ModelCustomArmour", remap = false)
public abstract class FlansCustomArmourMixin {
    @Unique private boolean combatives$posedArmour;

    @Inject(method = {"render(Lnet/minecraft/entity/Entity;FFFFFF)V",
            "func_78088_a(Lnet/minecraft/entity/Entity;FFFFFF)V"}, at = @At("HEAD"), remap = false)
    private void combatives$beginCustomArmour(Entity entity, float limbSwing,
            float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
            float scaleFactor, CallbackInfo ci) {
        this.combatives$posedArmour = entity instanceof net.minecraft.entity.player.EntityPlayer
                && (CombativesVisualPoseHelper.getVisualSwimAnimation((net.minecraft.entity.player.EntityPlayer) entity, 1.0F) > 0.0F
                || entity instanceof ICombativesLocomotion && ((ICombativesLocomotion) entity).getAcceptedLean() != 0.0F);
    }

    // Include the limb helper and the entity render's independently drawn skirts.
    @Redirect(method = {"render(Lnet/minecraft/entity/Entity;FFFFFF)V",
            "func_78088_a(Lnet/minecraft/entity/Entity;FFFFFF)V",
            "render([Lcom/flansmod/client/tmt/ModelRendererTurbo;Lnet/minecraft/client/model/ModelRenderer;FF)V"},
            at = @At(value = "INVOKE", target = "Lcom/flansmod/client/tmt/ModelRendererTurbo;render(F)V"),
            remap = false, require = 0)
    private void combatives$renderResolvedPart(@Coerce ModelRenderer part, float scale) {
        this.combatives$drawResolvedPart(part, scale);
    }

    @Redirect(method = {"render(Lnet/minecraft/entity/Entity;FFFFFF)V",
            "func_78088_a(Lnet/minecraft/entity/Entity;FFFFFF)V",
            "render([Lcom/flansmod/client/tmt/ModelRendererTurbo;Lnet/minecraft/client/model/ModelRenderer;FF)V"},
            at = @At(value = "INVOKE", target = "Lcom/flansmod/client/tmt/ModelRendererTurbo;func_78785_a(F)V"),
            remap = false, require = 0)
    private void combatives$renderResolvedPartSrg(@Coerce ModelRenderer part, float scale) {
        this.combatives$drawResolvedPart(part, scale);
    }

    @Unique private void combatives$drawResolvedPart(ModelRenderer part, float scale) {
        if (!this.combatives$posedArmour || part.rotateAngleY == 0.0F || part.rotateAngleZ == 0.0F) {
            part.render(scale);
            return;
        }
        // Turbo draws T(p) Ry Rz Rx; prepend T(p) Rz Ry Rz^-1 Ry^-1 T(-p)
        // to obtain vanilla's T(p) Rz Ry Rx without modifying the copied pose.
        float y = part.rotateAngleY * 57.29578F;
        float z = part.rotateAngleZ * 57.29578F;
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(part.rotationPointX * scale, part.rotationPointY * scale, part.rotationPointZ * scale);
            GL11.glRotatef(z, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(y, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-z, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-y, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-part.rotationPointX * scale, -part.rotationPointY * scale, -part.rotationPointZ * scale);
            part.render(scale);
        } finally {
            GL11.glPopMatrix();
        }
    }

    @Inject(method = {"render(Lnet/minecraft/entity/Entity;FFFFFF)V",
            "func_78088_a(Lnet/minecraft/entity/Entity;FFFFFF)V"}, at = @At("RETURN"), remap = false)
    private void combatives$restoreLeanAfterCustomArmourRender(Entity entity, float limbSwing,
            float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
            float scaleFactor, CallbackInfo ci) {
        ((ICombativesLeanModel) this).combatives$restoreVisualLean();
        this.combatives$posedArmour = false;
    }
}
