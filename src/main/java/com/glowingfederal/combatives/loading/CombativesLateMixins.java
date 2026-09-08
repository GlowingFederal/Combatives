package com.glowingfederal.combatives.loading;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Loads integrations which target optional mod classes after FML has discovered mods. */
@LateMixin
@SuppressWarnings("unused")
public final class CombativesLateMixins implements ILateMixinLoader {
    private static final String CONFIG = "mixins.combatives.compat.late.json";
    private static final Logger LOGGER = LogManager.getLogger("Combatives");

    @Override
    public String getMixinConfig() {
        return CONFIG;
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        List<String> mixins = new ArrayList<String>();
        if (loadedMods.contains("flansmod")) {
            mixins.add("FlansBulletMixin");
            mixins.add("FlansGunFireMixin");
            mixins.add("FlansSnapshotMixin");
            mixins.add("FlansItemGunMixin");
            if (FMLLaunchHandler.side().isClient()) mixins.add("FlansCustomArmourMixin");
            LOGGER.info("Flan's detected; enabling authoritative lean compatibility: {}", mixins);
        }
        return mixins;
    }
}
