package com.flansmod.common.network;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.PlayerData;
import com.flansmod.common.PlayerHandler;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketCancelReloadSound extends PacketBase {
	
	public String playerWhoReloaded;
	
	public PacketCancelReloadSound() { }
			
	public PacketCancelReloadSound(String playerWhoReloaded) {
		this.playerWhoReloaded = playerWhoReloaded;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
		writeUTF(data, playerWhoReloaded);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
	    playerWhoReloaded = readUTF(data);
	}

	@Override
	public void handleServerSide(EntityPlayerMP playerEntity)  {
	    FlansMod.log("Received cancel reload sound packet on server. Skipping.");
	}

	@Override
	public void handleClientSide(EntityPlayer clientPlayer) {
		ISound reloadSound = FlansModClient.reloadSound.remove(playerWhoReloaded);
	    if(reloadSound == null) return;
		
	    Minecraft.getMinecraft().getSoundHandler().stopSound(reloadSound);	    
	}

}
