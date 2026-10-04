package com.glowingfederal.combatives.client.gui;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraftforge.client.event.GuiScreenEvent;

/** Use vanilla Options' vacant left-hand slot, without overlapping another mod's button. */
public final class CameraOptionsMenu {
    private static final int BUTTON_ID = 0xC0BA;
    @SubscribeEvent public void init(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.gui instanceof GuiOptions)) return;
        int x = event.gui.width / 2 - 155, y = event.gui.height / 6 + 42;
        for (Object entry : event.buttonList) {
            GuiButton b = (GuiButton) entry;
            if (b.visible && x < b.xPosition + b.width && x + 150 > b.xPosition
                    && y < b.yPosition + b.height && y + 20 > b.yPosition) return;
        }
        event.buttonList.add(new GuiButton(BUTTON_ID, x, y, 150, 20, "Camera Effects..."));
    }
    @SubscribeEvent public void action(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.gui instanceof GuiOptions && event.button.id == BUTTON_ID) {
            event.setCanceled(true);
            Minecraft.getMinecraft().displayGuiScreen(new CameraEffectsGui(event.gui));
        }
    }
}
