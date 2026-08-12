package com.glowingfederal.combatives.client;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.network.play.client.C07PacketPlayerDigging;

/** Values at the actual controller boundary; no ray or geometry is reconstructed here. */
public final class InteractionDiagnostics {
    private InteractionDiagnostics() { }

    public static void logDigRequest(String action, int x, int y, int z, int face) {
        if (!enabled()) return;
        logObjectMouseOver();
        World world = Minecraft.getMinecraft().theWorld;
        Combatives.logger.info("CLIENT CONTROLLER BLOCK TARGET phase={} xyz=[{},{},{}] face={} block={}",
                action, x, y, z, face, block(world, x, y, z));
    }

    public static void logDigPacket(C07PacketPlayerDigging packet) {
        if (!enabled()) return;
        int x = packet.func_149505_c(), y = packet.func_149503_d(), z = packet.func_149502_e();
        Combatives.logger.info("CLIENT DIG REQUEST action={} packetXYZ=[{},{},{}] packetFace={} block={}",
                packet.func_149506_g(), x, y, z, packet.func_149501_f(),
                block(Minecraft.getMinecraft().theWorld, x, y, z));
    }

    public static void logInteraction(String action, int x, int y, int z, int face, Vec3 hit) {
        if (!enabled()) return;
        logObjectMouseOver();
        Combatives.logger.info("CLIENT INTERACTION REQUEST action={} xyz=[{},{},{}] face={} hitVec={} block={}",
                action, x, y, z, face, vector(hit), block(Minecraft.getMinecraft().theWorld, x, y, z));
    }

    public static void logEntityAttack(Entity target) {
        if (!enabled()) return;
        logObjectMouseOver();
        Combatives.logger.info("CLIENT ENTITY ATTACK entityId={} class={}", target == null ? -1 : target.getEntityId(),
                target == null ? "null" : target.getClass().getName());
    }

    private static void logObjectMouseOver() {
        MovingObjectPosition hit = Minecraft.getMinecraft().objectMouseOver;
        World world = Minecraft.getMinecraft().theWorld;
        Combatives.logger.info("CLIENT OBJECT_MOUSE_OVER type={} xyz=[{},{},{}] sideHit={} hitVec={} block={}",
                hit == null ? "null" : hit.typeOfHit,
                hit == null ? 0 : hit.blockX, hit == null ? 0 : hit.blockY, hit == null ? 0 : hit.blockZ,
                hit == null ? -1 : hit.sideHit, hit == null ? "null" : vector(hit.hitVec),
                hit == null ? "unavailable" : block(world, hit.blockX, hit.blockY, hit.blockZ));
    }

    private static String block(World world, int x, int y, int z) {
        if (world == null) return "unavailable";
        Block block = world.getBlock(x, y, z);
        return Block.blockRegistry.getNameForObject(block) + ":" + world.getBlockMetadata(x, y, z);
    }

    private static String vector(Vec3 value) {
        return value == null ? "null" : "[" + value.xCoord + "," + value.yCoord + "," + value.zCoord + "]";
    }

    private static boolean enabled() {
        return Combatives.logger != null && CombativesConfig.verboseMovementDebug;
    }
}
