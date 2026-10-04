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
import com.glowingfederal.combatives.entity.Pose;
import com.glowingfederal.combatives.entity.player.ICombativesPlayerPose;

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
        EntityCameraBehaviorRegistry.register("combatives:player_crawl", 15, metadata, EntityMatchers.assignableClass(EntityPlayerSP.class), factory(4));
        EntityCameraBehaviorRegistry.register("combatives:player_jump", 25, metadata, EntityMatchers.assignableClass(EntityPlayerSP.class), factory(5));
        registered = true;
    }

    private static EntityCameraBehaviorFactory factory(final int kind) {
        return new EntityCameraBehaviorFactory() { public EntityCameraBehavior create() {
            return kind == 0 ? new Landing() : kind == 1 ? new Collision() : kind == 2 ? new Freefall() : kind == 3 ? new Inertia() : kind == 4 ? new Crawl() : new Jump();
        }};
    }

    private static final class Crawl extends Base {
        private static final CameraImpulse CYCLE_POS=CameraImpulse.builder("combatives:crawl_cycle_pos").rotation(0.34F,0,0).translation(0,0.014F,-0.018F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse CYCLE_NEG=CameraImpulse.builder("combatives:crawl_cycle_neg").rotation(-0.34F,0,0).translation(0,-0.014F,0.018F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse POSTURE=CameraImpulse.builder("combatives:crawl_posture").rotation(0.65F,0,0).translation(0,-0.035F,-0.012F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse PULL=CameraImpulse.builder("combatives:crawl_pull").translation(0,-0.012F,-0.018F).duration(0.16F).attackTime(0.035F).priority(CameraPriority.BACKGROUND).build();
        private float blend,phase,motionWeight,previousBlend,previousPhase,previousMotionWeight; private int cycle;
        void reset(){blend=phase=motionWeight=previousBlend=previousPhase=previousMotionWeight=0;cycle=0;}
        public void onTick(MountCameraContext c,CameraEffectSink sink){
            EntityPlayerSP p=player(c);EntityMotionSample m=c.getMotion();if(p==null||m.isDiscontinuity()){reset();return;}
            previousBlend=blend; previousPhase=phase; previousMotionWeight=motionWeight;
            boolean crawling=false;
            ICombativesPlayerPose pose=null;
            if(p instanceof ICombativesPlayerPose){
                pose=(ICombativesPlayerPose)p;
                // isActuallySwimming() means "uses the prone pose" in this port and is true for
                // land crawling too.  The authoritative swim flag plus water state distinguish it.
                crawling=pose.getPose()==Pose.SWIMMING&&!pose.isSwimming()&&!p.isInWater();
            }
            float ticks=Math.max(3F,CombativesConfig.crawlTransitionMillis/50F),target=crawling&&CombativesConfig.enableCrawlCamera?1F:0F;
            blend=approach(blend,target,1F/ticks);
            float speed=clamp(m.getHorizontalSpeed()/0.16D,0,1);
            motionWeight+=(speed-motionWeight)*(speed>motionWeight?0.32F:0.22F);
            phase+=0.43F*motionWeight*blend;
            int now=(int)(phase/(float)Math.PI);
            if(now!=cycle&&blend>0.8F&&motionWeight>0.12F){sink.emitImpulse(PULL);cycle=now;}
            EntityCameraBehaviorDiagnostics.crawl(crawling,pose!=null&&pose.isSwimming(),p.isInWater(),pose==null?"unavailable":pose.getPose(),blend,motionWeight,phase,0,0,false);
        }
        public void onRender(MountCameraContext c,CameraEffectSink sink){
            float renderBlend=lerp(previousBlend,blend,c.getPartialTicks());
            float renderMotion=lerp(previousMotionWeight,motionWeight,c.getPartialTicks());
            if(renderBlend<=0.001F)return;
            float amp=renderBlend;
            boolean postureAccepted=sink.emitFrame(POSTURE,amp);
            float wave=(float)Math.sin(lerp(previousPhase,phase,c.getPartialTicks()));
            float cycleStrength=Math.abs(wave)*amp*renderMotion;
            boolean cycleAccepted=cycleStrength>0.001F&&sink.emitFrame(wave>=0?CYCLE_POS:CYCLE_NEG,cycleStrength);
            EntityCameraBehaviorDiagnostics.crawl(true,false,false,Pose.SWIMMING,blend,motionWeight,phase,wave,cycleStrength,postureAccepted||cycleAccepted);
        }
        private static float approach(float v,float target,float step){return v<target?Math.min(target,v+step):Math.max(target,v-step);}
    }

    private abstract static class Base implements EntityCameraBehavior {
        public void onAttach(MountCameraContext context, CameraEffectSink sink) { reset(); }
        public void onRender(MountCameraContext context, CameraEffectSink sink) {}
        public void onDetach(MountCameraContext context, CameraEffectSink sink) { reset(); }
        void reset() {}
        EntityPlayerSP player(MountCameraContext c) { return c.getRider() instanceof EntityPlayerSP ? (EntityPlayerSP)c.getRider() : null; }
        static float clamp(double value, double min, double max) { return (float)(value < min ? min : value > max ? max : value); }
        static float lerp(float a,float b,float p){return a+(b-a)*p;}
        static double lerp(double a,double b,float p){return a+(b-a)*p;}
    }

    /** One bounded pulse per takeoff; no vertical-velocity pitch carried into support. */
    private static final class Jump extends Base {
        private static final CameraImpulse FRAME = CameraImpulse.builder("combatives:player_jump")
                .rotation(-0.9F,0,0).translation(0,0.012F,0.008F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private boolean grounded = true;
        private float age = 1, previousAge = 1, strength;
        void reset() { grounded=true; age=previousAge=1; strength=0; }
        public void onTick(MountCameraContext c, CameraEffectSink sink) {
            EntityPlayerSP p=player(c); EntityMotionSample m=c.getMotion();
            if(p==null||m.isDiscontinuity()){reset();grounded=p==null||p.onGround;return;}
            previousAge=age; age+=0.05F;
            if(grounded&&!p.onGround&&!p.isInWater()&&!p.isOnLadder()&&m.getVerticalVelocity()>0.08D) {
                strength=clamp(m.getVerticalVelocity()/0.42D,0,1);
                previousAge=0; age=0.05F;
            }
            if(p.onGround) strength=0;
            grounded=p.onGround;
        }
        public void onRender(MountCameraContext c, CameraEffectSink sink) {
            float t=lerp(previousAge,age,c.getPartialTicks());
            if(t<0.35F&&strength>0) sink.emitFrame(FRAME,strength*compressionPulse(t,0.065F,32F));
        }
    }

    private static final class Landing extends Base {
        private static final CameraImpulse FRAME = CameraImpulse.builder("combatives:player_landing")
                .rotation(3.2F,0,0).translation(0,-0.11F,-0.018F).duration(0.1F).priority(CameraPriority.NORMAL).build();
        private static final CameraImpulse ROLL_POS = CameraImpulse.builder("combatives:player_landing")
                .rotation(0,0,1).duration(0.1F).priority(CameraPriority.NORMAL).build();
        private static final CameraImpulse ROLL_NEG = CameraImpulse.builder("combatives:player_landing")
                .rotation(0,0,-1).duration(0.1F).priority(CameraPriority.NORMAL).build();
        private boolean grounded = true;
        private double fastestDescent;
        private float greatestFallDistance, age=1, previousAge=1, strength, rollBias;
        void reset(){grounded=true;fastestDescent=0;greatestFallDistance=strength=rollBias=0;age=previousAge=1;}
        public void onTick(MountCameraContext c, CameraEffectSink sink) {
            EntityPlayerSP p=player(c); EntityMotionSample m=c.getMotion();
            if(p==null||m.isDiscontinuity()){reset();grounded=p==null||p.onGround;return;}
            previousAge=age; age+=0.05F;
            if(!p.onGround) {
                if(grounded) strength=0; // A new airborne phase cannot inherit the previous landing.
                fastestDescent=Math.min(fastestDescent,m.getVerticalVelocity());
                greatestFallDistance=Math.max(greatestFallDistance,p.fallDistance);
            } else if(!grounded) {
                double preImpact=Math.min(fastestDescent,m.getPreviousVelocityY());
                double impactSpeed=Math.max(0,-preImpact);
                double momentumLoss=Math.max(0,m.getVerticalVelocity()-preImpact);
                double speedEnergy=clamp((impactSpeed-0.12D)/0.82D,0,1);
                double impulseEnergy=clamp(momentumLoss/0.82D,0,1);
                double distanceEnergy=1D-Math.exp(-Math.max(0,greatestFallDistance-1D)/7D);
                float energy=clamp(speedEnergy*0.55D+impulseEnergy*0.27D+distanceEnergy*0.18D,0,1);
                strength=(float)Math.pow(energy,1.3D);
                rollBias=clamp(m.getLateralAcceleration()*0.28D,-0.18D,0.18D);
                previousAge=0; age=0.05F;
                fastestDescent=0; greatestFallDistance=0;
                EntityCameraBehaviorDiagnostics.motionEvent("landing","energy="+energy+" strength="+strength+" preImpactVelocity="+preImpact);
            }
            grounded=p.onGround;
            EntityCameraBehaviorDiagnostics.motionSample("landing",m);
        }
        public void onRender(MountCameraContext c, CameraEffectSink sink) {
            float t=lerp(previousAge,age,c.getPartialTicks());
            if(t>=0.5F||strength<=0||!CombativesConfig.enableLandingCameraFeedback)return;
            float value=strength*compressionPulse(t,0.065F,28F);
            sink.emitFrame(FRAME,value);
            if(Math.abs(rollBias)>0.001F) sink.emitFrame(rollBias>=0?ROLL_POS:ROLL_NEG,value*Math.abs(rollBias));
        }
    }

    /** Smooth 65 ms loading followed by the exact critically damped displacement solution. */
    private static float compressionPulse(float age, float attack, float omega) {
        if(age<0)return 0;
        if(age<attack){float t=age/attack;return t*t*(3F-2F*t);}
        float t=(age-attack)*omega;
        return (1F+t)*(float)Math.exp(-t);
    }
    private static final class Freefall extends Base {
        private int fallingTicks; private float intensity,previousIntensity;
        void reset(){fallingTicks=0;intensity=previousIntensity=0;}
        public void onTick(MountCameraContext c,CameraEffectSink sink){
            EntityPlayerSP p=player(c);EntityMotionSample m=c.getMotion();if(p==null||m.isDiscontinuity()){reset();return;}
            previousIntensity=intensity;
            if(p.onGround){fallingTicks=0;intensity=previousIntensity=0;return;}
            boolean unsupported=!p.onGround && m.getVerticalVelocity() < -0.27D && m.getVerticalAcceleration() < 0.08D;
            fallingTicks=unsupported?fallingTicks+1:0;
            float speedEnvelope=clamp((-m.getVerticalVelocity()-0.24D)*1.55D,0,1);
            float timeEnvelope=fallingTicks<3?0:clamp((fallingTicks-2)/18D,0,1);
            float target=speedEnvelope*timeEnvelope;
            intensity+=(target-intensity)*(target>intensity?0.14F:0.24F);
            if(CombativesConfig.debugCamera&&((fallingTicks==3)||(fallingTicks==0&&intensity>0.01F)))EntityCameraBehaviorDiagnostics.motionEvent("freefall","active="+(fallingTicks>=3)+" intensity="+intensity+" speedEnvelope="+speedEnvelope+" timeEnvelope="+timeEnvelope);
            EntityCameraBehaviorDiagnostics.motionSample("freefall",m);
        }
        public void onRender(MountCameraContext c,CameraEffectSink sink){
            float value=lerp(previousIntensity,intensity,c.getPartialTicks());
            if(value>0.01F&&CombativesConfig.enablePlayerFreefallCamera) sink.emitFrame(CameraImpulse.builder("combatives:player_freefall")
                .rotation(1.0F,0,0).translation(0,-0.045F,0.012F).duration(0.1F).priority(CameraPriority.BACKGROUND).build(),clamp(value,0,1));
        }
    }

    private static final class Inertia extends Base {
        private double forward,lateral,turnLag; private float contribution,compositionWeight;
        private double previousForward,previousLateral,previousTurnLag;
        private float previousContribution,previousCompositionWeight;
        private boolean grounded=true; private int takeoffBlend,landingBlend;
        void reset(){forward=lateral=turnLag=previousForward=previousLateral=previousTurnLag=0;contribution=previousContribution=0;compositionWeight=previousCompositionWeight=1;grounded=true;takeoffBlend=landingBlend=0;}
        public void onTick(MountCameraContext c,CameraEffectSink sink){
            EntityMotionSample m=c.getMotion();if(m.isDiscontinuity()||!CombativesConfig.enablePlayerInertiaCamera){reset();return;}
            previousForward=forward;previousLateral=lateral;previousTurnLag=turnLag;
            previousContribution=contribution;previousCompositionWeight=compositionWeight;
            EntityPlayerSP p=player(c);boolean onGround=p==null||p.onGround;boolean ascending=!onGround&&m.getVerticalVelocity()>0.04D;
            if(grounded&&ascending)takeoffBlend=4;
            if(!grounded&&onGround)landingBlend=3;
            double rawForward=m.getForwardAcceleration(),rawLateral=m.getLateralAcceleration();
            if(takeoffBlend>0){
                // A jump impulse changes the position-derived horizontal acceleration for one or two
                // samples.  Preserve the pre-jump momentum and slew toward airborne input instead of
                // interpreting that sampling transient as a camera impulse.
                forward=slew(forward,rawForward,0.045D,0.22D);
                lateral=slew(lateral,rawLateral,0.045D,0.22D);
                takeoffBlend--;
            } else {forward=rawForward;lateral=rawLateral;}
            turnLag=m.getYawRate()*m.getHorizontalSpeed()/18D;
            compositionWeight=landingBlend>0?0.35F+(3-landingBlend)*0.325F:1F;if(landingBlend>0)landingBlend--;
            contribution=clamp(Math.max(Math.abs(forward)*2.8D,Math.max(Math.abs(lateral)*2.2D,Math.abs(turnLag)))*compositionWeight,0,1);
            grounded=onGround;
            EntityCameraBehaviorDiagnostics.inertia(rawForward,rawLateral,forward,lateral,turnLag,contribution,ascending,takeoffBlend,compositionWeight);
            EntityCameraBehaviorDiagnostics.motionSample("inertia",m);
        }
        private static double slew(double filtered,double raw,double limit,double alpha){double delta=(raw-filtered)*alpha;return filtered+(delta<-limit?-limit:delta>limit?limit:delta);}
        public void onRender(MountCameraContext c,CameraEffectSink sink){
            float p=c.getPartialTicks();
            double f=lerp(previousForward,forward,p),l=lerp(previousLateral,lateral,p),t=lerp(previousTurnLag,turnLag,p);
            if(lerp(previousContribution,contribution,p)>0.008F)sink.emitFrame(CameraImpulse.builder("combatives:player_inertia")
                .rotation(clamp(-f*6.3D,-1.6D,1.6D),clamp(-t*0.32D,-0.4D,0.4D),clamp(-l*2.8D-t*0.28D,-0.9D,0.9D))
                .translation(clamp(-l*0.012D,-0.012D,0.012D),0,clamp(f*0.018D,-0.018D,0.018D)).duration(0.1F).priority(CameraPriority.BACKGROUND).build(),clamp(lerp(previousCompositionWeight,compositionWeight,p),0,1));
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
                float severity=clamp((loss-0.08D)*2.7D+impulse*0.65D,0,1);float strength=severity;
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
