package com.glowingfederal.combatives.client;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.network.NetworkHandler;
import com.glowingfederal.combatives.network.message.PacketCrawlKeyState;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class ClientMovementInputHandler {
    private boolean lastCrawlDown;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getMinecraft().thePlayer == null || CombativesKeyBindings.crawl == null) {
            return;
        }
        boolean crawlDown = CombativesKeyBindings.crawl.getIsKeyPressed();
        if (crawlDown == this.lastCrawlDown) {
            return;
        }
        this.lastCrawlDown = crawlDown;
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        MovementDiagnostics.debug(player, "crawl key " + (crawlDown ? "pressed" : "released"));
        if (!crawlDown) {
            MovementDiagnostics.debug(player, "crawl toggle key released; Aqua toggle state unchanged");
            return;
        }
        if (player instanceof ICombativesPlayerPose) {
            ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
            pose.setCrawlKeyDown(!pose.isCrawlKeyDown());
            MovementDiagnostics.debug(player, "client predicted crawl toggle: " + pose.isCrawlKeyDown());
        }
        MovementDiagnostics.debug(player, "crawl toggle request sent");
        NetworkHandler.channel.sendToServer(new PacketCrawlKeyState(true));
    }
}
