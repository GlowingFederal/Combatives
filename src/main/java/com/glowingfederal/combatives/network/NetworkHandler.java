package com.glowingfederal.combatives.network;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.network.message.PacketCrawlKeyState;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public final class NetworkHandler {
    private static int packetId;
    public static SimpleNetworkWrapper channel;

    private NetworkHandler() {
    }

    public static void register() {
        channel = NetworkRegistry.INSTANCE.newSimpleChannel(Combatives.MOD_ID);
        channel.registerMessage(PacketCrawlKeyState.Handler.class, PacketCrawlKeyState.class, packetId++, Side.SERVER);
    }
}
