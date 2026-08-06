package com.combatives.api.camera.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.Entity;

/** Runtime registry. All matching registrations are active, in registration order. */
public final class EntityCameraBehaviorRegistry {
    private static final List<EntityBehaviorRegistration> REGISTRATIONS = new ArrayList<EntityBehaviorRegistration>();
    private EntityCameraBehaviorRegistry() {}

    public static synchronized EntityBehaviorRegistration register(String id, EntityMatcher matcher, EntityCameraBehaviorFactory factory) {
        EntityBehaviorRegistration registration = new EntityBehaviorRegistration(id, matcher, factory);
        REGISTRATIONS.add(registration);
        return registration;
    }
    public static synchronized boolean unregister(EntityBehaviorRegistration registration) { return REGISTRATIONS.remove(registration); }
    public static synchronized List<EntityBehaviorRegistration> registrations() {
        return Collections.unmodifiableList(new ArrayList<EntityBehaviorRegistration>(REGISTRATIONS));
    }
    public static synchronized List<EntityBehaviorRegistration> matching(Entity entity) {
        List<EntityBehaviorRegistration> result = new ArrayList<EntityBehaviorRegistration>();
        for (EntityBehaviorRegistration registration : REGISTRATIONS) {
            if (registration.getMatcher().matches(entity)) result.add(registration);
        }
        return result;
    }
}
