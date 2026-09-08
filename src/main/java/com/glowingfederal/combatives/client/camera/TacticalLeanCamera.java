package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.config.AuthoritativeGameplaySettings;
import com.glowingfederal.combatives.interaction.InteractionRay;
import com.glowingfederal.combatives.movement.PlayerLocalBasis;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
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

    /** Before vanilla yaw/pitch: Z is the view axis, so roll preserves the center ray. */
    public static void applyRoll(float partialTicks) {
        EntityPlayer player = player();
        if (player == null || !CombativesConfig.enableCameraRotations) return;
        InteractionRay ray = InteractionRay.interpolated(player, partialTicks);
        double x = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double z = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        float yaw = PlayerLocalBasis.interpolateYaw(player.prevRotationYaw, player.rotationYaw, partialTicks);
        double max = AuthoritativeGameplaySettings.getMaxLeanDistance(player);
        double accepted = max > 0.0D ? PlayerLocalBasis.fromYaw(yaw).projectRight(
                ray.origin.xCoord - x, ray.origin.zCoord - z) / max : 0.0D;
        GL11.glRotatef((float) (-accepted * CombativesConfig.maxLeanRoll), 0.0F, 0.0F, 1.0F);
    }

    /** After vanilla orientation: translate the world by the inverse camera displacement. */
    public static void applyOffset(float partialTicks) {
        EntityPlayer player = player();
        if (player == null) return;
        InteractionRay ray = InteractionRay.interpolated(player, partialTicks);
        double x = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double z = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        GL11.glTranslated(x - ray.origin.xCoord, 0.0D, z - ray.origin.zCoord);
    }

    public static void beginHand() { renderingHand = true; }
    public static void endHand() { renderingHand = false; }
    public static boolean isRenderingHand() { return renderingHand; }
}
