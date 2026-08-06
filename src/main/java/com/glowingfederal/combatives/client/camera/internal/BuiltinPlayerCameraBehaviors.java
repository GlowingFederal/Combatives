package com.glowingfederal.combatives.client.camera.internal;

import com.combatives.api.camera.CameraDecayType;
import com.combatives.api.camera.CameraImpulse;
import com.combatives.api.camera.CameraPriority;
import com.combatives.api.camera.CameraStackingMode;
import com.combatives.api.camera.entity.CameraEffectSink;
import com.combatives.api.camera.entity.EntityBehaviorMetadata;
import com.combatives.api.camera.entity.EntityCameraBehavior;
import com.combatives.api.camera.entity.EntityCameraBehaviorFactory;
import com.combatives.api.camera.entity.EntityCameraBehaviorRegistry;
import com.combatives.api.camera.entity.EntityMatchers;
import com.combatives.api.camera.entity.EntityMotionSample;
import com.combatives.api.camera.entity.MountCameraContext;
import com.glowingfederal.combatives.config.CombativesConfig;
import java.util.Collections;
import net.minecraft.client.entity.EntityPlayerSP;

/** Conservative, generic local-player consumers of the shared entity motion sample. */
public final class BuiltinPlayerCameraBehaviors {
    private static boolean registered;
    private BuiltinPlayerCameraBehaviors() {}

    public static synchronized void register() {
        if (registered) return;
        EntityBehaviorMetadata metadata = new EntityBehaviorMetadata("combatives", Collections.<String, String>emptyMap());
        EntityCameraBehaviorRegistry.register("combatives:player_landing", 40, metadata, EntityMatchers.assignableClass(EntityPlayerSP.class), factory(0));
        EntityCameraBehaviorRegistry.register("combatives:player_collision", 30, metadata, EntityMatchers.assignableClass(EntityPlayerSP.class), factory(1));
        EntityCameraBehaviorRegistry.register("combatives:player_freefall", 20, metadata, EntityMatchers.assignableClass(EntityPlayerSP.class), factory(2));
        EntityCameraBehaviorRegistry.register("combatives:player_inertia", 10, metadata, EntityMatchers.assignableClass(EntityPlayerSP.class), factory(3));
        registered = true;
    }

    private static EntityCameraBehaviorFactory factory(final int kind) {
        return new EntityCameraBehaviorFactory() { public EntityCameraBehavior create() {
            return kind == 0 ? new Landing() : kind == 1 ? new Collision() : kind == 2 ? new Freefall() : new Inertia();
        }};
    }

    private abstract static class Base implements EntityCameraBehavior {
        public void onAttach(MountCameraContext context, CameraEffectSink sink) { reset(); }
        public void onRender(MountCameraContext context, CameraEffectSink sink) {}
        public void onDetach(MountCameraContext context, CameraEffectSink sink) { reset(); }
        void reset() {}
        EntityPlayerSP player(MountCameraContext c) { return c.getRider() instanceof EntityPlayerSP ? (EntityPlayerSP)c.getRider() : null; }
        static float clamp(double value, double min, double max) { return (float)(value < min ? min : value > max ? max : value); }
    }

    private static final class Landing extends Base {
        private boolean grounded = true;
        private double fastestDescent; private float greatestFallDistance;
        void reset() { grounded = true; fastestDescent = 0; greatestFallDistance = 0; }
        public void onTick(MountCameraContext c, CameraEffectSink sink) {
            EntityPlayerSP player=player(c); EntityMotionSample m=c.getMotion();
            if(player==null || m.isDiscontinuity()){reset();grounded=player==null||player.onGround;return;}
            if(!player.onGround) { fastestDescent=Math.min(fastestDescent,m.getVerticalVelocity()); greatestFallDistance=Math.max(greatestFallDistance,player.fallDistance); }
            if(CombativesConfig.enablePlayerLandingCamera && CombativesConfig.enableLandingCameraFeedback && player.onGround && !grounded) {
                double speed=Math.max(0,-fastestDescent);
                double arrest=Math.max(0,m.getVerticalAcceleration());
                double fall=Math.max(0,greatestFallDistance-1.5D);
                float severity=clamp((speed-0.24D)*1.15D + arrest*0.55D + fall*0.035D,0,1);
                severity=severity*severity*(3F-2F*severity);
                if(severity>0.012F) {
                    float strength=clamp(severity*CombativesConfig.playerLandingCameraStrength*CombativesConfig.landingFeedbackStrength,0,1);
                    float uneven=clamp(m.getLateralAcceleration()*0.35D,-0.22D,0.22D);
                    sink.emitImpulse(CameraImpulse.builder("combatives:player_landing")
                        .sourceEntity(player).rotation(3.6F*strength,0,uneven*strength)
                        .translation(0,-0.105F*strength,-0.018F*strength).duration(0.34F).attackTime(0.035F)
                        .decayType(CameraDecayType.SPRING).priority(CameraPriority.NORMAL).stackingMode(CameraStackingMode.ADD).build());
                    EntityCameraBehaviorDiagnostics.motionEvent("landing", "severity="+severity+" speed="+speed+" arrest="+arrest+" fallDistance="+fall);
                }
                fastestDescent=0; greatestFallDistance=0;
            }
            grounded=player.onGround;
            EntityCameraBehaviorDiagnostics.motionSample("landing",m);
        }
    }

    private static final class Freefall extends Base {
        private int fallingTicks; private float intensity;
        void reset(){fallingTicks=0;intensity=0;}
        public void onTick(MountCameraContext c,CameraEffectSink sink){
            EntityPlayerSP p=player(c);EntityMotionSample m=c.getMotion();if(p==null||m.isDiscontinuity()){reset();return;}
            boolean unsupported=!p.onGround && m.getVerticalVelocity() < -0.27D && m.getVerticalAcceleration() < 0.08D;
            fallingTicks=unsupported?fallingTicks+1:0;
            float target=fallingTicks>=4?clamp((-m.getVerticalVelocity()-0.27D)*1.35D,0,1):0;
            intensity+=(target-intensity)*(target>intensity?0.22F:0.42F);
            if(CombativesConfig.debugCamera&&((fallingTicks==4)||(fallingTicks==0&&intensity>0.01F)))EntityCameraBehaviorDiagnostics.motionEvent("freefall","active="+(fallingTicks>=4)+" intensity="+intensity);
            EntityCameraBehaviorDiagnostics.motionSample("freefall",m);
        }
        public void onRender(MountCameraContext c,CameraEffectSink sink){
            if(intensity>0.01F&&CombativesConfig.enablePlayerFreefallCamera) sink.emitFrame(CameraImpulse.builder("combatives:player_freefall")
                .rotation(-0.42F,0,0).translation(0,-0.018F,0.006F).duration(0.1F).priority(CameraPriority.BACKGROUND).build(),clamp(intensity*CombativesConfig.playerFreefallCameraStrength,0,1));
        }
    }

    private static final class Inertia extends Base {
        private double forward,lateral,turnLag; private float contribution;
        void reset(){forward=lateral=turnLag=contribution=0;}
        public void onTick(MountCameraContext c,CameraEffectSink sink){
            EntityMotionSample m=c.getMotion();if(m.isDiscontinuity()||!CombativesConfig.enablePlayerInertiaCamera){reset();return;}
            forward=m.getForwardAcceleration(); lateral=m.getLateralAcceleration();
            turnLag=m.getYawRate()*m.getHorizontalSpeed()/18D;
            contribution=clamp(Math.max(Math.abs(forward)*2.8D,Math.max(Math.abs(lateral)*2.2D,Math.abs(turnLag))),0,1);
            EntityCameraBehaviorDiagnostics.inertia(forward,lateral,turnLag,contribution);
            EntityCameraBehaviorDiagnostics.motionSample("inertia",m);
        }
        public void onRender(MountCameraContext c,CameraEffectSink sink){
            if(contribution>0.008F)sink.emitFrame(CameraImpulse.builder("combatives:player_inertia")
                .rotation(clamp(-forward*4.2D,-1.05D,1.05D),clamp(-turnLag*0.24D,-0.28D,0.28D),clamp(-lateral*2.0D-turnLag*0.22D,-0.65D,0.65D))
                .translation(clamp(-lateral*0.012D,-0.012D,0.012D),0,clamp(forward*0.018D,-0.018D,0.018D)).duration(0.1F).priority(CameraPriority.BACKGROUND).build(),clamp(CombativesConfig.playerInertiaCameraStrength,0,1));
        }
    }

    private static final class Collision extends Base {
        private int cooldown;
        public void onTick(MountCameraContext c,CameraEffectSink sink){
            EntityMotionSample m=c.getMotion();if(cooldown>0)cooldown--;if(m.isDiscontinuity()||!CombativesConfig.enablePlayerCollisionCamera)return;
            double previousSpeed=Math.sqrt(m.getPreviousVelocityX()*m.getPreviousVelocityX()+m.getPreviousVelocityZ()*m.getPreviousVelocityZ());
            double loss=previousSpeed-m.getHorizontalSpeed();
            double impulse=Math.sqrt(m.getAccelerationX()*m.getAccelerationX()+m.getAccelerationZ()*m.getAccelerationZ());
            if(cooldown==0&&previousSpeed>0.18D&&loss>0.105D&&impulse>0.13D){
                float severity=clamp((loss-0.08D)*2.7D+impulse*0.65D,0,1);float strength=clamp(severity*CombativesConfig.playerCollisionCameraStrength,0,1);
                float forward=clamp(m.getForwardAcceleration()*-3.2D,-1,1),side=clamp(m.getLateralAcceleration()*-3.2D,-1,1);
                sink.emitImpulse(CameraImpulse.builder("combatives:player_collision").sourceEntity(player(c))
                    .rotation(2.1F*forward*strength,0,1.5F*side*strength).translation(0.025F*side*strength,0,0.045F*forward*strength)
                    .duration(0.2F).attackTime(0.02F).decayType(CameraDecayType.SMOOTH).priority(CameraPriority.NORMAL).stackingMode(CameraStackingMode.REFRESH_SAME_ID).build());
                cooldown=5;EntityCameraBehaviorDiagnostics.motionEvent("collision","severity="+severity+" speedLoss="+loss+" acceleration="+impulse+" direction=("+forward+","+side+")");
            }
            EntityCameraBehaviorDiagnostics.motionSample("collision",m);
        }
    }
}
