package com.glowingfederal.combatives.loading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
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
        LOGGER.info("Combatives core plugin discovered; Aqua crawl/swim mixins will be offered to GTNHMixins");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String getMixinConfig() {
        LOGGER.info("Combatives Aqua crawl/swim mixin config requested: {}", COMMON_MIXIN_CONFIG);
        return COMMON_MIXIN_CONFIG;
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        List<String> mixins = new ArrayList<String>(Arrays.asList(
            "EntityPlayerMixin",
            "EntityMixin",
            "EntityLivingBaseMixin",
            "EntityPlayerMPMixin"
        ));

        if (FMLLaunchHandler.side().isClient()) {
            mixins.addAll(Arrays.asList(
                "EntityPlayerSPMixin",
                "EntityClientPlayerMPMixin",
                "EntityRendererMixin",
                "ModelBipedMixin",
                "RenderPlayerMixin",
                "EntityOtherPlayerMPMixin",
                "PlayerControllerMPMixin"
            ));
        }

        LOGGER.info("Combatives Aqua crawl/swim mixins loaded from {}: {}", COMMON_MIXIN_CONFIG, mixins);
        return mixins;
    }
}
