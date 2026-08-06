# Combatives Camera API

Combatives exposes a stable, versioned camera-effect API under `com.combatives.api.camera`. External mods describe effects; Combatives remains the only owner of camera state, interpolation, accumulation, nonlinear saturation, hard clamps, and rendering.

## Version and capabilities

Use `CombativesCameraAPI.getApiVersion()` and `CombativesCameraAPI.getCapabilities()` instead of checking the Combatives mod version. API version 1 supports preset effects, custom impulses, positional falloff, granular pitch/yaw/roll rotation capabilities, translation, FOV contributions, and continuous-effect handles. `ROTATION_YAW` is advertised only because yaw is now consumed by validation, active state, envelope sampling, stacking, nonlinear saturation, an independent hard yaw clamp, final frame output, and the Combatives-owned visual-only render transform.

## Presets

Most integrations should call:

```java
CombativesCameraAPI.trigger(CameraEffectType.EXPLOSION, context, strength);
```

`strength` is normalized to `0.0F..1.0F`; Combatives resolves the final tuned behavior.

## Context

`CameraEffectContext` can carry an optional source entity, world position, radius, and deterministic seed. Integrations should pass source data and let Combatives calculate distance falloff and directional influence.

## Custom effects

Advanced integrations may submit `CameraImpulse` descriptions. Every custom impulse must use a namespaced ID such as `mcheli:rotor_vibration`. The fields are effect intent only; they are not direct camera transforms and still pass through Combatives validation, stacking, saturation, and clamps. Yaw impulses are accepted as visual-only horizontal camera offsets; Combatives never writes `player.rotationYaw`, `player.prevRotationYaw`, mouse deltas, mouse input consumption, or `Entity#setAngles` for camera API yaw. Render composition keeps the existing order of translation, pitch, roll, with yaw inserted between pitch and roll so API yaw follows the established rotation path without creating another mixin or injection point.

## Continuous effects

`CombativesCameraAPI.startContinuousEffect(...)` returns a `CameraEffectHandle` that can update strength, update position, enable/disable, or stop the effect. Handles are intended for vibration, machinery, underwater drift, rotor shake, and similar persistent effects.

## Networking helpers

`CameraNetworkAPI` is present as a dedicated server-safe facade for future server-originated camera-effect packets. API version 1 keeps packet payloads as effect descriptions and does not expose client renderer classes.

## Entity camera behavior framework

Entity-driven camera intent is registered through `EntityCameraBehaviorRegistry`. Registrations pair an extensible `EntityMatcher` with a factory; every matching registration is activated, and the factory creates a separate stateful `EntityCameraBehavior` for each mount lifecycle. Built-in matchers cover exact classes, assignable classes, entity registry identifiers, and arbitrary matcher predicates. Registration objects can be retained and unregistered at runtime.

Providers receive immutable `MountCameraContext` values in `onAttach`, once-per-client-tick `onTick`, per-camera-update `onRender`, and `onDetach`. The context contains only the rider, current/previous mount, transition, client tick, partial ticks, and a generic `EntityMotionSample`. A provider must keep its interpretation state in its own instance and must not change player rotation or issue render calls.

`EntityMotionSampler` observes any `Entity` independently of `MovementSnapshot`. Its sample exposes render-interpolated position/orientation, current and previous velocity and acceleration, angular velocity, tick timestamp, and discontinuity status. A first sample, skipped tick, entity replacement, or movement over the teleport threshold is a discontinuity and resets derivatives.

Providers send intent only through `CameraEffectSink`:

* `contribute` adds a frame contribution;
* `submitImpulse` enters the existing impulse lifecycle;
* `startContinuous` enters the existing continuous-effect lifecycle.

The client sink delegates to `CameraEffectManager`, which still validates channels, applies positional falloff and priority, accumulates effects, and performs saturation clamps. `CameraController` still owns the final camera state and render mixins remain the only render integration. An empty registry therefore produces exactly the previous visual output.

### Integration sketch

```java
EntityBehaviorRegistration registration = EntityCameraBehaviorRegistry.register(
    "example:rideable_camera",
    EntityMatchers.registryId("ExampleRideable"),
    new EntityCameraBehaviorFactory() {
        public EntityCameraBehavior create() {
            return new ExampleRideableBehavior();
        }
    });
```

The external mod supplies the provider and may use a custom `EntityMatcher` when class or registry matching is insufficient. Multiple mods and multiple registrations may match the same mount; their contributions compose in registration order through the manager. Providers should stop any continuous handles they own during `onDetach`. No optional-mod class needs to be referenced by Combatives.

### Extension points

* `EntityMatcher` for arbitrary selection policies.
* `EntityCameraBehaviorFactory` for fresh per-mount provider state.
* `EntityCameraBehavior` lifecycle callbacks for interpretation.
* `CameraEffectSink` for frame, impulse, and continuous intent.
* `EntityMotionSampler` and immutable samples for reusable physical observation.
