package com.glowingfederal.combatives.client;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

/** Verbose, read-only tracing at controller and packet-construction boundaries. */
public final class InteractionDiagnostics {
    private static boolean previousLeftDown;
    private static boolean previousRightDown;
    private static boolean initialLeftClick;
    private static boolean initialRightClick;

    private InteractionDiagnostics() { }

    public static void beginClientTick() {
        if (!enabled()) return;

        boolean leftDown = Mouse.isButtonDown(0);
        boolean rightDown = Mouse.isButtonDown(1);
        initialLeftClick = leftDown && !previousLeftDown;
        initialRightClick = rightDown && !previousRightDown;
        previousLeftDown = leftDown;
        previousRightDown = rightDown;
    }

    public static void logLeftController(int x, int y, int z, int face, boolean continuation) {
        if (!enabled()) return;
        String phase = continuation || !initialLeftClick ? "HELD_LEFT_CLICK" : "INITIAL_LEFT_CLICK";
        log(phase, continuation ? "CONTINUE_DESTROY_BLOCK" : "START_DESTROY_BLOCK",
                x, y, z, face, Float.NaN, Float.NaN, Float.NaN);
    }

    public static void logRightController(int x, int y, int z, int face,
            float hitX, float hitY, float hitZ) {
        if (!enabled()) return;
        String phase = initialRightClick ? "INITIAL_RIGHT_CLICK" : "HELD_RIGHT_CLICK";
        log(phase, "USE", x, y, z, face, hitX, hitY, hitZ);
    }

    public static void logDigPacket(C07PacketPlayerDigging packet) {
        if (!enabled()) return;
        log("C07_SENT", String.valueOf(packet.func_149506_g()), packet.func_149505_c(),
                packet.func_149503_d(), packet.func_149502_e(), packet.func_149501_f(),
                Float.NaN, Float.NaN, Float.NaN);
    }

    public static void logUsePacket(C08PacketPlayerBlockPlacement packet) {
        if (!enabled()) return;
        log("C08_SENT", "USE", packet.func_149576_c(), packet.func_149571_d(),
                packet.func_149570_e(), packet.func_149568_f(), packet.func_149573_h(),
                packet.func_149569_i(), packet.func_149575_j());
    }

    private static void log(String phase, String action, int x, int y, int z, int face,
            float hitX, float hitY, float hitZ) {
        Minecraft minecraft = Minecraft.getMinecraft();
        int tick = minecraft.thePlayer == null ? -1 : minecraft.thePlayer.ticksExisted;
        Combatives.logger.info(
                "INTERACTION phase={} tick={} action={} "
                        + "packet={x={},y={},z={},face={},hitX={},hitY={},hitZ={}} objectMouseOver={}",
                phase, tick, action, x, y, z, face, number(hitX), number(hitY), number(hitZ),
                format(minecraft.objectMouseOver));
    }

    private static String number(float value) {
        return Float.isNaN(value) ? "n/a" : String.valueOf(value);
    }

    private static String format(MovingObjectPosition hit) {
        if (hit == null) return "null";
        return "{type=" + hit.typeOfHit + ",x=" + hit.blockX + ",y=" + hit.blockY
                + ",z=" + hit.blockZ + ",face=" + hit.sideHit + ",hit=" + hit.hitVec + "}";
    }

    private static boolean enabled() {
        return Combatives.logger != null && CombativesConfig.verboseMovementDebug;
    }
}
