package com.glowingfederal.combatives.client.render;

import com.glowingfederal.combatives.util.math.MathHelperNew;
import net.minecraft.client.model.ModelBiped;

/** A stable asymmetric low slide, intentionally distinct from the crawl stroke. */
public final class SlidePoseAnimator {
    private SlidePoseAnimator() {}

    public static void apply(ModelBiped model, float blend) {
        model.bipedHead.rotateAngleX += -(float) Math.PI * 0.40F * blend;
        model.bipedHeadwear.rotateAngleX = model.bipedHead.rotateAngleX;
        model.bipedRightArm.rotateAngleX = lerp(model.bipedRightArm.rotateAngleX, -0.72F, blend);
        model.bipedLeftArm.rotateAngleX = lerp(model.bipedLeftArm.rotateAngleX, -0.25F, blend);
        model.bipedRightArm.rotateAngleZ = lerp(model.bipedRightArm.rotateAngleZ, 0.34F, blend);
        model.bipedLeftArm.rotateAngleZ = lerp(model.bipedLeftArm.rotateAngleZ, -0.48F, blend);
        model.bipedRightLeg.rotateAngleX = lerp(model.bipedRightLeg.rotateAngleX, 0.08F, blend);
        model.bipedLeftLeg.rotateAngleX = lerp(model.bipedLeftLeg.rotateAngleX, -1.05F, blend);
        model.bipedLeftLeg.rotateAngleZ = lerp(model.bipedLeftLeg.rotateAngleZ, -0.18F, blend);
    }

    private static float lerp(float current, float target, float blend) {
        return MathHelperNew.lerp(blend, current, target);
    }
}
