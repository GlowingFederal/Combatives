package com.glowingfederal.combatives.client.camera.internal;

import com.combatives.api.camera.CameraEffectHandle;
import com.combatives.api.camera.CameraImpulse;
import com.combatives.api.camera.ContinuousCameraEffect;
import com.combatives.api.camera.entity.CameraEffectSink;

/** Thin validated adapter; CameraEffectManager retains all accumulation and clamp ownership. */
public final class EntityCameraEffectSink implements CameraEffectSink {
    public static final EntityCameraEffectSink INSTANCE = new EntityCameraEffectSink();
    private EntityCameraEffectSink() {}
    public boolean contribute(CameraImpulse contribution, float strength) { return CameraEffectManager.submitFrameContribution(contribution, strength); }
    public boolean submitImpulse(CameraImpulse impulse) { return CameraEffectManager.submitImpulse(impulse); }
    public CameraEffectHandle startContinuous(ContinuousCameraEffect effect) { return CameraEffectManager.startContinuousEffect(effect); }
}
