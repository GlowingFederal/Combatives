package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.config.CameraVisualSetting;
import com.glowingfederal.combatives.config.AuthoritativeGameplaySettings;
import com.glowingfederal.combatives.interaction.InteractionRay;
import com.glowingfederal.combatives.movement.ICombativesLocomotion;
import com.glowingfederal.combatives.movement.LeanGeometry;
import com.glowingfederal.combatives.movement.PlayerLocalBasis;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

/** Tick-owned visual lean. Gameplay rays and collision continue to read authoritative lean. */
public final class TacticalLeanCamera {
    private static boolean renderingHand;
    private static float previousLean, currentLean;

    private TacticalLeanCamera() { }

    private static EntityPlayer player(boolean firstPersonOnly) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || firstPersonOnly && mc.gameSettings.thirdPersonView != 0
                || !(mc.renderViewEntity instanceof EntityPlayer)) return null;
        EntityPlayer player = (EntityPlayer) mc.renderViewEntity;
        return player != mc.thePlayer || player.isRiding() || player.isPlayerSleeping() ? null : player;
    }

    public static void tick(EntityPlayer player) {
        if (player.isRiding() || player.isPlayerSleeping()) {
            previousLean = currentLean = 0;
            return;
        }
        previousLean = currentLean;
        float target = player instanceof ICombativesLocomotion ? LeanGeometry.acceptedLean(player) : 0;
        currentLean += (target - currentLean) * CameraVisualSetting.LEAN_RESPONSE.get();
    }

    public static void reset() { previousLean = currentLean = 0; renderingHand = false; }

    /**
     * Before vanilla yaw/pitch: Z is the view axis, so roll preserves the center ray.
     * This rotates the world/view matrix, not the camera model, and therefore uses
     * the semantic lean sign directly: the visible camera roll is its inverse.
     */
    public static void applyRoll(float partialTicks) {
        EntityPlayer player = player(false);
        if (player == null || !CombativesConfig.enableCameraRotations) return;
        // Keep the established lean cue independent of cosmetic intensity controls.
        GL11.glRotatef(getRenderLean(partialTicks) * (float) CombativesConfig.maxLeanRoll,
                0.0F, 0.0F, 1.0F);
    }

    /** After vanilla orientation: translate the world by the inverse camera displacement. */
    public static void applyOffset(float partialTicks) {
        EntityPlayer player = player(true);
        if (player == null) return;
        float p = Math.max(0, Math.min(1, partialTicks));
        float yaw = PlayerLocalBasis.interpolateYaw(player.prevRotationYaw, player.rotationYaw, p);
        Vec3 offset = renderOffset(player, p, yaw);
        GL11.glTranslated(-offset.xCoord, 0.0D, -offset.zCoord);
    }

    /** Shared camera/hand/HUD presentation sample. Never use this for a gameplay ray. */
    public static float getRenderLean(float partialTicks) {
        EntityPlayer player = player(false);
        if (player == null) return 0;
        float p = Math.max(0, Math.min(1, partialTicks));
        float yaw = PlayerLocalBasis.interpolateYaw(player.prevRotationYaw, player.rotationYaw, p);
        Vec3 offset = renderOffset(player, p, yaw);
        double max = AuthoritativeGameplaySettings.getMaxLeanDistance(player);
        return max > 0 ? (float) (PlayerLocalBasis.fromYaw(yaw).projectRight(offset.xCoord, offset.zCoord) / max) : 0;
    }

    private static Vec3 renderOffset(EntityPlayer player, float partialTicks, float yaw) {
        float p = Math.max(0, Math.min(1, partialTicks));
        float requested = previousLean + (currentLean - previousLean) * p;
        return LeanGeometry.legalOffset(player, InteractionRay.interpolatedBase(player, p),
                requested, yaw);
    }

    public static void beginHand() { renderingHand = true; }
    public static void endHand() { renderingHand = false; }
    public static boolean isRenderingHand() { return renderingHand; }
}
