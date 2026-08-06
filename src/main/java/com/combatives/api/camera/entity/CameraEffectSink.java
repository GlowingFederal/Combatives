package com.combatives.api.camera.entity;

import com.combatives.api.camera.CameraEffectHandle;
import com.combatives.api.camera.CameraImpulse;
import com.combatives.api.camera.ContinuousCameraEffect;

/** The sole provider-facing route into the existing camera effect pipeline. */
public interface CameraEffectSink {
    boolean contribute(CameraImpulse contribution, float strength);
    boolean submitImpulse(CameraImpulse impulse);
    CameraEffectHandle startContinuous(ContinuousCameraEffect effect);
}
