package com.glowingfederal.combatives.combat;

import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.EffectivePlayerGeometry;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;
import com.glowingfederal.combatives.entity.player.PlayerGeometryResolver;
import com.glowingfederal.combatives.movement.LeanGeometry;
import com.glowingfederal.combatives.movement.LeanPoseMath;
import com.glowingfederal.combatives.movement.PlayerLocalBasis;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/** Pose-aware combat geometry. World movement continues to use Entity.boundingBox. */
public final class CombatHitVolumes {
    private static final double MODEL_UNIT = 0.9375D / 16.0D;
    private static final double MODEL_BASE_Y = (24.0D / 16.0D + 0.0078125D) * 0.9375D;
    private static final double HEAD_HALF = 4.0D * MODEL_UNIT;
    private static final double TORSO_HALF_X = 4.0D * MODEL_UNIT;
    private static final double TORSO_HALF_Y = 6.0D * MODEL_UNIT;
    private static final double TORSO_HALF_Z = 2.0D * MODEL_UNIT;

    private CombatHitVolumes() { }

    public static boolean supports(Entity entity) {
        return entity instanceof EntityPlayer && entity instanceof ICombativesPlayerPose;
    }

    public static List<CombatHitVolume> volumes(EntityPlayer player) {
        return volumes(player, 1.0F);
    }

    /** Render-timeline variant used by client targeting; partialTicks=1 is tick-authoritative. */
    public static List<CombatHitVolume> volumes(EntityPlayer player, float partialTicks) {
        if (!(player instanceof ICombativesPlayerPose)) return Collections.emptyList();
        ICombativesPlayerPose poseState = (ICombativesPlayerPose) player;
        Pose pose = poseState.getPose();
        EffectivePlayerGeometry effective = poseState.getEffectiveGeometry();
        EffectivePlayerGeometry base = PlayerGeometryResolver.resolve(pose);
        double scaleX = base.width == 0.0F ? 1.0D : effective.width / base.width;
        double scaleY = base.height == 0.0F ? 1.0D : effective.height / base.height;

        float lean = LeanGeometry.acceptedLean(player);
        float bodyYaw = PlayerLocalBasis.interpolateYaw(player.prevRenderYawOffset, player.renderYawOffset, partialTicks);
        float viewYaw = PlayerLocalBasis.interpolateYaw(player.prevRotationYaw, player.rotationYaw, partialTicks);
        float rootYaw = lean == 0.0F ? bodyYaw : viewYaw;
        PlayerLocalBasis basis = PlayerLocalBasis.fromYaw(rootYaw);
        double worldX = interpolate(player.lastTickPosX, player.posX, partialTicks);
        double worldZ = interpolate(player.lastTickPosZ, player.posZ, partialTicks);
        double positionY = interpolate(player.lastTickPosY, player.posY, partialTicks);
        double floorY = positionY + player.boundingBox.minY - player.posY;
        Transform world = Transform.basis(
                Vec3.createVectorHelper(basis.rightX, 0.0D, basis.rightZ),
                Vec3.createVectorHelper(0.0D, 1.0D, 0.0D),
                Vec3.createVectorHelper(basis.forwardX, 0.0D, basis.forwardZ),
                worldX, floorY, worldZ);

        boolean lowPose = pose == Pose.SWIMMING || pose == Pose.FALL_FLYING || pose == Pose.SPIN_ATTACK;
        double animation = poseState.getSwimAnimation(partialTicks);
        if (lowPose && animation <= 0.0D) animation = 1.0D;
        if (animation > 0.0D) {
            float pitch = (float) interpolate(player.prevRotationPitch, player.rotationPitch, partialTicks);
            double target = Math.toRadians(player.isInWater() ? 90.0F + pitch : 90.0F);
            double grounding = lowPose && !player.isInWater() ? -3.0D / 16.0D : 0.0D;
            world = world.then(Transform.translation(0.0D, grounding, 0.0D))
                    .then(Transform.rotateX(animation * target));
            if (lowPose) world = world.then(Transform.translation(0.0D, -1.0D, -0.3D));
        }

        boolean crouching = !lowPose && (pose == Pose.CROUCHING || player.isSneaking());
        double crouchDrop = crouching ? 0.125D : 0.0D;
        Transform model = world.then(Transform.translation(0.0D, -crouchDrop, 0.0D));
        double pelvisY = (crouching ? MODEL_BASE_Y - 9.0D * MODEL_UNIT : MODEL_BASE_Y - 12.0D * MODEL_UNIT) * scaleY;
        double leanShift = -LeanPoseMath.pelvisShiftPixels(lean) * MODEL_UNIT * scaleX;
        double leanRoll = LeanPoseMath.roll(lean);
        Transform upperLean = Transform.translation(leanShift, 0.0D, 0.0D)
                .then(Transform.translation(0.0D, pelvisY, 0.0D))
                .then(Transform.rotateZ(leanRoll))
                .then(Transform.translation(0.0D, -pelvisY, 0.0D));
        List<CombatHitVolume> result = new ArrayList<CombatHitVolume>(3);
        double headPivotY = (MODEL_BASE_Y - (crouching ? MODEL_UNIT : 0.0D)) * scaleY;
        float renderedPitch = (float) interpolate(player.prevRotationPitch, player.rotationPitch, partialTicks);
        double headPitch = Math.toRadians(renderedPitch);
        if (lowPose && !player.isInWater()) headPitch -= Math.PI * 0.40D * animation;
        else if (lowPose && player.isInWater()) {
            headPitch += animation * (-Math.PI / 4.0D - headPitch);
        }
        float renderedHeadYaw = PlayerLocalBasis.interpolateYaw(player.prevRotationYawHead, player.rotationYawHead, partialTicks);
        double headYaw = Math.toRadians(renderedHeadYaw - rootYaw);
        Transform head = model.then(upperLean)
                .then(Transform.translation(0.0D, headPivotY, 0.0D))
                .then(Transform.rotateY(headYaw))
                .then(Transform.rotateX(headPitch))
                .then(Transform.translation(0.0D, HEAD_HALF * scaleY, 0.0D));
        result.add(new CombatHitVolume(CombatHitPart.HEAD, head,
                HEAD_HALF * scaleX, HEAD_HALF * scaleY, HEAD_HALF * scaleX));

        double shoulderY = MODEL_BASE_Y * scaleY;
        double torsoCenterY = (MODEL_BASE_Y - 6.0D * MODEL_UNIT) * scaleY;
        Transform torso = model.then(upperLean)
                .then(Transform.translation(0.0D, shoulderY, 0.0D))
                .then(Transform.rotateX(crouching ? 0.5D : 0.0D))
                .then(Transform.translation(0.0D, torsoCenterY - shoulderY, 0.0D));
        result.add(new CombatHitVolume(CombatHitPart.TORSO, torso,
                TORSO_HALF_X * scaleX, TORSO_HALF_Y * scaleY, TORSO_HALF_Z * scaleX));

        double lowerTop = (MODEL_BASE_Y - (crouching ? 9.0D : 12.0D) * MODEL_UNIT) * scaleY;
        double lowerBottom = (MODEL_BASE_Y - (crouching ? 21.0D : 24.0D) * MODEL_UNIT) * scaleY;
        double lowerZ = crouching ? -4.0D * MODEL_UNIT * scaleX : 0.0D;
        Transform core = model.then(upperLean).then(Transform.translation(0.0D,
                (lowerTop + lowerBottom) * 0.5D, lowerZ));
        result.add(new CombatHitVolume(CombatHitPart.CORE, core,
                TORSO_HALF_X * scaleX, (lowerTop - lowerBottom) * 0.5D, TORSO_HALF_Z * scaleX));
        return result;
    }

    public static CombatHitResult intersect(EntityPlayer player, Vec3 start, Vec3 end, double inflation) {
        return intersect(player, start, end, inflation, 1.0F);
    }

    public static CombatHitResult intersect(EntityPlayer player, Vec3 start, Vec3 end,
            double inflation, float partialTicks) {
        CombatHitResult nearest = null;
        for (CombatHitVolume volume : volumes(player, partialTicks)) {
            CombatHitResult hit = volume.intersect(start, end, inflation);
            if (hit != null && (nearest == null || hit.fraction < nearest.fraction)) nearest = hit;
        }
        return nearest;
    }

    public static boolean contains(EntityPlayer player, Vec3 point, double inflation) {
        return contains(player, point, inflation, 1.0F);
    }

    public static boolean contains(EntityPlayer player, Vec3 point, double inflation, float partialTicks) {
        for (CombatHitVolume volume : volumes(player, partialTicks)) if (volume.contains(point, inflation)) return true;
        return false;
    }

    public static MovingObjectPosition intercept(Entity entity, AxisAlignedBB fallback,
            Vec3 start, Vec3 end, double inflation) {
        if (!supports(entity)) return fallback.calculateIntercept(start, end);
        CombatHitResult hit = intersect((EntityPlayer) entity, start, end, inflation);
        return hit == null ? null : new MovingObjectPosition(entity, hit.hitVec);
    }

    private static double interpolate(double previous, double current, float partialTicks) {
        return previous + (current - previous) * partialTicks;
    }
}
