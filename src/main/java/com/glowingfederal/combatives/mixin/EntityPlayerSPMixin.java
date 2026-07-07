package com.glowingfederal.combatives.mixin;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovementInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public abstract class EntityPlayerSPMixin {
    @Shadow protected Minecraft mc;
    @Shadow public MovementInput movementInput;
    @Shadow protected int sprintToggleTimer;
    @Shadow public abstract boolean isSprinting();
    @Shadow public abstract void setSprinting(boolean sprinting);
    @Shadow public abstract boolean isInWater();
    @Shadow public abstract boolean isPotionActive(Potion potion);

    @Inject(method = "onLivingUpdate", at = @At("TAIL"))
    private void combatives$keepWaterSprint(CallbackInfo ci) {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;
        if (!(self instanceof ICombativesPlayerPose) || this.movementInput == null) {
            return;
        }

        ICombativesPlayerPose pose = (ICombativesPlayerPose) self;
        boolean movingForward = this.movementInput.moveForward > 1.0E-5F;
        boolean canSprint = ((float) self.getFoodStats().getFoodLevel() > 6.0F || self.capabilities.allowFlying)
            && !this.isPotionActive(Potion.blindness);
        boolean sprintKeyDown = this.mc != null && this.mc.gameSettings.keyBindSprint.getIsKeyPressed();

        if (this.isInWater() && pose.canSwim() && movingForward && canSprint) {
            if (sprintKeyDown && !this.isSprinting()) {
                MovementDiagnostics.debug(self, "water sprint restored from sprint key");
                this.setSprinting(true);
            }
            if (this.isSprinting()) {
                this.sprintToggleTimer = 0;
            }
        }

        if (pose.isSwimming() && this.isSprinting() && (!movingForward || !this.isInWater())) {
            MovementDiagnostics.debug(self, !movingForward ? "swimming cancelled: movement stopped" : "swimming cancelled: left water");
            this.setSprinting(false);
        }
    }
}
