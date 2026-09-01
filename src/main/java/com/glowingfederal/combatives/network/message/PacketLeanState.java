package com.glowingfederal.combatives.network.message;

import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.movement.ICombativesLocomotion;
import com.glowingfederal.combatives.network.PoseSync;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketLeanState implements IMessage {
    private byte direction;
    public PacketLeanState() {}
    public PacketLeanState(int direction) { this.direction = (byte) Math.max(-1, Math.min(1, direction)); }
    @Override public void fromBytes(ByteBuf buf) { this.direction = buf.readByte(); }
    @Override public void toBytes(ByteBuf buf) { buf.writeByte(this.direction); }

    public static class Handler implements IMessageHandler<PacketLeanState, IMessage> {
        @Override public IMessage onMessage(PacketLeanState message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player instanceof ICombativesLocomotion) {
                com.glowingfederal.combatives.movement.LocomotionState state = ((ICombativesLocomotion) player).getLocomotionState();
                float lean = CombativesConfig.enableLeaning && !player.isRiding() && !player.isPlayerSleeping()
                    && !player.isInWater() && state == com.glowingfederal.combatives.movement.LocomotionState.NORMAL
                    ? Math.max(-1, Math.min(1, message.direction)) : 0.0F;
                ((ICombativesLocomotion) player).setLean(lean);
                PoseSync.broadcastAuthoritativePose(player, true);
            }
            return null;
        }
    }
}
