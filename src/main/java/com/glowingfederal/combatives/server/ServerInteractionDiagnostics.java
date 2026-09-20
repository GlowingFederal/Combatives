package com.glowingfederal.combatives.server;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.interaction.InteractionRay;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.MovingObjectPosition;

/** Read-only comparison of packet-owned targets with the current server ray. */
public final class ServerInteractionDiagnostics {
    private ServerInteractionDiagnostics() { }

    public static void logDig(EntityPlayerMP player, C07PacketPlayerDigging packet) {
        if (!enabled()) return;

        MovingObjectPosition rayTarget = trace(player);
        Combatives.logger.info(
                "INTERACTION phase=C07_RECEIVED player={} tick={} action={} "
                        + "packet={x={},y={},z={},face={},hitX=n/a,hitY=n/a,hitZ=n/a} "
                        + "objectMouseOver=client-only serverRay={}",
                player.getCommandSenderName(), player.ticksExisted, packet.func_149506_g(),
                packet.func_149505_c(), packet.func_149503_d(), packet.func_149502_e(),
                packet.func_149501_f(), format(rayTarget));
    }

    public static void logUse(EntityPlayerMP player, C08PacketPlayerBlockPlacement packet) {
        if (!enabled()) return;

        MovingObjectPosition rayTarget = trace(player);
        Combatives.logger.info(
                "INTERACTION phase=C08_RECEIVED player={} tick={} action=USE "
                        + "packet={x={},y={},z={},face={},hitX={},hitY={},hitZ={}} "
                        + "objectMouseOver=client-only serverRay={}",
                player.getCommandSenderName(), player.ticksExisted,
                packet.func_149576_c(), packet.func_149571_d(), packet.func_149570_e(),
                packet.func_149568_f(), packet.func_149573_h(), packet.func_149569_i(),
                packet.func_149575_j(), format(rayTarget));
    }

    private static MovingObjectPosition trace(EntityPlayerMP player) {
        InteractionRay ray = InteractionRay.authoritative(player);
        return ray.traceBlocks(player, player.theItemInWorldManager.getBlockReachDistance());
    }

    private static boolean enabled() {
        return Combatives.logger != null && CombativesConfig.verboseMovementDebug;
    }

    private static String format(MovingObjectPosition hit) {
        if (hit == null) return "null";
        return "{type=" + hit.typeOfHit + ",x=" + hit.blockX + ",y=" + hit.blockY
                + ",z=" + hit.blockZ + ",face=" + hit.sideHit + ",hit=" + hit.hitVec + "}";
    }
}
