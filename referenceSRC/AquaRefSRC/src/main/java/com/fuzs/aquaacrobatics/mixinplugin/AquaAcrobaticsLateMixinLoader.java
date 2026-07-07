package com.fuzs.aquaacrobatics.mixinplugin;

import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

@LateMixin
public class AquaAcrobaticsLateMixinLoader implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.aquaacrobatics.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        return Mixins.getLateMixins(loadedMods);
    }
}
