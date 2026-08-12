package com.glowingfederal.combatives.server;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.world.World;

/** Dedicated-server trace of the immutable packet coordinates and manager arguments. */
public final class InteractionDiagnostics {
    private InteractionDiagnostics() { }

    public static void logPacket(NetHandlerPlayServer handler, C07PacketPlayerDigging packet) {
        if (!enabled()) return;
        EntityPlayerMP player = handler.playerEntity;
        int x = packet.func_149505_c(), y = packet.func_149503_d(), z = packet.func_149502_e();
        Combatives.logger.info("SERVER DIG PACKET RECEIVED action={} packetXYZ=[{},{},{}] face={} block={} playerPos=[{},{},{}] playerBox={} eyeY={}",
                packet.func_149506_g(), x, y, z, packet.func_149501_f(), block(player.worldObj, x, y, z),
                player.posX, player.posY, player.posZ, player.boundingBox,
                player.posY + player.getEyeHeight());
    }

    public static void logDamage(ItemInWorldManager manager, String action, int x, int y, int z,
            int face, Boolean result) {
        if (!enabled()) return;
        World world = manager.theWorld;
        Combatives.logger.info("SERVER BLOCK DAMAGE action={} xyz=[{},{},{}] face={} block={} result={}",
                action, x, y, z, face, block(world, x, y, z), result == null ? "pending" : result);
    }

    private static String block(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return Block.blockRegistry.getNameForObject(block) + ":" + world.getBlockMetadata(x, y, z);
    }

    private static boolean enabled() {
        return Combatives.logger != null && CombativesConfig.verboseMovementDebug;
    }
}
