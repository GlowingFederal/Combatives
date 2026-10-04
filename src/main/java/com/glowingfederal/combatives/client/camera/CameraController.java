package com.glowingfederal.combatives.client.camera;

import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.config.CameraVisualSetting;
import com.glowingfederal.combatives.client.camera.internal.CameraEffectManager;
import com.glowingfederal.combatives.client.camera.internal.EntityCameraBehaviorManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import org.lwjgl.opengl.GL11;
import com.glowingfederal.combatives.movement.ICombativesLocomotion;
import com.glowingfederal.combatives.movement.LocomotionState;
import com.glowingfederal.combatives.movement.PlayerLocalBasis;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.world.World;

public final class CameraController {
    public static final CameraController INSTANCE = new CameraController();

    private final MovementCameraState movement = new MovementCameraState();
    private final LeanController lean = new LeanController();
    private final BobController bob = new BobController();
    private final FOVController fov = new FOVController();
    private final ShakeController shake = new ShakeController();

    private static final float MAX_CAMERA_PITCH_DEGREES = 6.0F;
    private static final float MAX_CAMERA_ROLL_DEGREES = 5.0F;
    private static final float MAX_AMBIENT_X_OFFSET = 0.04F;
    private static final float MAX_AMBIENT_Y_OFFSET = 0.06F;
    private static final float MAX_IMPACT_PITCH_DEGREES = 8.0F;
    private static final float MAX_IMPACT_ROLL_DEGREES = 4.5F;
    private static final float MAX_IMPACT_X_OFFSET = 0.16F;
    private static final float MAX_IMPACT_Y_OFFSET = 0.2F;
    private static final float MAX_IMPACT_Z_OFFSET = 0.16F;

    private float leanRoll, leanPitch, bobVertical, bobSway, bobPitch, bobRoll, shakeVertical, shakeForward, shakeLateral, shakePitch, shakeRoll, fovModifier;
    private float lastTranslationX, lastTranslationY, lastTranslationZ;
    private float lastPitch, lastYaw, lastRoll;
    private float slideCameraBlend;
    private float bobSuppression;
    private float sampledAmbientScale = 1F;
    private final TickState previous = new TickState();
    private final TickState current = new TickState();
    private EntityPlayerSP statePlayer;
    private World stateWorld;
    private int positionRevision;

    private CameraController() {}

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.isGamePaused()) return;
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || !player.isEntityAlive()) { reset(); return; }
        int revision = player instanceof ICombativesPlayerPose
                ? ((ICombativesPlayerPose) player).getPositionHistoryRevision() : 0;
        if (statePlayer != player || stateWorld != player.worldObj || positionRevision != revision) {
            reset();
            statePlayer = player;
            stateWorld = player.worldObj;
            positionRevision = revision;
        }
        TacticalLeanCamera.tick(player);
        if (!CombativesConfig.enableCombativesCamera) { resetEffects(); return; }
        previous.copy(current);
        WeaponCameraPresentation.tick(player);
        movement.tick(player);
        if (CombativesConfig.enableMovementLean) lean.update(movement); else lean.reset();
        if (CombativesConfig.enableMovementFov) fov.update(movement); else fov.reset();
        if (CombativesConfig.enableCameraShake) shake.tick(); else shake.reset();
        CameraEffectManager.tick();
        EntityCameraBehaviorManager.INSTANCE.tick(player);
        float slideTarget = player instanceof ICombativesLocomotion
                && ((ICombativesLocomotion) player).getLocomotionState() == LocomotionState.SLIDING ? 1.0F : 0.0F;
        current.slide += (slideTarget - current.slide) * (1.0F - 0.65F * 0.65F * 0.65F);
        current.leanRoll = lean.getRoll(); current.leanPitch = lean.getPitch();
        current.vertical = shake.getVertical(); current.forward = shake.getForward(); current.lateral = shake.getLateral();
        current.pitch = shake.getPitch(); current.roll = shake.getRoll();
        current.suppression = shake.getBobSuppression(); current.fov = fov.getModifier();
    }

    /** Pure presentation sampling. Repeated render passes cannot integrate or recover state. */
    public void sample(Minecraft mc, float partialTicks) {
        if (!CombativesConfig.enableCombativesCamera || statePlayer == null || statePlayer != mc.thePlayer) return;
        float p = clamp(partialTicks, 0, 1);
        float ambientScale = WeaponCameraPresentation.ambientScale(p);
        sampledAmbientScale = ambientScale;
        float movementLeanScale = CameraVisualSetting.MOVEMENT_LEAN.get() * ambientScale;
        float explosionScale = CameraVisualSetting.EXPLOSION.get();
        movement.sample(statePlayer, p);
        if (CombativesConfig.enableProceduralBob) bob.update(movement); else bob.reset();
        leanRoll = lerp(previous.leanRoll, current.leanRoll, p); leanPitch = lerp(previous.leanPitch, current.leanPitch, p);
        leanRoll *= movementLeanScale; leanPitch *= movementLeanScale;
        bobVertical = bob.getVertical(); bobSway = bob.getSway(); bobPitch = bob.getPitch(); bobRoll = bob.getRoll();
        bobVertical *= ambientScale; bobSway *= ambientScale; bobPitch *= ambientScale; bobRoll *= ambientScale;
        shakeVertical = lerp(previous.vertical, current.vertical, p); shakeForward = lerp(previous.forward, current.forward, p);
        shakeLateral = lerp(previous.lateral, current.lateral, p); shakePitch = lerp(previous.pitch, current.pitch, p);
        shakeRoll = lerp(previous.roll, current.roll, p); bobSuppression = lerp(previous.suppression, current.suppression, p);
        shakeVertical *= explosionScale; shakeForward *= explosionScale; shakeLateral *= explosionScale;
        shakePitch *= explosionScale; shakeRoll *= explosionScale; bobSuppression *= explosionScale;
        slideCameraBlend = lerp(previous.slide, current.slide, p); fovModifier = lerp(previous.fov, current.fov, p);
        EntityCameraBehaviorManager.INSTANCE.render(statePlayer, p);
        CameraEffectManager.sample(statePlayer, p);
    }

    public void applyTransforms(float partialTicks) {
        if (!CombativesConfig.enableCombativesCamera) return;
        float master = CameraVisualSetting.MASTER.get();

        float bobScale = 1.0F - clamp(bobSuppression, 0.0F, 0.8F);
        float ambientX = clamp(bobSway * bobScale, -MAX_AMBIENT_X_OFFSET, MAX_AMBIENT_X_OFFSET);
        float ambientY = clamp(bobVertical * bobScale, -MAX_AMBIENT_Y_OFFSET, MAX_AMBIENT_Y_OFFSET);
        float impactX = clamp(shakeLateral + CameraEffectManager.getX(), -MAX_IMPACT_X_OFFSET, MAX_IMPACT_X_OFFSET);
        float impactY = clamp(shakeVertical + CameraEffectManager.getY(), -MAX_IMPACT_Y_OFFSET, MAX_IMPACT_Y_OFFSET);
        float impactZ = clamp(shakeForward + CameraEffectManager.getZ(), -MAX_IMPACT_Z_OFFSET, MAX_IMPACT_Z_OFFSET);
        float xOffset = (ambientX + impactX) * master;
        float yOffset = (ambientY + impactY - slideCameraBlend * 0.035F * CameraVisualSetting.SLIDE.get()) * master;
        this.lastTranslationY = yOffset;
        float zOffset = impactZ * master;
        this.lastTranslationX = xOffset;
        GL11.glTranslatef(xOffset, yOffset, zOffset);
        this.lastTranslationZ = zOffset;

        float pitch = 0.0F;
        float yaw = 0.0F;
        float roll = 0.0F;
        if (CombativesConfig.enableCameraRotations) {
            float ambientPitch = clamp(bobPitch * bobScale + leanPitch, -MAX_CAMERA_PITCH_DEGREES, MAX_CAMERA_PITCH_DEGREES);
            float ambientRoll = clamp(bobRoll * bobScale + leanRoll, -MAX_CAMERA_ROLL_DEGREES, MAX_CAMERA_ROLL_DEGREES);
            yaw = clamp(CameraEffectManager.getYaw(), -CombativesConfig.maxCameraYawDegrees, CombativesConfig.maxCameraYawDegrees);
            pitch = ambientPitch + clamp(shakePitch + CameraEffectManager.getPitch(), -MAX_IMPACT_PITCH_DEGREES, MAX_IMPACT_PITCH_DEGREES);
            roll = ambientRoll + clamp(shakeRoll + CameraEffectManager.getRoll(), -MAX_IMPACT_ROLL_DEGREES, MAX_IMPACT_ROLL_DEGREES);
            pitch *= master; yaw *= master; roll *= master;
            GL11.glRotatef(pitch, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(roll, 0.0F, 0.0F, 1.0F);
        }
        this.lastPitch = pitch;
        this.lastYaw = yaw;
        this.lastRoll = roll;
        if (Combatives.logger != null && CombativesConfig.verboseCameraDebug) Combatives.logger.info("Combatives camera final render transform pitch={} yaw={} roll={} translation=({},{},{}) fov={}", pitch, yaw, roll, xOffset, yOffset, zOffset, getFovModifier());
    }

    public void applyHandTransforms(float partialTicks) {
        if (!CombativesConfig.enableCombativesCamera || !CombativesConfig.enableProceduralBob) return;
        applyVanillaStyleBob();
    }

    private void applyVanillaStyleBob() {
        float master = CameraVisualSetting.MASTER.get();
        float xOffset = clamp(bobSway, -MAX_AMBIENT_X_OFFSET, MAX_AMBIENT_X_OFFSET) * master;
        float yOffset = clamp(bobVertical, -MAX_AMBIENT_Y_OFFSET, MAX_AMBIENT_Y_OFFSET) * master;
        float zOffset = clamp(0.0F, -MAX_IMPACT_Z_OFFSET, MAX_IMPACT_Z_OFFSET);
        GL11.glTranslatef(xOffset, yOffset, zOffset);
        if (!CombativesConfig.enableCameraRotations) return;
        GL11.glRotatef(clamp(bobPitch, -MAX_CAMERA_PITCH_DEGREES, MAX_CAMERA_PITCH_DEGREES) * master, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(clamp(bobRoll, -MAX_CAMERA_ROLL_DEGREES, MAX_CAMERA_ROLL_DEGREES) * master, 0.0F, 0.0F, 1.0F);
    }



    private static float clamp(float value, float min, float max) {
        return value < min ? min : value > max ? max : value;
    }

    public void reset() { resetEffects(); TacticalLeanCamera.reset(); statePlayer = null; stateWorld = null; }

    private void resetEffects() { EntityCameraBehaviorManager.INSTANCE.reset(statePlayer); WeaponCameraPresentation.reset(); sampledAmbientScale = 1F; movement.reset(); lean.reset(); bob.reset(); fov.reset(); shake.reset(); CameraEffectManager.reset(); previous.clear(); current.clear(); leanRoll = leanPitch = bobVertical = bobSway = bobPitch = bobRoll = shakeVertical = shakeForward = shakeLateral = shakePitch = shakeRoll = fovModifier = lastTranslationX = lastTranslationY = lastTranslationZ = lastPitch = lastYaw = lastRoll = slideCameraBlend = bobSuppression = 0.0F; }

    private static float lerp(float a, float b, float p) { return a + (b - a) * p; }

    private static final class TickState {
        float leanRoll, leanPitch, vertical, forward, lateral, pitch, roll, suppression, slide, fov;
        void copy(TickState s) { leanRoll=s.leanRoll; leanPitch=s.leanPitch; vertical=s.vertical; forward=s.forward; lateral=s.lateral; pitch=s.pitch; roll=s.roll; suppression=s.suppression; slide=s.slide; fov=s.fov; }
        void clear() { leanRoll=leanPitch=vertical=forward=lateral=pitch=roll=suppression=slide=fov=0; }
    }

    public void addExplosionFeedback(EntityPlayerSP player, double x, double y, double z, float strength) {
        if (!CombativesConfig.enableCombativesCamera || !CombativesConfig.enableCameraShake || !CombativesConfig.enableExplosionCameraFeedback || player == null) {
            if (Combatives.logger != null && CombativesConfig.debugCamera) Combatives.logger.info("Combatives explosion feedback rejected: enableCombativesCamera={}, enableCameraShake={}, enableExplosionCameraFeedback={}, hasPlayer={}", CombativesConfig.enableCombativesCamera, CombativesConfig.enableCameraShake, CombativesConfig.enableExplosionCameraFeedback, player != null);
            return;
        }
        double dx = player.posX - x;
        double dy = player.posY + player.getEyeHeight() - y;
        double dz = player.posZ - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float radius = Math.max(8.0F, strength * 4.0F);
        float distanceFalloff = clamp(1.0F - (float) (distance / radius), 0.0F, 1.0F);
        float strengthFactor = clamp(strength / 4.0F, 0.0F, 2.0F);
        float response = (float) Math.pow(clamp(strengthFactor * distanceFalloff, 0.0F, 1.0F), 0.45F);
        if (response <= 0.0F) {
            if (Combatives.logger != null && CombativesConfig.verboseCameraDebug) Combatives.logger.info("Combatives explosion impulse rejected: reason=outside_radius_or_zero_response, distance={}, radius={}, strength={}", distance, radius, strength);
            return;
        }
        PlayerLocalBasis basis = PlayerLocalBasis.fromYaw(player.rotationYaw);
        double forwardX = basis.forwardX;
        double forwardZ = basis.forwardZ;
        double rightX = basis.rightX;
        double rightZ = basis.rightZ;
        double invDistance = 1.0D / Math.max(0.001D, distance);
        double dirX = dx * invDistance;
        double dirY = dy * invDistance;
        double dirZ = dz * invDistance;
        float localForward = clamp((float) (dirX * forwardX + dirZ * forwardZ), -1.0F, 1.0F);
        float localRight = clamp((float) (dirX * rightX + dirZ * rightZ), -1.0F, 1.0F);
        float localVertical = clamp((float) dirY, -1.0F, 1.0F);
        shake.addExplosionImpulse(response, localForward, localRight, localVertical);
    }
    public float getFovModifier() { return (fovModifier * CameraVisualSetting.FOV.get() * sampledAmbientScale
            + CameraEffectManager.getFov() * 0.01F) * CameraVisualSetting.MASTER.get(); }
    public float getLastTranslationX() { return lastTranslationX; }
    public float getLastTranslationY() { return lastTranslationY; }
    public float getLastTranslationZ() { return lastTranslationZ; }
    public float getLastPitch() { return lastPitch; }
    public float getLastYaw() { return lastYaw; }
    public float getLastRoll() { return lastRoll; }
}
