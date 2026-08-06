package com.combatives.api.camera.entity;

public final class EntityBehaviorRegistration {
    private final String id;
    private final EntityMatcher matcher;
    private final EntityCameraBehaviorFactory factory;

    public EntityBehaviorRegistration(String id, EntityMatcher matcher, EntityCameraBehaviorFactory factory) {
        if (id == null || id.length() == 0) throw new IllegalArgumentException("id");
        if (matcher == null) throw new IllegalArgumentException("matcher");
        if (factory == null) throw new IllegalArgumentException("factory");
        this.id = id;
        this.matcher = matcher;
        this.factory = factory;
    }
    public String getId() { return id; }
    public EntityMatcher getMatcher() { return matcher; }
    public EntityCameraBehaviorFactory getFactory() { return factory; }
}
