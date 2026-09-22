package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.config.AuthoritativeGameplaySettings;
import com.glowingfederal.combatives.movement.LeanGeometry;
import com.glowingfederal.combatives.movement.PlayerLocalBasis;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

/** Gameplay eye displacement and view-space roll, independent of cosmetic camera effects. */
public final class TacticalLeanCamera {
    private static boolean renderingHand;

    private TacticalLeanCamera() { }

    private static EntityPlayer player() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings.thirdPersonView != 0
                || !(mc.renderViewEntity instanceof EntityPlayer)) return null;
        EntityPlayer player = (EntityPlayer) mc.renderViewEntity;
        return player.isRiding() || player.isPlayerSleeping() ? null : player;
    }

    /**
     * Before vanilla yaw/pitch: Z is the view axis, so roll preserves the center ray.
     * This rotates the world/view matrix, not the camera model, and therefore uses
     * the semantic lean sign directly: the visible camera roll is its inverse.
     */
    public static void applyRoll(float partialTicks) {
        EntityPlayer player = player();
        if (player == null || !CombativesConfig.enableCameraRotations) return;
        float yaw = PlayerLocalBasis.interpolateYaw(player.prevRotationYaw, player.rotationYaw, partialTicks);
        Vec3 offset = leanOffset(player, partialTicks, yaw);
        double max = AuthoritativeGameplaySettings.getMaxLeanDistance(player);
        double accepted = max > 0.0D
                ? PlayerLocalBasis.fromYaw(yaw).projectRight(offset.xCoord, offset.zCoord) / max
                : 0.0D;
        GL11.glRotatef((float) (accepted * CombativesConfig.maxLeanRoll), 0.0F, 0.0F, 1.0F);
    }

    /** After vanilla orientation: translate the world by the inverse camera displacement. */
    public static void applyOffset(float partialTicks) {
        EntityPlayer player = player();
        if (player == null) return;
        float yaw = PlayerLocalBasis.interpolateYaw(player.prevRotationYaw, player.rotationYaw, partialTicks);
        Vec3 offset = leanOffset(player, partialTicks, yaw);
        GL11.glTranslated(-offset.xCoord, 0.0D, -offset.zCoord);
    }

    private static Vec3 leanOffset(EntityPlayer player, float partialTicks, float yaw) {
        return LeanGeometry.acceptedOffset(player, yaw);
    }

    public static void beginHand() { renderingHand = true; }
    public static void endHand() { renderingHand = false; }
    public static boolean isRenderingHand() { return renderingHand; }
}
