package com.fuzs.aquaacrobatics.mixins.early.minecraft.client;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fuzs.aquaacrobatics.client.model.IModelBipedSwimming;
import com.fuzs.aquaacrobatics.entity.player.IPlayerResizeable;
import com.fuzs.aquaacrobatics.integration.efr.EFRIntegration;
import com.fuzs.aquaacrobatics.util.math.MathHelperNew;

@SuppressWarnings("unused")
@Mixin(RenderPlayer.class)
public abstract class RenderPlayerMixin extends RendererLivingEntity {

    public RenderPlayerMixin(ModelBase modelBaseIn, float shadowSizeIn) {

        super(modelBaseIn, shadowSizeIn);
    }

    @Inject(method = "renderFirstPersonArm", at = @At("HEAD"))
    public void renderRightArm(EntityPlayer clientPlayer, CallbackInfo callbackInfo) {

        ModelBiped modelplayer = (ModelBiped) this.mainModel;;
        ((IModelBipedSwimming) modelplayer).setSwimAnimation(0.0F);
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "rotateCorpse(Lnet/minecraft/client/entity/AbstractClientPlayer;FFF)V", at = @At("TAIL"))
    protected void applyRotations(AbstractClientPlayer entityLiving, float p_77043_2_, float rotationYaw,
        float partialTicks, CallbackInfo callbackInfo) {

        if (!EFRIntegration.isElytraFlying(entityLiving)) {

            float f = ((IPlayerResizeable) entityLiving).getSwimAnimation(partialTicks);
            float f3 = entityLiving.isInWater() ? -90.0F - entityLiving.rotationPitch : -90.0F;
            float f4 = MathHelperNew.lerp(f, 0.0F, f3);
            GL11.glRotatef(f4, 1.0F, 0.0F, 0.0F);

            if (((IPlayerResizeable) entityLiving).isActuallySwimming()) {
                GL11.glTranslatef(0.0F, -1.0F, 0.3F);

            }
        }
    }

}
