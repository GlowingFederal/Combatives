package com.glowingfederal.combatives.network;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.network.message.PacketCrawlKeyState;
import com.glowingfederal.combatives.network.message.PacketPlayerPoseC2S;
import com.glowingfederal.combatives.network.message.PacketPlayerPoseS2C;
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
        channel.registerMessage(PacketPlayerPoseC2S.Handler.class, PacketPlayerPoseC2S.class, packetId++, Side.SERVER);
        channel.registerMessage(PacketPlayerPoseS2C.Handler.class, PacketPlayerPoseS2C.class, packetId++, Side.CLIENT);
    }
}
