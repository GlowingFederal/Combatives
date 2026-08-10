package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.EffectivePlayerGeometry;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/** Throttled numeric comparison of the physical camera base and gameplay ray origin. */
public final class TargetingDiagnostics {
    private static boolean loggingPass;
    private static double originY;
    private static double physicalCameraBaseY;
    private static double originX, originZ;
    private static float targetYaw, targetPitch;
    private static long frameId;
    private static Vec3 actualTargetOrigin;
    private static Vec3 actualTargetLook;

    private TargetingDiagnostics() { }

    public static void beforeTargeting(Object renderer, float partialTicks) {
        frameId++;
        actualTargetOrigin = null;
        actualTargetLook = null;
        loggingPass = false;
        Minecraft mc = Minecraft.getMinecraft();
        Entity view = mc.renderViewEntity;
        if ((!CombativesConfig.debugCamera && !CombativesConfig.debugMovement)
                || Combatives.logger == null || !(view instanceof EntityPlayer)
                || view.ticksExisted % 20 != 0) {
            return;
        }
        EntityPlayer player = (EntityPlayer) view;
        if (!(player instanceof ICombativesPlayerPose)) {
            return;
        }
        ICombativesPlayerPose state = (ICombativesPlayerPose) player;
        EffectivePlayerGeometry geometry = state.getEffectiveGeometry();
        Pose pose = state.getPose();
        double interpolatedY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double interpolatedMinY = interpolatedY + (player.boundingBox.minY - player.posY);
        physicalCameraBaseY = interpolatedMinY + geometry.eyeAboveMinY;
        originY = interpolatedY + player.getEyeHeight();
        Vec3 look = player.getLook(partialTicks);
        originX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        originZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        targetYaw = (float) Math.toDegrees(Math.atan2(-look.xCoord, look.zCoord));
        targetPitch = (float) Math.toDegrees(Math.asin(-look.yCoord));
        float reach = mc.playerController == null ? Float.NaN : mc.playerController.getBlockReachDistance();
        Combatives.logger.info("Combatives targeting enter: renderer={} pose={} geometry={}x{} eyeAboveMinY={} boundingBox=[{},{}] getEyeHeight={} posY={} prevPosY={} lastTickPosY={} yOffset={} ySize={} entityHeight={} partialTicks={} targetOrigin=[{},{},{}] expectedTargetOriginY={} actualVanillaOriginY={} physicalCameraBaseY={} originDelta={} targetLook=[{},{},{}] targetYaw={} targetPitch={} reach={}",
                renderer.getClass().getName(), pose, geometry.width, geometry.height, geometry.eyeAboveMinY,
                player.boundingBox.minY, player.boundingBox.maxY, player.getEyeHeight(), player.posY, player.prevPosY,
                player.lastTickPosY, player.yOffset, player.ySize, player.height, partialTicks, originX, originY, originZ, physicalCameraBaseY, originY,
                physicalCameraBaseY, originY - physicalCameraBaseY, look.xCoord, look.yCoord, look.zCoord, targetYaw, targetPitch, reach);
        loggingPass = true;
    }

    /** Called by redirects at the exact vanilla expression consumed by getMouseOver. */
    public static void captureActualTargetOrigin(Entity entity, float partialTicks, Vec3 actual) {
        actualTargetOrigin = actual;
        if (!focused(entity) || entity.ticksExisted % 100 != 0) return;
        Combatives.logger.info("BASE POV TRACE frame={} phase=TARGET ACTUAL_TARGET_ORIGIN=[{},{},{}] partialTicks={} pos=[{},{},{}] prevPos=[{},{},{}] lastTickPos=[{},{},{}] getEyeHeight={}",
                frameId, actual.xCoord, actual.yCoord, actual.zCoord, partialTicks,
                entity.posX, entity.posY, entity.posZ, entity.prevPosX, entity.prevPosY, entity.prevPosZ,
                entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ, entity.getEyeHeight());
    }

    /** Called by a redirect around the exact look-vector expression consumed by getMouseOver. */
    public static void captureActualTargetLook(Vec3 actual) {
        actualTargetLook = actual;
        Entity entity = Minecraft.getMinecraft().renderViewEntity;
        if (!focused(entity) || entity.ticksExisted % 100 != 0) return;
        Combatives.logger.info("BASE POV TRACE frame={} phase=TARGET_LOOK ACTUAL_TARGET_LOOK=[{},{},{}]",
                frameId, actual.xCoord, actual.yCoord, actual.zCoord);
    }

    /** Called from orientCamera's modified local, before procedural tail transforms. */
    public static void captureActualCameraOrigin(EntityPlayer player, float partialTicks, float consumedCameraOffset) {
        if (!focused(player) || player.ticksExisted % 100 != 0) return;
        double x = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double interpolatedY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double y = interpolatedY - consumedCameraOffset;
        double z = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        Double delta = actualTargetOrigin == null ? null : y - actualTargetOrigin.yCoord;
        Combatives.logger.info("BASE POV TRACE frame={} phase=CAMERA ACTUAL_CAMERA_ORIGIN=[{},{},{}] partialTicks={} activeYOffset={} activeInterpolatedPosY={} consumedCameraOffset={} sameFrameTargetOrigin={} sameFrameCameraMinusTargetY={} targetLook={}",
                frameId, x, y, z, partialTicks, player.yOffset, interpolatedY, consumedCameraOffset,
                vector(actualTargetOrigin), delta, vector(actualTargetLook));
    }

    private static boolean focused(Entity entity) {
        return Combatives.logger != null && entity instanceof EntityPlayer
                && (CombativesConfig.debugCamera || CombativesConfig.debugMpmPov);
    }

    private static String vector(Vec3 value) {
        return value == null ? "unavailable" : "[" + value.xCoord + "," + value.yCoord + "," + value.zCoord + "]";
    }

    public static void logRenderedCamera(EntityPlayer player, float partialTicks, double renderedBaseY) {
        if ((!CombativesConfig.debugCamera && !CombativesConfig.debugMovement) || Combatives.logger == null
                || player == null || player.ticksExisted % 20 != 0) return;
        double x = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double z = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        float renderYaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
        float renderPitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
        CameraController c = CameraController.INSTANCE;
        Combatives.logger.info("Combatives rendered camera: baseOrigin=[{},{},{}] authoritativePhysicalEyeY={} targetOrigin=[{},{},{}] basePositionDelta=[{},{},{}] renderYaw={} renderPitch={} visualYaw={} visualPitch={} visualRoll={} targetYaw={} targetPitch={} baseYawDelta={} basePitchDelta={} visualTranslation=[{},{},{}] fovModifier={}",
                x, renderedBaseY, z, physicalCameraBaseY, originX, originY, originZ, x-originX, renderedBaseY-originY, z-originZ,
                renderYaw, renderPitch, c.getLastYaw(), c.getLastPitch(), c.getLastRoll(), targetYaw, targetPitch,
                wrapDegrees(renderYaw-targetYaw), renderPitch-targetPitch, c.getLastTranslationX(), c.getLastTranslationY(), c.getLastTranslationZ(), c.getFovModifier());
    }

    private static float wrapDegrees(float value) {
        value %= 360.0F;
        return value >= 180.0F ? value - 360.0F : value < -180.0F ? value + 360.0F : value;
    }

    public static void afterTargeting() {
        if (!loggingPass || Combatives.logger == null) {
            return;
        }
        MovingObjectPosition hit = Minecraft.getMinecraft().objectMouseOver;
        Vec3 point = hit == null ? null : hit.hitVec;
        Combatives.logger.info("Combatives targeting exit: objectMouseOver={} hitPosition={} targetOriginY={} physicalCameraBaseY={} originDelta={}",
                hit == null ? "null" : hit.typeOfHit,
                point == null ? "null" : "[" + point.xCoord + "," + point.yCoord + "," + point.zCoord + "]",
                originY, physicalCameraBaseY, originY - physicalCameraBaseY);
        loggingPass = false;
    }
}
