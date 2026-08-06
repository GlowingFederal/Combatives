# Entity camera behavior architecture

## Architecture summary

The framework has one directional ownership path:

`camera entity -> EntityMotionSampler -> provider -> CameraEffectSink -> CameraEffectManager -> CameraController -> render mixins`

Observation, interpretation, camera intent, accumulation, final camera state, and rendering remain separate concerns. The camera entity is the local player when unmounted and the ridden entity when mounted. The built-in player registrations are the first consumers; integrations can add generic mounted-entity registrations without exclusive ownership, and every matching provider contributes in stable order.

## Built-in generic player providers

Four independent providers match the local player and consume the same immutable sample created once per tick:

- **Landing** tracks the fastest sampled downward velocity until the grounded transition, then combines that velocity, sampled vertical acceleration, and fall distance as a secondary severity signal. It submits a short vertical compression, slight pitch, and acceleration-directed micro-roll impulse.
- **Freefall** requires at least four consecutive unsupported ticks below the downward-speed threshold. This excludes steps, slabs, terrain jitter, and ordinary jump arcs; its restrained downward float eases in with fall speed and fades promptly when support returns.
- **Inertia** maps sample-local forward acceleration to acceleration/braking pitch, lateral acceleration to roll and lateral lag, and yaw rate weighted by horizontal speed to subtle directional head lag. It is a frame contribution and adds no input filtering or latency.
- **Collision** requires meaningful prior horizontal speed, abrupt speed loss, and horizontal acceleration together. Its cooldown and thresholds prevent stationary wall pushing from retriggering, while local acceleration supplies the physical pitch/roll/translation direction.

Landing and collision use finite impulses; freefall and inertia submit current-frame contributions. Every intent passes through `CameraEffectSink`, so these effects compose with explosions and retain the shared manager/controller safety clamps. Providers never change rotation, input, player movement, or entity motion.

The `camera` configuration category exposes an enable flag and `0.0`–`4.0` strength for each provider: `enablePlayerLandingCamera`/`playerLandingCameraStrength`, `enablePlayerFreefallCamera`/`playerFreefallCameraStrength`, `enablePlayerInertiaCamera`/`playerInertiaCameraStrength`, and `enablePlayerCollisionCamera`/`playerCollisionCameraStrength`.

General diagnostics report provider lifecycle plus landing, freefall, and collision events. Verbose camera diagnostics add provider emissions, inertia contributions, and shared world/local velocity, acceleration, speed, yaw-rate, and discontinuity values. Formatting and timing remain guarded when diagnostics are disabled.

## Registration, ordering, and identity

`EntityCameraBehaviorRegistry.register(id, priority, metadata, matcher, factory)` registers a provider. The legacy three-argument overload remains available with priority `0` and unknown ownership metadata. Registration results are sorted by:

1. higher integer priority first;
2. registration ID in ascending lexical order;
3. monotonically increasing registration sequence as the final tie breaker.

All matches remain active. Priority controls callback and intent-submission order only; it does not arbitrate, suppress, or replace another provider. Integrations should use globally namespaced IDs and should not deliberately duplicate an ID.

`EntityBehaviorMetadata` records the owning mod and an immutable string attribute map. `EntityBehaviorProviderInfo` exposes the ID, priority, metadata, and diagnostic sequence directly to contextual factories, avoiding a registry lookup. A `ContextualEntityCameraBehaviorFactory` receives both provider identity and the environment. It also extends the legacy factory for source compatibility; implementations should have their no-argument `create()` delegate or return the same provider shape.

## Shared behavior environment

`EntityBehaviorEnvironment` is an immutable mount-lifecycle snapshot shared by contextual factories. It contains the current world, a read-only configuration view, logger, Entity Camera API version, stateless helpers, and a deterministic random-stream factory. Random streams are created per provider and salt, so providers do not couple behavior through shared mutable randomness.

The environment is intentionally separate from `MountCameraContext`. It is generic, contains no behavior-specific settings, and does not hold per-mount interpretation state. Every mutable gait phase, filter, cooldown, or other future state belongs only to the provider instance created for that mount lifecycle.

## Provider lifecycle

For each camera entity (the player while unmounted, otherwise its mount) the manager performs the following lifecycle:

1. Sample the mount once for the current client tick.
2. Evaluate every matcher and sort all matches deterministically.
3. Create missing provider instances and call `onAttach` in provider order.
4. Call `onTick` once when the client tick changes, in provider order.
5. Call `onRender` for every camera update, in provider order.
6. Reconcile runtime registry changes. Removed/nonmatching instances receive `onDetach`; retained instances are reordered without losing their state.
7. On dismount, mount replacement, reset, or registry removal, call `onDetach` and discard the instance.

A factory returning `null` opts out of that reconciliation pass. A provider must not cache state globally, mutate rider rotation, manipulate rendering, or call `CameraEffectManager` directly. Mount replacement creates a fresh environment and provider set.

## Motion sampling

`EntityMotionSampler` observes arbitrary entities independently from player movement shaping. One immutable `EntityMotionSample` contains:

- render-interpolated world position, yaw, and pitch;
- world X/Y/Z current and previous velocity;
- world X/Y/Z current and previous acceleration;
- mount-local forward, lateral, and vertical velocity;
- mount-local forward, lateral, and vertical acceleration;
- horizontal and three-dimensional total speed;
- wrapped yaw rate and pitch rate per tick.

Forward and lateral components are projected once using the sample's render yaw. Vertical values remain world Y. Linear values are blocks per tick (or blocks per tick squared for acceleration); angular rates are degrees per tick. Providers should use these derived channels rather than independently projecting world vectors.

The first observation, an entity change, a skipped client tick, or movement beyond the teleport threshold remains a discontinuity. A discontinuity zeroes current/previous derivatives and angular rates, while preserving the observed position and orientation. Providers should reset their own filters when `isDiscontinuity()` is true.

## Camera intent sink

`CameraEffectSink` is the only provider-facing effect route. Its intent vocabulary is:

- `emitFrame(intent, strength)` for the current accumulation frame;
- `emitImpulse(intent)` for a validated finite impulse;
- `beginContinuous(intent)` for an effect controlled through its returned handle.

The older method names remain compatibility aliases. The sink reveals no accumulator, clamp, render, or controller state. `CameraEffectManager` continues to own validation, stacking, expiration, saturation, and accumulation. The provider-scoped internal decorator only attributes diagnostic emissions before delegating to that manager adapter.

## Diagnostics

General camera diagnostics report provider attach/detach with registration ID, priority, owning mod, and mount. Verbose camera diagnostics additionally report the complete matched/ordered list, callback execution duration in nanoseconds, and each emitted effect's kind, effect ID, and acceptance result. All hot-path timing, string construction, and emission logging is guarded by the existing diagnostic switches; disabled diagnostics perform only cheap boolean checks.

## Updated extension points and future readiness

- `EntityMatcher` and `EntityMatchers` select vanilla, modded, seat, or unknown third-party entities without core dependencies.
- Ordered registry registrations allow broad and specific integrations to coexist.
- `ContextualEntityCameraBehaviorFactory` supplies identity and common immutable resources.
- Per-mount `EntityCameraBehavior` instances isolate all integration state.
- Normalized `EntityMotionSample` channels remove repeated vehicle/gait math.
- `CameraEffectSink` accepts intent without exposing camera-core internals.
- Immutable metadata and scoped diagnostics make third-party integrations attributable.

Horse gait/rocking, boats, vehicle inertia/impacts/suspension, aircraft/engine/rotor vibration, rideable mobs, and custom seats can therefore be separate registrations and provider classes. None require edits to `CameraController` or `CameraEffectManager`.

## Compatibility guarantee

The built-in providers apply only to the local unmounted player. Explosion effects, movement lean, procedural bob, public Camera API submissions, continuous effects, mounted-entity compatibility, and the render pipeline retain their existing paths. On mounting, the player providers detach and the same generic manager immediately samples and matches the mount for future integrations.
