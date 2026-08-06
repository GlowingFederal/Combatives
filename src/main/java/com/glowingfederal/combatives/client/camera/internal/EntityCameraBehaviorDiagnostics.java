package com.glowingfederal.combatives.client.camera.internal;

import com.combatives.api.camera.CameraImpulse;
import com.combatives.api.camera.entity.EntityBehaviorRegistration;
import com.glowingfederal.combatives.Combatives;
import com.glowingfederal.combatives.config.CombativesConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;

final class EntityCameraBehaviorDiagnostics {
    private EntityCameraBehaviorDiagnostics() {}
    static void matches(Entity mount, java.util.List<EntityBehaviorRegistration> matches) {
        if (!CombativesConfig.verboseCameraDebug) return;
        StringBuilder order=new StringBuilder(); for(EntityBehaviorRegistration r:matches){if(order.length()>0)order.append(", ");order.append(r.getId()).append('@').append(r.getPriority());}
        Combatives.logger.info("Entity camera providers matched mount={} order=[{}]", mount(mount), order);
    }
    static void lifecycle(String event, EntityBehaviorRegistration r, Entity mount) {
        if (!CombativesConfig.debugCamera) return;
        Combatives.logger.info("Entity camera provider {} id={} priority={} owner={} mount={}",event,r.getId(),r.getPriority(),r.getMetadata().getOwningMod(),mount(mount));
    }
    static long begin() { return CombativesConfig.verboseCameraDebug ? System.nanoTime() : 0L; }
    static void execution(String callback, EntityBehaviorRegistration r, Entity mount, long started) {
        if (started == 0L) return;
        Combatives.logger.info("Entity camera provider execution callback={} id={} owner={} mount={} durationNs={}",callback,r.getId(),r.getMetadata().getOwningMod(),mount(mount),System.nanoTime()-started);
    }
    static void effect(EntityBehaviorRegistration r,String kind,CameraImpulse effect,boolean accepted) {
        if (!CombativesConfig.verboseCameraDebug) return;
        Combatives.logger.info("Entity camera provider emitted id={} owner={} kind={} effect={} accepted={}",r.getId(),r.getMetadata().getOwningMod(),kind,effect==null?"null":effect.getEffectId(),accepted);
    }
    private static String mount(Entity entity){if(entity==null)return "none";String id=EntityList.getEntityString(entity);return (id==null?entity.getClass().getName():id)+"#"+entity.getEntityId();}
}
