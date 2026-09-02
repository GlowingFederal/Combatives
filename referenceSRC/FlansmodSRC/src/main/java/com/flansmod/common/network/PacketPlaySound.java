package com.flansmod.common.network;

import java.util.Random;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.FlansModResourceHandler;
import com.flansmod.common.FlansMod;

public class PacketPlaySound extends PacketBase 
{
	public static Random rand = new Random();
	public float posX, posY, posZ;
	public String sound;
	public boolean distort, silenced;
	
	public String cause = "";
	

	public PacketPlaySound() {}

	public static void sendSoundPacket(double x, double y, double z, double range, int dimension, String s, boolean distort)
	{	
		sendSoundPacket(x, y, z, range, dimension, s, distort, false, "");
	}
	
	public static void sendSoundPacket(double x, double y, double z, double range, int dimension, String s, boolean distort, boolean silenced)
    {   
        sendSoundPacket(x, y, z, range, dimension, s, distort, silenced, "");
    }
	
	public static void sendSoundPacket(double x, double y, double z, double range, int dimension, String s, boolean distort, boolean silenced, String cause)
	{
		if(s!=null && !s.isEmpty())
		{
			FlansMod.getPacketHandler().sendToAllAround(new PacketPlaySound(x, y, z, s, distort, silenced, cause), x, y, z, (float)range, dimension);
		}
	}

	public PacketPlaySound(double x, double y, double z, String s)
	{
		this(x, y, z, s, false);
	}

	public PacketPlaySound(double x, double y, double z, String s, boolean distort)
	{	
		this(x, y, z, s, distort, false);
	}
	
	public PacketPlaySound(double x, double y, double z, String s, boolean distort, boolean silenced)
	{
	    this(x, y, z, s, distort, silenced, "");
	}
	
	public PacketPlaySound(double x, double y, double z, String s, boolean distort, boolean silenced, String cause)
    {
        posX = (float)x; posY = (float)y; posZ = (float)z;
        sound = s;
        this.distort = distort;
        this.silenced = silenced;
        this.cause = cause;
    }

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) 
	{
		data.writeFloat(posX);
    	data.writeFloat(posY);
    	data.writeFloat(posZ);
    	writeUTF(data, sound);
    	data.writeBoolean(distort);
    	data.writeBoolean(silenced);
    	writeUTF(data, cause);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) 
	{
		posX = data.readFloat();
		posY = data.readFloat();
		posZ = data.readFloat();
		sound = readUTF(data);
		distort = data.readBoolean();
		silenced = data.readBoolean();
		cause = readUTF(data);
	}

	@Override
	public void handleServerSide(EntityPlayerMP playerEntity) 
	{
		FlansMod.log("Received play sound packet on server. Skipping.");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleClientSide(EntityPlayer clientPlayer) 
	{
	    ISound soundToPlay = new PositionedSoundRecord(FlansModResourceHandler.getSound(sound), silenced ? 5F : 10F, (distort ? 1.0F / (rand.nextFloat() * 0.4F + 0.8F) : 1.0F) * (silenced ? 2F : 1F), posX, posY, posZ);
	    
	    if(!cause.isEmpty()) {
	        /* it's a reload sound */
	        FlansModClient.reloadSound.put(cause, soundToPlay);
	    }
	    
		FMLClientHandler.instance().getClient().getSoundHandler().playSound(soundToPlay);
	}

}
