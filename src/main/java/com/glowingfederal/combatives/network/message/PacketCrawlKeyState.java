package com.glowingfederal.combatives.network.message;

import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.movement.MovementDiagnostics;
import com.glowingfederal.combatives.network.PoseSync;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketCrawlKeyState implements IMessage {
    private boolean pressed;

    public PacketCrawlKeyState() {
    }

    public PacketCrawlKeyState(boolean pressed) {
        this.pressed = pressed;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pressed = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.pressed);
    }

    public static class Handler implements IMessageHandler<PacketCrawlKeyState, IMessage> {
        @Override
        public IMessage onMessage(PacketCrawlKeyState message, MessageContext ctx) {
            MovementDiagnostics.debug("server received crawl packet pressed=" + message.pressed);
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null) {
                MovementDiagnostics.debug("server could not resolve player for crawl packet");
                return null;
            }

            MovementDiagnostics.debug(player, "server player resolved for crawl packet");
            if (player instanceof ICombativesPlayerPose) {
                ICombativesPlayerPose pose = (ICombativesPlayerPose) player;
                MovementDiagnostics.debug(player, "server pose before crawl packet: pose=" + pose.getPose() + " swimming=" + pose.isSwimming() + " crawl=" + pose.isCrawlKeyDown());
                MovementDiagnostics.debug(player, "crawl request received: " + (message.pressed ? "toggle" : "release ignored"));
                if (message.pressed) {
                    boolean next = !pose.isCrawlKeyDown();
                    pose.setCrawlKeyDown(next);
                    MovementDiagnostics.debug(player, next ? "server accepted pose crawl toggle on" : "server accepted pose crawl toggle off");
                    pose.recalculateSize();
                    PoseSync.broadcastAuthoritativePose(player, true);
                }
                MovementDiagnostics.debug(player, "server pose after crawl packet: pose=" + pose.getPose() + " swimming=" + pose.isSwimming() + " crawl=" + pose.isCrawlKeyDown());
            } else {
                MovementDiagnostics.debug(player, "server player does not expose combatives pose state");
            }
            return null;
        }
    }
}
