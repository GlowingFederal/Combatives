package com.glowingfederal.combatives.network;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;

public class PoseSyncEvents {
    @SubscribeEvent
    public void onStartTracking(StartTracking event) {
        if (event.entityPlayer instanceof EntityPlayerMP) {
            PoseSync.sendAuthoritativePose((EntityPlayerMP) event.entityPlayer, event.target);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            PoseSync.broadcastAuthoritativePose((EntityPlayerMP) event.player, true);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            PoseSync.broadcastAuthoritativePose((EntityPlayerMP) event.player, true);
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            PoseSync.broadcastAuthoritativePose((EntityPlayerMP) event.player, true);
        }
    }
}
