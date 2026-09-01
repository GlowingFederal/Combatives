package com.glowingfederal.combatives.client;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.network.NetworkHandler;
import com.glowingfederal.combatives.network.message.PacketCrawlKeyState;
import com.glowingfederal.combatives.network.message.PacketLeanState;
import com.glowingfederal.combatives.movement.ICombativesLocomotion;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class ClientMovementInputHandler {
    private boolean lastCrawlDown;
    private int lastLeanDirection;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getMinecraft().thePlayer == null || CombativesKeyBindings.crawl == null) {
            return;
        }
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        int leanDirection = CombativesKeyBindings.leanLeft.getIsKeyPressed() == CombativesKeyBindings.leanRight.getIsKeyPressed() ? 0
            : CombativesKeyBindings.leanLeft.getIsKeyPressed() ? -1 : 1;
        if (leanDirection != this.lastLeanDirection) {
            this.lastLeanDirection = leanDirection;
            if (player instanceof ICombativesLocomotion) ((ICombativesLocomotion) player).setLean(leanDirection);
            if (NetworkHandler.channel != null) NetworkHandler.channel.sendToServer(new PacketLeanState(leanDirection));
        }
        boolean crawlDown = CombativesKeyBindings.crawl.getIsKeyPressed();
        if (crawlDown == this.lastCrawlDown) {
            return;
        }
        this.lastCrawlDown = crawlDown;
        MovementDiagnostics.verbose(player, "crawl key " + (crawlDown ? "pressed" : "released"));
        if (!crawlDown) {
            MovementDiagnostics.verbose(player, "crawl key released: debounce reset only");
            return;
        }
        // Preserve the movement state at the low-pose request edge. Vanilla may
        // send its sprint-stop action before this custom packet is handled.
        boolean sprintingAtRequest = player.isSprinting();
        if (player instanceof ICombativesPlayerPose) {
            ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
            pose.setCrawlKeyDown(!pose.isCrawlKeyDown());
            MovementDiagnostics.verbose(player, "client predicted crawl toggle: " + pose.isCrawlKeyDown());
        }
        MovementDiagnostics.debug(player, "client sends crawl toggle request");
        if (NetworkHandler.channel == null) {
            MovementDiagnostics.warn(player, "client crawl packet send skipped because network channel is not initialized");
            return;
        }
        NetworkHandler.channel.sendToServer(new PacketCrawlKeyState(sprintingAtRequest));
    }
}
