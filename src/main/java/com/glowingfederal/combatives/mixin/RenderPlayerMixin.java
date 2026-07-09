package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.client.model.ICombativesModelBipedSwimming;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.util.math.MathHelperNew;
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

@Mixin(RenderPlayer.class)
public abstract class RenderPlayerMixin extends RendererLivingEntity {
    public RenderPlayerMixin(ModelBase model, float shadowSize) {
        super(model, shadowSize);
    }

    @Inject(method = "renderFirstPersonArm", at = @At("HEAD"))
    private void combatives$resetFirstPersonSwimAnimation(EntityPlayer player, CallbackInfo ci) {
        ModelBiped modelPlayer = (ModelBiped) this.mainModel;
        ((ICombativesModelBipedSwimming) modelPlayer).setSwimAnimation(0.0F);
    }

    @Inject(method = "rotateCorpse(Lnet/minecraft/client/entity/AbstractClientPlayer;FFF)V", at = @At("TAIL"))
    private void combatives$applyAquaSwimRotations(AbstractClientPlayer player, float p_77043_2_, float rotationYaw, float partialTicks, CallbackInfo ci) {
        if (player instanceof ICombativesPlayerPose) {
            ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
            float animation = pose.getSwimAnimation(partialTicks);
            if (animation > 0.0F || pose.isActuallySwimming()) {
                MovementDiagnostics.debug(player, "RenderPlayer Aqua hook fired: crawl=" + pose.isCrawlKeyDown() + " swim=" + pose.isSwimming() + " pose=" + pose.getPose() + " animation=" + animation);
            }
            float targetPitch = player.isInWater() ? -90.0F - player.rotationPitch : -90.0F;
            float rotation = MathHelperNew.lerp(animation, 0.0F, targetPitch);
            GL11.glRotatef(rotation, 1.0F, 0.0F, 0.0F);

            if (pose.isActuallySwimming()) {
                GL11.glTranslatef(0.0F, -1.0F, 0.3F);
            }
        }
    }
}
