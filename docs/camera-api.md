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

Advanced integrations may submit `CameraImpulse` descriptions. Every custom impulse must use a namespaced ID such as `mcheli:rotor_vibration`. The fields are effect intent only; they are not direct camera transforms and still pass through Combatives validation, stacking, saturation, and clamps. Yaw impulses are accepted as visual-only horizontal camera offsets; Combatives never writes `player.rotationYaw`, `player.prevRotationYaw`, mouse deltas, mouse input consumption, or `Entity#setAngles` for camera API yaw. Render composition uses view-space translation, pitch, yaw, then roll before vanilla camera placement and orientation. It does not append camera-local effects to an already world-oriented matrix.

## Continuous effects

`CombativesCameraAPI.startContinuousEffect(...)` returns a `CameraEffectHandle` that can update strength, update position, enable/disable, or stop the effect. Handles are intended for vibration, machinery, underwater drift, rotor shake, and similar persistent effects.

## Networking helpers

`CameraNetworkAPI` is present as a dedicated server-safe facade for future server-originated camera-effect packets. API version 1 keeps packet payloads as effect descriptions and does not expose client renderer classes.

## Entity camera behavior framework

Entity-driven camera intent is registered through `EntityCameraBehaviorRegistry`. Registrations pair an extensible `EntityMatcher` with a factory; every matching registration is activated, and the factory creates a separate stateful `EntityCameraBehavior` for each mount lifecycle. Matches execute deterministically by descending priority, lexical registration ID, and registration sequence. Built-in matchers cover exact classes, assignable classes, entity registry identifiers, and arbitrary matcher predicates. Registration objects can be retained and unregistered at runtime.

Providers receive immutable `MountCameraContext` values in `onAttach`, once-per-client-tick `onTick`, per-camera-update `onRender`, and `onDetach`. The context contains only the rider, current/previous mount, transition, client tick, partial ticks, and a generic `EntityMotionSample`. A provider must keep its interpretation state in its own instance and must not change player rotation or issue render calls.

`EntityMotionSampler` observes any `Entity` independently of `MovementSnapshot`. Its sample exposes render-interpolated position/orientation; current and previous world velocity and acceleration; forward/lateral/vertical velocity and acceleration; horizontal/total speed; yaw/pitch rates; tick timestamp; and discontinuity status. A first sample, skipped tick, entity replacement, or movement over the teleport threshold is a discontinuity and resets derivatives.

Providers send intent only through `CameraEffectSink` (`emitFrame`, `emitImpulse`, and `beginContinuous`; the original names remain aliases). Contextual factories additionally receive immutable `EntityBehaviorEnvironment` resources and `EntityBehaviorProviderInfo` identity. Full lifecycle, ordering, units, diagnostics, and extension contracts are documented in [Entity camera behaviors](entity-camera-behaviors.md).

* `emitFrame` adds a frame contribution;
* `emitImpulse` enters the existing impulse lifecycle;
* `beginContinuous` enters the existing continuous-effect lifecycle.

The client sink delegates to `CameraEffectManager`, which validates channels, applies positional falloff and priority, accumulates effects, and performs saturation clamps. `CameraController` owns tick simulation and final camera state; render mixins remain the only GL integration. An empty registry removes provider contributions while retaining movement bob/lean, explosion shake, and API effects.

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

The external mod supplies the provider and may use a custom `EntityMatcher` when class or registry matching is insufficient. Multiple mods and multiple registrations may match the same mount; their contributions compose in deterministic priority/ID/sequence order through the manager. Providers should stop any continuous handles they own during `onDetach`. No optional-mod class needs to be referenced by Combatives.

### Extension points

* `EntityMatcher` for arbitrary selection policies.
* `EntityCameraBehaviorFactory` for fresh per-mount provider state.
* `ContextualEntityCameraBehaviorFactory` for provider identity and immutable shared resources.
* `EntityCameraBehavior` lifecycle callbacks for interpretation.
* `CameraEffectSink` for frame, impulse, and continuous intent.
* `EntityMotionSampler` and immutable samples for reusable physical observation.

## Built-in continuous riding and crawling providers

Combatives registers `combatives:horse_riding` with an assignable-class matcher for `EntityHorse`. Consequently horse subclasses participate automatically, while another mod can support a different rideable through the existing registry without changing `CameraController`. The provider consumes only `EntityMotionSample`: smooth speed envelopes select gait amplitude/frequency, sampled yaw rate produces damped roll, and vertical support transitions produce subtle terrain/landing impulses. It never copies mount pitch/yaw or writes entity rotation.

The unmounted player registry includes `combatives:player_crawl`. Pose state selects a monotonic enter/exit envelope, while sampled horizontal speed advances the crawl phase. Its cycle frames and pull impulses flow through `CameraEffectSink`, alongside every other provider and public API effect. Provider detach/reset and sampler discontinuities clear phase, gait, landing, and transition history on mount changes, teleports, world changes, death/camera reset, and camera disable.

Relevant camera configuration keys are `enableHorseCamera`, `horseCameraAmplitude`, `horseTerrainImpulse`, `horseLanding`, `horseTurningRoll`, `enableCrawlCamera`, `crawlCameraAmplitude`, and `crawlTransitionMillis` (clamped to 150–250 ms). Multipliers default to `1.0`; both feature toggles default on. These are presentation-only client settings.

For end-to-end provider investigation, `verboseCameraDebug` logs registry matching, lifecycle callbacks, tick/render execution, provider-specific crawl/horse state, sink acceptance, manager accumulation, and the final controller transform. Crawl state must not be inferred from `isActuallySwimming()` in this port because that method describes the prone animation pose, not water propulsion; use the authoritative swim flag and environment alongside `Pose.SWIMMING`.

## Camera pipeline and timing

The client-only controller is registered on the FML client tick bus after movement input.
At tick END it saves previous state, advances simulation once, and stores current state.
Integrated-server pause freezes simulation. Player/world replacement, position-history
rebasing, and death clear presentation history; camera disable clears cosmetic effects
without disabling the authoritative tactical eye offset or its cosmetic roll setting.

| Effect | Target source | Persistent update | Render sampling | Space and application |
| --- | --- | --- | --- | --- |
| Movement lean | Local forward/strafe input, pose scale | Input filter and lean springs on client tick; three fixed spring substeps | Previous/current pitch and roll | View axes at `orientCamera` HEAD |
| Walk/sprint bob | Vanilla walked distance, camera yaw/pitch, pose intensity | Vanilla player tick endpoints | Interpolated distance and amplitudes; no phase integration | View axes at camera HEAD; separate hand matrix |
| Movement FOV | Horizontal speed and sprint/swim state | Speed/FOV filters on client tick | Previous/current modifier | Sampled before projection's `getFOVModifier` |
| Slide blend | Existing locomotion state; slide entry remains deferred | Client tick filter | Previous/current blend | View-local vertical offset |
| Explosion shake | Vanilla explosion packet, distance falloff, player-local direction | Client tick, three fixed 1/60-second spring substeps | Previous/current channels and bob suppression | View-local camera HEAD |
| API recoil/collision | One submitted impulse per action | Age/expiry on client tick | Analytic envelope and sine at interpolated age in seconds | Common accumulator, then view-local camera HEAD |
| API continuous vibration | Handle strength/position/enabled state | Lifetime cleanup on client tick | Sine on interpolated game time, frequency in Hz | Common accumulator, then view-local camera HEAD |
| Landing/freefall/inertia | Entity world velocity/acceleration projected into local axes | Provider `onTick` from client tick, even if no frame renders | Provider previous/current state | Common frame intents, then view-local camera HEAD |
| Crawl/horse cycle | Pose/speed or mount limb swing | Tick-owned phase, gait filters, impulses | Interpolated phase/envelopes; horse limb swing uses vanilla interpolation | Common frame intents |
| Tactical lean roll | Accepted wall-limited lean, client `leanInterpolation` | Client tick presentation filter | Previous/current lean with render-time wall limit | View Z at camera HEAD, including third-person; separate hand matrix |
| Tactical eye displacement | Predicted/server-approved lean and shared wall trace | Gameplay/network state, not camera simulation | Interpolated eye/yaw, same physical offset as interaction ray | Inverse world vector at camera TAIL, first-person only |

`partialTicks` is an interpolation fraction, never elapsed time. Input, FOV and slide
filter coefficients retain their nominal old 60 Hz response by converting three
reference-frame steps into one 50 ms tick response. Existing effect limits remain;
no FPS cap or direction-specific correction participates in simulation.

The modelview call order is vanilla hurt camera, vanilla bob if enabled and not
replaced, portal transform, Combatives view translation/pitch/yaw/roll, tactical
roll, vanilla roll/third-person placement/yaw/pitch/eye translation, then the
first-person tactical world displacement. Vanilla resets modelview per camera
pass. Camera transforms intentionally remain active while the world renders.
The hand pass resets modelview and owns its existing push/pop around hurt/bob
and item rendering; Combatives adds tactical roll and procedural hand bob once
inside that scope. It does not push/pop away the world camera or leave an extra
matrix stack entry.

For a vanilla view matrix `V`, cosmetic rotation `R` produces `R * V`. If camera
center `c` satisfies `V * c = 0`, it also satisfies `R * V * c = 0`: roll/pitch/yaw
cannot rotate the third-person boom around the player. The former `V * R` rotated
world axes and changed the camera center. Cosmetic translations deliberately add
small local bob/shake displacement; tactical third-person lean adds no translation.

The canonical Minecraft-yaw forward vector is `(-sin(yaw), cos(yaw))`; right is
`(-cos(yaw), -sin(yaw))`. Movement input is already local. The public motion sampler
retains its existing **positive-left lateral** channel, opposite to semantic right;
its tick callbacks now receive current tick yaw (`partialTicks=1`) rather than an
arbitrary render fraction. Cardinal/diagonal headings therefore need no branches.

The inspected Forge 1.7.10 renderer has no `CameraSetup` event. Its relevant Forge
hooks are bed orientation and first-person hand routing. MPM's inspected
`EntityRendererAlt` delegates to the private vanilla camera pipeline; MCHeli
retains its mounted/dummy base-camera ownership. Other mods' secondary GL hooks
still compose in their own order and require integration testing.

HMG Overdrive's inspected optional `HMGRecoilBridge` submits finite kick/punch/
sustained-fire impulses here, with ADS-dependent intent. Combatives owns their
visual duration and axes. HMG's aiming recoil and Flan's client-tick recoil/ADS
remain weapon-owned; Combatives adds no second aiming kick, ADS transform,
weapon sway, or breathing oscillator. API submissions and handle changes should
run on the client thread; providers must advance state in `onTick` and only
sample/emit presentation in `onRender`.

## In-game regression matrix

Use the same world, config, weapon and actions at **30, 60, 144, 240 FPS and
uncapped (including 300–600+ FPS)**. At each rate test first person, rear third
person and front third person; stationary, forward/backward, left/right strafe,
all diagonals, sprint, jump/landing, ADS, single shots/bursts and left/right lean.
Repeat representative combinations at north/east/south/west and diagonal yaw
angles, plus the ±180-degree yaw seam and looking up/down. Compare magnitude,
cycle frequency, recovery duration and third-person camera position/distance.
Hold lean while stationary to isolate roll from intentional movement bob/shake.
Repeat with bob/lean/shake disabled individually, vanilla view bob enabled/disabled,
pause/resume, teleport, respawn, dimension change, mount/dismount, MPM and optional
camera/weapon mods. Check both integrated and dedicated-server sessions for
unchanged aim, targeting and tactical wall limits.

These are manual acceptance checks. Source/math checks and compilation do not
establish in-game behavior or cross-mod renderer ordering. High-frequency API
vibration can be undersampled at low FPS; its underlying frequency and envelope
remain game-time based rather than frame-count based.
