package com.glowingfederal.combatives.client.camera.internal;

import com.combatives.api.camera.entity.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;

/** Owns client provider instances and lifecycle, but never interprets motion or renders camera output. */
public final class EntityCameraBehaviorManager {
    public static final EntityCameraBehaviorManager INSTANCE = new EntityCameraBehaviorManager();
    private final EntityMotionSampler sampler = new EntityMotionSampler();
    private final Map<EntityBehaviorRegistration, EntityCameraBehavior> active = new LinkedHashMap<EntityBehaviorRegistration, EntityCameraBehavior>();
    private Entity mount;
    private Entity previousMount;
    private long lastTick = Long.MIN_VALUE;
    private EntityCameraBehaviorManager() {}

    public void update(EntityPlayerSP rider, float partialTicks) {
        Entity current = rider == null ? null : rider.ridingEntity;
        long tick = rider == null ? 0 : rider.ticksExisted;
        Entity previous = mount;
        MountTransition transition = transition(previous, current);
        if (current != mount) {
            previousMount = previous;
            MountCameraContext detach = context(rider, previous, previous, null, transition, tick, partialTicks);
            for (EntityCameraBehavior behavior : active.values()) behavior.onDetach(detach, EntityCameraEffectSink.INSTANCE);
            active.clear(); sampler.reset(); mount = current; lastTick = Long.MIN_VALUE;
        }
        if (mount == null) return;
        sampler.sampleTick(mount, tick);
        MountCameraContext context = context(rider, mount, previousMount, current, transition, tick, partialTicks);
        reconcile(context);
        if (tick != lastTick) {
            for (EntityCameraBehavior behavior : active.values()) behavior.onTick(context, EntityCameraEffectSink.INSTANCE);
            lastTick = tick;
        }
        for (EntityCameraBehavior behavior : active.values()) behavior.onRender(context, EntityCameraEffectSink.INSTANCE);
    }

    private MountCameraContext context(EntityPlayerSP rider, Entity sampled, Entity previous, Entity current, MountTransition transition, long tick, float partialTicks) {
        return new MountCameraContext(rider, current, previous, transition, tick, partialTicks, sampled == null ? EntityMotionSample.EMPTY : sampler.render(partialTicks));
    }
    private void reconcile(MountCameraContext context) {
        List<EntityBehaviorRegistration> matches = EntityCameraBehaviorRegistry.matching(mount);
        for (java.util.Iterator<Map.Entry<EntityBehaviorRegistration,EntityCameraBehavior>> it=active.entrySet().iterator();it.hasNext();) {
            Map.Entry<EntityBehaviorRegistration,EntityCameraBehavior> entry=it.next();
            if (!matches.contains(entry.getKey())) { entry.getValue().onDetach(context,EntityCameraEffectSink.INSTANCE); it.remove(); }
        }
        for (EntityBehaviorRegistration registration : matches) if (!active.containsKey(registration)) {
            EntityCameraBehavior behavior=registration.getFactory().create();
            if (behavior != null) { active.put(registration,behavior); behavior.onAttach(context,EntityCameraEffectSink.INSTANCE); }
        }
    }
    private static MountTransition transition(Entity oldMount, Entity newMount) {
        if (oldMount == newMount) return MountTransition.NONE;
        if (oldMount == null) return MountTransition.ATTACHED;
        if (newMount == null) return MountTransition.DETACHED;
        return MountTransition.CHANGED;
    }
    public void reset(EntityPlayerSP rider) {
        MountCameraContext context=context(rider,mount,mount,null,MountTransition.DETACHED,rider==null?0:rider.ticksExisted,0);
        for(EntityCameraBehavior behavior:active.values())behavior.onDetach(context,EntityCameraEffectSink.INSTANCE);
        active.clear(); previousMount=mount; mount=null; lastTick=Long.MIN_VALUE; sampler.reset();
    }
}
