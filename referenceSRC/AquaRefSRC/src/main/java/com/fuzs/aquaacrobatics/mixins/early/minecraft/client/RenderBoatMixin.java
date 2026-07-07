package com.fuzs.aquaacrobatics.mixins.early.minecraft.client;

import net.minecraft.client.renderer.entity.RenderBoat;
import net.minecraft.entity.item.EntityBoat;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fuzs.aquaacrobatics.entity.IRockableBoat;
import com.fuzs.aquaacrobatics.util.math.MathHelperNew;

@Mixin(RenderBoat.class)
public class RenderBoatMixin {

    // I'm not sure if this works
    @Inject(
        method = "doRender",
        at = @At(
            value = "INVOKE",
            target = "org/lwjgl/opengl/GL11.glScalef(FFF)V",
            ordinal = 2,
            shift = At.Shift.BEFORE,
            remap = false))
    private void addRockingRotation(EntityBoat boat, double x, double y, double z, float entityYaw, float partialTicks,
        CallbackInfo ci) {
        float f2 = ((IRockableBoat) boat).getRockingAngle(partialTicks);
        if (!MathHelperNew.epsilonEquals(f2, 0.0F)) {
            GL11.glRotatef(f2, 1.0F, 0.0F, 1.0F);
        }
    }
}
