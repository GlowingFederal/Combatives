package com.glowingfederal.combatives.client.model;

/** Bounded visual-lean lifecycle used by renderers which override ModelBiped.render. */
public interface ICombativesLeanModel {
    void combatives$restoreVisualLean();
}
