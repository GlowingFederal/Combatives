package com.glowingfederal.combatives.client;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.ICombativesLocomotion;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.network.NetworkHandler;
import com.glowingfederal.combatives.network.message.PacketCrawlKeyState;
import com.glowingfederal.combatives.network.message.PacketLeanState;
import com.glowingfederal.combatives.config.AuthoritativeGameplaySettings;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;

public class ClientMovementInputHandler {
    private boolean lastCrawlDown;
    private int lastLeanDirection;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        EntityClientPlayerMP player = minecraft.thePlayer;

        if (player == null || CombativesKeyBindings.crawl == null) {
            return;
        }

        handleLeanInput(player);
        handleCrawlInput(player);
    }

    private void handleLeanInput(EntityClientPlayerMP player) {
        if (CombativesKeyBindings.leanLeft == null || CombativesKeyBindings.leanRight == null) {
            return;
        }

        boolean leanLeft = CombativesKeyBindings.leanLeft.getIsKeyPressed();
        boolean leanRight = CombativesKeyBindings.leanRight.getIsKeyPressed();

        int leanDirection;

        if (leanLeft == leanRight) {
            leanDirection = 0;
        } else {
            leanDirection = leanLeft ? -1 : 1;
        }
        if (!AuthoritativeGameplaySettings.isLeaningEnabled(player)) leanDirection = 0;

        if (leanDirection == this.lastLeanDirection) {
            return;
        }

        this.lastLeanDirection = leanDirection;

        if (player instanceof ICombativesLocomotion) {
            ((ICombativesLocomotion) player).setLean(leanDirection);
        }

        if (NetworkHandler.channel != null) {
            NetworkHandler.channel.sendToServer(new PacketLeanState(leanDirection));
        }
    }

    private void handleCrawlInput(EntityClientPlayerMP player) {
        boolean crawlDown = CombativesKeyBindings.crawl.getIsKeyPressed();

        if (crawlDown == this.lastCrawlDown) {
            return;
        }

        this.lastCrawlDown = crawlDown;

        MovementDiagnostics.verbose(
                player,
                "crawl key " + (crawlDown ? "pressed" : "released")
        );

        // Crawl is a toggle. Releasing the physical key only resets the
        // press-edge debounce and does not alter the requested crawl state.
        if (!crawlDown) {
            MovementDiagnostics.verbose(
                    player,
                    "crawl key released: debounce reset only"
            );
            return;
        }

        if (player instanceof ICombativesPlayerPose) {
            ICombativesPlayerPose pose = (ICombativesPlayerPose) player;

            boolean requestedCrawl = !pose.isCrawlKeyDown();
            pose.setCrawlKeyDown(requestedCrawl);

            MovementDiagnostics.verbose(
                    player,
                    "client predicted crawl toggle: " + requestedCrawl
            );
        }

        MovementDiagnostics.debug(
                player,
                "client sends crawl toggle request"
        );

        if (NetworkHandler.channel == null) {
            MovementDiagnostics.warn(
                    player,
                    "client crawl packet send skipped because network channel is not initialized"
            );
            return;
        }

        NetworkHandler.channel.sendToServer(new PacketCrawlKeyState());
    }
}
