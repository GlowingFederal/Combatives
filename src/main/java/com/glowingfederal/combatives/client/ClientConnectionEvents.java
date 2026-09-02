package com.glowingfederal.combatives.client;

import com.glowingfederal.combatives.config.AuthoritativeGameplaySettings;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;

/** Prevents a previous server's gameplay snapshot from surviving a reconnect. */
public final class ClientConnectionEvents {
    @SubscribeEvent
    public void onConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        AuthoritativeGameplaySettings.clearClientSnapshot();
    }

    @SubscribeEvent
    public void onDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        AuthoritativeGameplaySettings.clearClientSnapshot();
    }
}
