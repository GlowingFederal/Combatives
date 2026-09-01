package com.glowingfederal.combatives.network.message;

import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.network.PoseSync;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import com.glowingfederal.combatives.movement.ICombativesLocomotion;
import com.glowingfederal.combatives.movement.LocomotionState;

public class PacketPlayerPoseS2C implements IMessage {
    private int entityId;
    private Pose pose;
    private boolean swimming;
    private boolean crawlKeyDown;
    private LocomotionState locomotion = LocomotionState.NORMAL;
    private float lean;

    public PacketPlayerPoseS2C() {
    }

    public PacketPlayerPoseS2C(int entityId, Pose pose, boolean swimming, boolean crawlKeyDown, LocomotionState locomotion, float lean) {
        this.entityId = entityId;
        this.pose = pose;
        this.swimming = swimming;
        this.crawlKeyDown = crawlKeyDown;
        this.locomotion = locomotion == null ? LocomotionState.NORMAL : locomotion;
        this.lean = lean;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        int id = buf.readByte();
        this.pose = id >= 0 && id < Pose.values().length ? Pose.values()[id] : Pose.STANDING;
        this.swimming = buf.readBoolean();
        this.crawlKeyDown = buf.readBoolean();
        int locomotionId = buf.readByte();
        this.locomotion = locomotionId >= 0 && locomotionId < LocomotionState.values().length ? LocomotionState.values()[locomotionId] : LocomotionState.NORMAL;
        this.lean = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeByte(this.pose == null ? Pose.STANDING.ordinal() : this.pose.ordinal());
        buf.writeBoolean(this.swimming);
        buf.writeBoolean(this.crawlKeyDown);
        buf.writeByte(this.locomotion.ordinal());
        buf.writeFloat(this.lean);
    }

    public static class Handler implements IMessageHandler<PacketPlayerPoseS2C, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketPlayerPoseS2C message, MessageContext ctx) {
            Entity entity = Minecraft.getMinecraft().theWorld == null ? null : Minecraft.getMinecraft().theWorld.getEntityByID(message.entityId);
            if (entity instanceof EntityPlayer) {
                PoseSync.applyAuthoritativePose((EntityPlayer) entity, message.pose, message.swimming, message.crawlKeyDown, "server");
                if (entity instanceof ICombativesLocomotion) {
                    ((ICombativesLocomotion) entity).setLocomotionState(message.locomotion);
                    ((ICombativesLocomotion) entity).setLean(message.lean);
                }
            }
            return null;
        }
    }
}
