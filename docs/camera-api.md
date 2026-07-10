# Combatives Camera API

Combatives exposes a stable, versioned camera-effect API under `com.combatives.api.camera`. External mods describe effects; Combatives remains the only owner of camera state, interpolation, accumulation, nonlinear saturation, hard clamps, and rendering.

## Version and capabilities

Use `CombativesCameraAPI.getApiVersion()` and `CombativesCameraAPI.getCapabilities()` instead of checking the Combatives mod version. API version 1 supports preset effects, custom impulses, positional falloff, rotation, translation, FOV contributions, and continuous-effect handles.

## Presets

Most integrations should call:

```java
CombativesCameraAPI.trigger(CameraEffectType.EXPLOSION, context, strength);
```

`strength` is normalized to `0.0F..1.0F`; Combatives resolves the final tuned behavior.

## Context

`CameraEffectContext` can carry an optional source entity, world position, radius, and deterministic seed. Integrations should pass source data and let Combatives calculate distance falloff and directional influence.

## Custom effects

Advanced integrations may submit `CameraImpulse` descriptions. Every custom impulse must use a namespaced ID such as `mcheli:rotor_vibration`. The fields are effect intent only; they are not direct camera transforms and still pass through Combatives validation, stacking, saturation, and clamps.

## Continuous effects

`CombativesCameraAPI.startContinuousEffect(...)` returns a `CameraEffectHandle` that can update strength, update position, enable/disable, or stop the effect. Handles are intended for vibration, machinery, underwater drift, rotor shake, and similar persistent effects.

## Networking helpers

`CameraNetworkAPI` is present as a dedicated server-safe facade for future server-originated camera-effect packets. API version 1 keeps packet payloads as effect descriptions and does not expose client renderer classes.
