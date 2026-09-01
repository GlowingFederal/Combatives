package com.glowingfederal.combatives.client;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.ICombativesLocomotion;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.network.NetworkHandler;
import com.glowingfederal.combatives.network.message.PacketCrawlKeyState;
import com.glowingfederal.combatives.network.message.PacketLeanState;
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

        /*
         * Capture movement state BEFORE applying the predicted crawl toggle.
         *
         * Entering the low pose may cause vanilla sprint cancellation, so the
         * server needs to know whether this press originated while the local
         * player was genuinely sprinting forward.
         */
        boolean sprintingAtRequest = player.isSprinting();
        boolean movingForwardAtRequest =
                player.movementInput != null
                        && player.movementInput.moveForward > 0.0F;

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
                        + " sprintingAtRequest=" + sprintingAtRequest
                        + " movingForwardAtRequest=" + movingForwardAtRequest
        );

        if (NetworkHandler.channel == null) {
            MovementDiagnostics.warn(
                    player,
                    "client crawl packet send skipped because network channel is not initialized"
            );
            return;
        }

        NetworkHandler.channel.sendToServer(
                new PacketCrawlKeyState(
                        sprintingAtRequest,
                        movingForwardAtRequest
                )
        );
    }
}