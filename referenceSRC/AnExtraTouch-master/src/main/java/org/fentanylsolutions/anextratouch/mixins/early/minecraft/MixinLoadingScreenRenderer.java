package org.fentanylsolutions.anextratouch.mixins.early.minecraft;

import net.minecraft.client.LoadingScreenRenderer;

import org.fentanylsolutions.anextratouch.AnExtraTouch;
import org.fentanylsolutions.anextratouch.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LoadingScreenRenderer.class)
public class MixinLoadingScreenRenderer {

    @ModifyConstant(method = "resetProgresAndWorkingMessage", constant = @Constant(intValue = -1))
    private int resetProgresAndWorkingMessage(int constant) {
        return Config.loadingProgressBarEnabled ? AnExtraTouch.vic.chunkLoadingProgress : -1;
    }

}
