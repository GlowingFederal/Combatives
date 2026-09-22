package com.glowingfederal.combatives.network.message;

import com.glowingfederal.combatives.config.AuthoritativeGameplaySettings;
import com.glowingfederal.combatives.movement.ICombativesLocomotion;
import com.glowingfederal.combatives.movement.LeanGeometry;
import com.glowingfederal.combatives.network.PoseSync;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketLeanState implements IMessage {
    private static final ConcurrentHashMap<EntityPlayerMP, Byte> pending = new ConcurrentHashMap<EntityPlayerMP, Byte>();
    private byte direction;
    public PacketLeanState() {}
    public PacketLeanState(int direction) { this.direction = (byte) Math.max(-1, Math.min(1, direction)); }
    @Override public void fromBytes(ByteBuf buf) { this.direction = buf.readByte(); }
    @Override public void toBytes(ByteBuf buf) { buf.writeByte(this.direction); }

    public static class Handler implements IMessageHandler<PacketLeanState, IMessage> {
        @Override public IMessage onMessage(PacketLeanState message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            pending.put(player, message.direction);
            return null;
        }
    }

    /** Forge 1.7.10 SimpleImpl handlers run on Netty threads; accept state on the server tick. */
    public static void applyPending() {
        for (Map.Entry<EntityPlayerMP, Byte> request : pending.entrySet()) {
            if (!pending.remove(request.getKey(), request.getValue())) continue;
            EntityPlayerMP player = request.getKey();
            if (player.isDead || !player.worldObj.playerEntities.contains(player)) continue;
            if (player instanceof ICombativesLocomotion) {
                com.glowingfederal.combatives.movement.LocomotionState state = ((ICombativesLocomotion) player).getLocomotionState();
                float lean = AuthoritativeGameplaySettings.isLeaningEnabled(player) && !player.isRiding() && !player.isPlayerSleeping()
                    && !player.isInWater() && state == com.glowingfederal.combatives.movement.LocomotionState.NORMAL
                    ? Math.max(-1, Math.min(1, request.getValue())) : 0.0F;
                ICombativesLocomotion locomotion = (ICombativesLocomotion) player;
                locomotion.setLean(lean);
                locomotion.setAcceptedLean(LeanGeometry.calculateAcceptedLean(player));
                PoseSync.broadcastAuthoritativePose(player, true);
            }
        }
    }
}
