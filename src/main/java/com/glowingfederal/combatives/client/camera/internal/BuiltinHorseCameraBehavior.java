package com.glowingfederal.combatives.client.camera.internal;

import com.combatives.api.camera.CameraImpulse;
import com.combatives.api.camera.CameraPriority;
import com.combatives.api.camera.entity.*;
import com.glowingfederal.combatives.config.CombativesConfig;
import java.util.Collections;
import net.minecraft.entity.passive.EntityHorse;

/** Built-in horse registration; other rideables can register the same provider contract independently. */
public final class BuiltinHorseCameraBehavior {
    private static boolean registered;
    private BuiltinHorseCameraBehavior() {}
    public static synchronized void register(){if(registered)return;EntityBehaviorMetadata metadata=new EntityBehaviorMetadata("combatives",Collections.<String,String>emptyMap());EntityCameraBehaviorRegistry.register("combatives:horse_riding",35,metadata,EntityMatchers.assignableClass(EntityHorse.class),new EntityCameraBehaviorFactory(){public EntityCameraBehavior create(){return new Horse();}});registered=true;}

    static final class Horse implements EntityCameraBehavior {
        private static final CameraImpulse UP=CameraImpulse.builder("combatives:horse_gait_up").rotation(1.5F,0,0).translation(0,0.052F,-0.026F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse DOWN=CameraImpulse.builder("combatives:horse_gait_down").rotation(-1.5F,0,0).translation(0,-0.052F,0.026F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse ROLL_LEFT=CameraImpulse.builder("combatives:horse_turn_left").rotation(0,0,-2F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse ROLL_RIGHT=CameraImpulse.builder("combatives:horse_turn_right").rotation(0,0,2F).duration(0.1F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse TERRAIN=CameraImpulse.builder("combatives:horse_terrain").translation(0,-0.018F,0).rotation(0.2F,0,0).duration(0.13F).attackTime(0.025F).priority(CameraPriority.BACKGROUND).build();
        private static final CameraImpulse LAND=CameraImpulse.builder("combatives:horse_landing").translation(0,-0.09F,-0.018F).rotation(3.2F,0,0).duration(0.32F).attackTime(0.055F).priority(CameraPriority.NORMAL).build();
        private float amplitude,frequency,roll,phase;private boolean grounded=true;private double descent;private float fall;
        public void onAttach(MountCameraContext c,CameraEffectSink sink){reset();}
        public void onDetach(MountCameraContext c,CameraEffectSink sink){reset();}
        private void reset(){amplitude=frequency=roll=phase=0;grounded=true;descent=0;fall=0;}
        public void onTick(MountCameraContext c,CameraEffectSink sink){EntityMotionSample m=c.getMotion();EntityHorse horse=c.getMount() instanceof EntityHorse?(EntityHorse)c.getMount():null;if(horse==null||m.isDiscontinuity()||!CombativesConfig.enableHorseCamera){reset();grounded=horse==null||horse.onGround;return;}
            float speed=clamp(m.getHorizontalSpeed(),0,0.65),gait=smooth(speed,0.015F,0.42F);float targetAmp=0.035F+0.965F*gait*gait;float trot=smooth(gait,0.18F,0.68F),gallop=smooth(gait,0.68F,1F);float targetFreq=0.12F+1.23F*trot-0.27F*gallop;amplitude+=(targetAmp-amplitude)*0.14F;frequency+=(targetFreq-frequency)*0.12F;phase+=frequency*0.31F;
            float targetRoll=clamp(m.getYawRate()/14F,-1,1);roll+=(targetRoll-roll)*0.18F;
            if(!horse.onGround){descent=Math.min(descent,m.getVerticalVelocity());fall=Math.max(fall,horse.fallDistance);}else if(!grounded){float energy=landingEnergy(descent,m.getPreviousVelocityY(),fall);if(energy>0.03F)sink.emitImpulse(scaleLanding(energy));descent=0;fall=0;}
            if(horse.onGround&&grounded&&Math.abs(m.getVerticalAcceleration())>0.12D&&Math.abs(m.getVerticalVelocity())<0.24D){float s=clamp(CombativesConfig.horseTerrainImpulse,0,1);if(s>0)sink.emitImpulse(CameraImpulse.builder("combatives:horse_terrain").translation(0,TERRAIN.getTranslateY()*s,0).rotation(TERRAIN.getPitch()*s,0,0).duration(TERRAIN.getDuration()).attackTime(TERRAIN.getAttackTime()).priority(CameraPriority.BACKGROUND).build());}grounded=horse.onGround;
        }
        public void onRender(MountCameraContext c,CameraEffectSink sink){if(!CombativesConfig.enableHorseCamera)return;float wave=(float)Math.sin(phase+c.getPartialTicks()*frequency*0.31F),strength=clamp(Math.abs(wave)*amplitude*CombativesConfig.horseCameraAmplitude,0,1);sink.emitFrame(wave>=0?UP:DOWN,strength);float rs=clamp(Math.abs(roll)*CombativesConfig.horseTurningRoll,0,1);if(rs>0.001F)sink.emitFrame(roll>=0?ROLL_RIGHT:ROLL_LEFT,rs);}
        private CameraImpulse scaleLanding(float energy){float s=clamp(energy*CombativesConfig.horseLanding,0,1);return CameraImpulse.builder("combatives:horse_landing").translation(0,LAND.getTranslateY()*s,LAND.getTranslateZ()*s).rotation(LAND.getPitch()*s,0,0).duration(LAND.getDuration()).attackTime(LAND.getAttackTime()).priority(CameraPriority.NORMAL).build();}
        static float landingEnergy(double descent,double previous,float fall){double impact=Math.max(0,-Math.min(descent,previous));return clamp((impact-0.12D)/0.82D*0.72D+(1D-Math.exp(-Math.max(0,fall-1)/7D))*0.28D,0,1);}
        static float smooth(float v,float low,float high){float t=clamp((v-low)/(high-low),0,1);return t*t*(3-2*t);}
        static float clamp(double v,double lo,double hi){return(float)(v<lo?lo:v>hi?hi:v);}
    }
}
