package com.glowingfederal.combatives.client;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public final class CombativesKeyBindings {
    public static KeyBinding crawl;
    public static KeyBinding leanLeft;
    public static KeyBinding leanRight;

    private CombativesKeyBindings() {
    }

    public static void register() {
        crawl = new KeyBinding("key.combatives.crawl", Keyboard.KEY_C, "key.categories.combatives");
        ClientRegistry.registerKeyBinding(crawl);
        leanLeft = new KeyBinding("key.combatives.leanLeft", Keyboard.KEY_Q, "key.categories.combatives");
        leanRight = new KeyBinding("key.combatives.leanRight", Keyboard.KEY_E, "key.categories.combatives");
        ClientRegistry.registerKeyBinding(leanLeft);
        ClientRegistry.registerKeyBinding(leanRight);
        Combatives.logger.info("Registered Combatives crawl keybind");
        MovementDiagnostics.verbose("crawl key registration complete");
    }
}
