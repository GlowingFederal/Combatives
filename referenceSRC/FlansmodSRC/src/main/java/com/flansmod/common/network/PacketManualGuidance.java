package com.flansmod.common.network;

import com.flansmod.common.FlansMod;
import com.flansmod.common.guns.EntityBullet;
import com.flansmod.common.vector.Vector3f;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketManualGuidance extends PacketBase {
    private int BulletEntityId;

    private float OriginX;
    private float OriginY;
    private float OriginZ;

    private float LookX;
    private float LookY;
    private float LookZ;

    public PacketManualGuidance() {

    }

    public PacketManualGuidance(int entityId, float originX, float originY, float originZ, float lookX, float lookY, float lookZ) {
        BulletEntityId = entityId;

        OriginX = originX;
        OriginY = originY;
        OriginZ = originZ;

        LookX = lookX;
        LookY = lookY;
        LookZ = lookZ;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeInt(BulletEntityId);

        data.writeFloat(OriginX);
        data.writeFloat(OriginY);
        data.writeFloat(OriginZ);

        data.writeFloat(LookX);
        data.writeFloat(LookY);
        data.writeFloat(LookZ);

    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        BulletEntityId = data.readInt();

        OriginX = data.readFloat();
        OriginY = data.readFloat();
        OriginZ = data.readFloat();

        LookX = data.readFloat();
        LookY = data.readFloat();
        LookZ = data.readFloat();
    }

    @Override
    public void handleServerSide(EntityPlayerMP playerEntity) {
        if (playerEntity == null || playerEntity.worldObj == null)
            return;
        EntityBullet bullet = null;
        for (Object obj : playerEntity.worldObj.loadedEntityList) {
            if (obj instanceof EntityBullet && ((Entity) obj).getEntityId() == BulletEntityId) {
                bullet = (EntityBullet) obj;
                bullet.ownerPos = new Vector3f(OriginX, OriginY, OriginZ);
                bullet.ownerLook = new Vector3f(LookX, LookY, LookZ);
                break;
            }
        }
    }

    @Override
    public void handleClientSide(EntityPlayer clientPlayer) {
        FlansMod.log("Incorrect packet received on client side (PacketManualGuidance)");
    }
}
