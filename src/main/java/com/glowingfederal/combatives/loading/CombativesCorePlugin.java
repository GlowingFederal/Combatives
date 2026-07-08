package com.glowingfederal.combatives.loading;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class CombativesCorePlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {
    public static final String COMMON_MIXIN_CONFIG = "mixins.combatives.common.json";
    private static final Logger LOGGER = LogManager.getLogger("Combatives");

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        LOGGER.info("Combatives core plugin discovered; common pose mixin config will be offered to GTNHMixins");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String getMixinConfig() {
        LOGGER.info("Combatives common pose mixin config requested: {}", COMMON_MIXIN_CONFIG);
        return COMMON_MIXIN_CONFIG;
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        LOGGER.info("Combatives common pose mixin config loaded: {}", COMMON_MIXIN_CONFIG);
        return Arrays.asList(
            "EntityPlayerMixin",
            "EntityMixin",
            "EntityLivingBaseMixin",
            "EntityPlayerMPMixin"
        );
    }
}
