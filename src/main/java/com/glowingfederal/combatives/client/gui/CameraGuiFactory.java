package com.glowingfederal.combatives.client.gui;

import cpw.mods.fml.client.IModGuiFactory;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public final class CameraGuiFactory implements IModGuiFactory {
    @Override public void initialize(Minecraft minecraft) { }
    @Override public Class<? extends GuiScreen> mainConfigGuiClass() { return CameraEffectsGui.class; }
    @Override public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() { return null; }
    @Override public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) { return null; }
}
