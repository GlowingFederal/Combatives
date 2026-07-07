package com.glowingfederal.combatives.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public final class CombativesKeyBindings {
    public static KeyBinding crawl;

    private CombativesKeyBindings() {
    }

    public static void register() {
        crawl = new KeyBinding("key.combatives.crawl", Keyboard.KEY_C, "key.categories.combatives");
        ClientRegistry.registerKeyBinding(crawl);
    }
}
