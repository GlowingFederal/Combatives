package com.glowingfederal.combatives.network.message;

import com.glowingfederal.combatives.config.AuthoritativeGameplaySettings;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Complete connection-scoped server snapshot of gameplay-affecting configuration. */
public class PacketAuthoritativeGameplayConfig implements IMessage {
    private static final int LAYOUT_VERSION = 1;
    private boolean leaningEnabled;
    private double maxLeanDistance;
    private boolean mpmHitboxScalingEnabled;

    public PacketAuthoritativeGameplayConfig() { }

    public PacketAuthoritativeGameplayConfig(AuthoritativeGameplaySettings.Snapshot snapshot) {
        this.leaningEnabled = snapshot.leaningEnabled;
        this.maxLeanDistance = snapshot.maxLeanDistance;
        this.mpmHitboxScalingEnabled = snapshot.mpmHitboxScalingEnabled;
    }

    @Override public void fromBytes(ByteBuf buf) {
        int version = buf.readUnsignedByte();
        if (version != LAYOUT_VERSION) throw new IllegalArgumentException("Unsupported gameplay config layout " + version);
        this.leaningEnabled = buf.readBoolean();
        this.maxLeanDistance = buf.readDouble();
        this.mpmHitboxScalingEnabled = buf.readBoolean();
    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeByte(LAYOUT_VERSION);
        buf.writeBoolean(this.leaningEnabled);
        buf.writeDouble(this.maxLeanDistance);
        buf.writeBoolean(this.mpmHitboxScalingEnabled);
    }

    public static class Handler implements IMessageHandler<PacketAuthoritativeGameplayConfig, IMessage> {
        @Override public IMessage onMessage(PacketAuthoritativeGameplayConfig message, MessageContext ctx) {
            AuthoritativeGameplaySettings.installClientSnapshot(new AuthoritativeGameplaySettings.Snapshot(
                    message.leaningEnabled, message.maxLeanDistance, message.mpmHitboxScalingEnabled));
            return null;
        }
    }
}
