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

Relevant camera configuration keys are `enableHorseCamera`, `horseCameraAmplitude`, `horseTerrainImpulse`, `horseLanding`, `horseTurningRoll`, `enableCrawlCamera`, `crawlCameraAmplitude`, and `crawlTransitionMillis` (clamped to 150–250 ms). Multipliers default to `1.0`; horses default on and crawling retains its opt-in default off. These are presentation-only client settings.

For end-to-end provider investigation, `verboseCameraDebug` logs registry matching, lifecycle callbacks, tick/render execution, provider-specific crawl/horse state, sink acceptance, manager accumulation, and the final controller transform. Crawl state must not be inferred from `isActuallySwimming()` in this port because that method describes the prone animation pose, not water propulsion; use the authoritative swim flag and environment alongside `Pose.SWIMMING`.

## Camera pipeline and timing

The client-only controller is registered on the FML client tick bus after movement input.
At tick END it saves previous state, advances simulation once, and stores current state.
Integrated-server pause freezes simulation. Player/world replacement, position-history
rebasing, and death clear presentation history; camera disable clears cosmetic effects
without disabling tactical lean presentation or authoritative gameplay lean.

| Effect | Target source | Persistent update | Render sampling | Space and application |
| --- | --- | --- | --- | --- |
| Movement lean | Local forward/strafe input, pose scale | Input filter and lean springs on client tick; three fixed spring substeps | Previous/current pitch and roll | View axes at `orientCamera` HEAD |
| Walk/sprint bob | Vanilla walked distance, camera yaw, pose intensity | Vanilla player tick endpoints | Interpolated distance and amplitudes; no phase integration or vertical-velocity pitch | View axes at camera HEAD; separate hand matrix |
| Movement FOV | Horizontal speed and sprint/swim state | Speed/FOV filters on client tick | Previous/current modifier | Sampled before projection's `getFOVModifier` |
| Slide blend | Existing locomotion state; slide entry remains deferred | Client tick filter | Previous/current blend | View-local vertical offset |
| Explosion shake | Vanilla explosion packet, distance falloff, player-local direction | Client tick, three fixed 1/60-second spring substeps | Previous/current channels and bob suppression | View-local camera HEAD |
| API recoil/collision | One submitted impulse per action | Age/expiry on client tick | Analytic envelope and sine at interpolated age in seconds | Common accumulator, then view-local camera HEAD |
| API continuous vibration | Handle strength/position/enabled state | Lifetime cleanup on client tick | Sine on interpolated game time, frequency in Hz | Common accumulator, then view-local camera HEAD |
| Landing/freefall/inertia | Entity world velocity/acceleration projected into local axes | Provider `onTick` from client tick, even if no frame renders | Provider previous/current state | Common frame intents, then view-local camera HEAD |
| Jump | One grounded-to-ascending transition, bounded takeoff speed | Tick-owned pulse age | Analytic compression/recovery on interpolated age | Common frame intents |
| Crawl/horse cycle | Pose/speed or mount limb swing | Tick-owned phase, gait filters, impulses | Interpolated phase/envelopes; horse limb swing uses vanilla interpolation | Common frame intents |
| Tactical lean roll | Accepted wall-limited lean, client `leanInterpolation` | Client tick presentation filter | Previous/current lean with render-time wall limit | View Z at camera HEAD, including third-person; separate hand matrix |
| Tactical eye presentation | Accepted lean target and shared wall trace | Same previous/current visual lean as roll | Shared `getRenderLean(partialTicks)` semantics; wall-limited interpolated eye offset | Inverse world vector at camera TAIL, first-person only |

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
remain weapon-owned. Combatives reads their ADS state at client tick to attenuate
ambient camera motion, without adding an aiming kick, zoom transform,
weapon-model sway, or breathing oscillator. API submissions and handle changes should
run on the client thread; providers must advance state in `onTick` and only
sample/emit presentation in `onRender`.

## Live camera controls and presentation tuning

Open **Options → Camera Effects**. The Forge **Mods → Combatives → Config**
entry also opens this screen, including when another mod occupies the Options
button slot. Sliders apply immediately, including to already active impulses.
They save to the existing Combatives configuration on release or screen close.
The screen does not pause the game, so changes can be observed live. Hover a
slider for its scope and default. Reset restores all displayed visual sliders
and feature switches, including the existing crawl default off.

All settings below are client cosmetics. The legacy lean-response key
remains in `movement`; every other live key remains or is added in `camera`. Existing
stored values and public strength fields are preserved. Server config, lean
reach, locomotion, collision, bullet origins, spread and aiming recoil are not
editable here. Multipliers are applied during sampling before provider mixing
and saturation; the master scales the final cosmetic translation, rotation,
hand bob and extra FOV. Tactical eye displacement and roll are independent of
master intensity. There is no live lean-roll angle or strength control: changing
the tilt alone could exaggerate or weaken the cue without changing actual peek
distance. The existing file-only `movement.maxLeanRoll` key is retained for
configuration compatibility, with its established 7-degree default. Roll and eye
presentation still transition together toward accepted lean. Zero cosmetic
intensity does not stop provider ticks or impulse aging.

| Control / config key | Default | Range |
| --- | ---: | ---: |
| Master / `cameraEffectsIntensity` | 1.0 | 0–2 |
| Walk / `walkBobIntensity` | 1.25 | 0–3 |
| Sprint / `sprintBobIntensity` | 1.4 | 0–3 |
| Movement lean / `movementLeanIntensity` | 1.35 | 0–3 |
| Jump / `jumpCameraIntensity` | 0.65 | 0–3 |
| Landing / `landingFeedbackStrength` | 1.0 | 0–4 |
| Freefall / `playerFreefallCameraStrength` | 1.0 | 0–4 |
| Camera recoil / `recoilCameraIntensity` | 1.25 | 0–3 |
| Explosion / `explosionFeedbackStrength` | 1.0 | 0–4 |
| Inertia / `playerInertiaCameraStrength` | 1.0 | 0–4 |
| Collision / `playerCollisionCameraStrength` | 1.0 | 0–4 |
| Crawl / `crawlCameraAmplitude` | 1.0 | 0–3 |
| Horse gait / `horseCameraAmplitude` | 1.0 | 0–3 |
| Horse terrain / `horseTerrainImpulse` | 1.0 | 0–3 |
| Horse landing / `horseLanding` | 1.0 | 0–3 |
| Horse turn / `horseTurningRoll` | 1.0 | 0–3 |
| Lean response / `movement.leanInterpolation` | 0.24 per tick | 0.05–1 |
| ADS ambient motion / `adsCameraMotion` | 0.5 | 0–1 |
| Extra movement FOV / `movementFovIntensity` | 1.25 | 0–3 |
| Slide dip / `slideCameraIntensity` | 1.0 | 0–3 |
| Movement response / `cameraResponse` | 1.0 | 0.5–2 |

The movement response changes only the tick-owned input/speed/FOV filters using
`1 - (1 - alpha)^response`. It does not change recoil duration, vibration
frequency, landing recovery or authoritative state. Lean response has its own
tick coefficient; at the default 0.24 it reaches about 95% of a fixed target in
550 ms. ADS attenuation uses a separate tick filter and affects bob, movement
lean/FOV, inertia and continuous crawl/horse motion. Impacts and recoil retain
their individual controls. Optional HMG/Flan's detection is cached and read-only;
unknown weapon mods retain full ambient output.

| Audited effect | Authored magnitude and response after this pass |
| --- | --- |
| Walk / sprint bob | Vanilla distance-driven sine/absolute cosine. Existing pose weights remain (walk 0.85, sprint 1.15, sneak 0.55, crawl 0.40, swim 0.35), multiplied by separate controls. No integration on render. |
| Movement lean | Existing 3 × 60 Hz tick spring steps (roll stiffness 0.08, velocity retention 0.72; pitch 0.07/0.74). Targets remain −1.35 degrees strafe and −0.45 forward, with a default 1.35 output multiplier. |
| Inertia / camera sway | Bounded sampled acceleration/turn response; authored pitch/yaw/roll bounds increased from 1.05/0.28/0.65 to 1.6/0.4/0.9 degrees. Existing takeoff slew and landing composition suppression remain. |
| Movement FOV / slide | Sprint/swim targets retain 3.5%/2.5% limits before the new 1.25 FOV multiplier. FOV uses the existing converted tick filter with the response control. Slide retains a 0.035-block dip and its converted tick blend, with a separate output intensity. |
| Player collision | Existing speed-loss threshold/cooldown and 200 ms pulse with 20 ms attack retained; directional templates remain 2.1-degree pitch, 1.5-degree roll and 0.025/0.045-block lateral/forward translation. Its control now scales active output live. |
| Jump | New bounded −0.9-degree, +0.012-block takeoff template. Default multiplier 0.65 and background priority 0.65 keep it restrained. 65 ms loading, critically damped recovery with omega 32/s; ends by 350 ms or support. |
| Freefall | Delayed speed/time envelope, tick rise 0.14 and fall 0.24. Template increases pitch 0.72 → 1 degree while reducing vertical dip 0.052 → 0.045 blocks. Cleared on support so it cannot trail into landing. |
| Landing | One contact capture, bounded energy^1.3. Full-energy template 3.2 degrees / −0.11 blocks / −0.018 forward; 65 ms loading and critically damped settle with omega 28/s. No velocity/displacement injection or oscillating recovery. |
| Recoil camera | Existing API envelopes, authored HMG kick/punch/sustained-fire axes and lifetimes retained. Default 1.25 multiplier affects `combatives:weapon_fire` and `hmg_overdrive:hmg_recoil_*`; custom IDs otherwise retain their authored intent. |
| Explosion | Existing 8-degree pitch / 4.5-degree roll shock plus spring translation; preserved axis stiffness 55–70 and damping 9–11, three fixed substeps. Already strong enough to reach existing output limits, so default strength stays 1.0. Scaling is now live, rather than baked into injected velocity. |
| ADS camera motion | New ambient retention control, default 50%; no new zoom or gameplay recoil. |
| Tactical lean | Established 7-degree roll retained and excluded from live intensity controls/master scaling. Camera eye/roll, hand roll and local biped presentation sample the same visual lean interpolation with wall limits. Actual peek distance comes from authoritative lateral displacement, never the roll angle. |
| Crawl / mounted | Crawl posture/cycle/pull templates and 150–250 ms transition retained. Horse loading changes 1.8 → 2.2 degrees and 0.066 → 0.075 blocks; recovery −1.2 → −1.5 degrees and 0.042 → 0.05 blocks. Terrain/landing/turn remain separate. Other mount providers keep their API intent and base-camera ownership. |
| Weapon-model sway / idle breathing | No Combatives provider exists. Inspected HMG imported `AnimationClient.shootPresentation` owns a 300 ms model-root noise tail, attenuated by its own ADS blend; it does not submit camera intent. No extra breathing oscillator is introduced. |
| Other API intent | Environmental rumble retains its 8 Hz, 1.2-second preset (1.2-degree pitch/roll, 0.02/0.025-block translation); other custom, suppression and vehicle-impact intent keeps its authored axes, priorities and envelopes. The master scales final output without altering lifetime or frequency. |

Template values are before priority weights, strength controls, shared saturation
and final master scaling; they are not promises of final screen displacement.
Landing's exact recovery is `(1 + omega*t)*exp(-omega*t)`, equivalent to unit-mass
stiffness 784/s² and damping 56/s at omega 28/s. It intentionally has no rebound
overshoot: compression returns smoothly to neutral rather than a bounce train.
At 365 ms from contact only about 0.21% of peak compression remains.

The previous active landing provider was already overdamped, but held its target
for 2–4 ticks and carried recovery velocity into a long tail. Vanilla bob also
added velocity-derived `cameraPitch` across jump/landing, and freefall faded after
support. These overlapping presentations contributed to the disproportionate
response. The unused second landing spring in `ShakeController` had no callers;
it was not evidence of double landing impulses and has been removed. Landing now
has one active provider and no inherited airborne spring state.

Tactical lean deliberately separates gameplay target, previous/current visual
lean, and interpolated render lean. The rendered eye offset formerly used raw
lean while roll interpolated, causing the centered crosshair's world presentation
to jump ahead of camera roll. Both now use the same interpolation. Combatives
has no separate lean-offset HUD renderer; its fixed centered reticle and hand
share the camera presentation. Future visual indicators should sample
`TacticalLeanCamera.getRenderLean(partialTicks)`. Gameplay targeting and HMG/Flan's
bullet origins still use `InteractionRay` and accepted raw lean immediately;
they are intentionally not delayed during the visual transition. Remote pose
and Flan's hit snapshots remain authoritative, while the local biped is visual.

## In-game regression matrix

Use the same world, config, weapon and actions at **30, 60, 144, 240 FPS and
600+ FPS and uncapped**. At each rate test first person, rear third
person and front third person; stationary, forward/backward, left/right strafe,
all diagonals, sprint, jump/landing, ADS, single shots/bursts and left/right lean.
Repeat representative combinations at north/east/south/west and diagonal yaw
angles, plus the ±180-degree yaw seam and looking up/down. Compare magnitude,
cycle frequency, recovery duration and third-person camera position/distance.
Hold lean while stationary to isolate roll from intentional movement bob/shake.
Test normal jumps and landings from several heights, semi-auto and automatic
fire, lean left/right during firing and ADS, and perspective changes while an
effect is active. Check camera/reticle/hand lean transitions together. Adjust
each slider live, including zero → nonzero during active recoil/shake; check
Reset, close/reopen and restart persistence. Lean walls and server-side shots
must keep the same authoritative geometry regardless of visual settings.
Repeat with bob/lean/shake disabled individually, vanilla view bob enabled/disabled,
pause/resume, teleport, respawn, dimension change, mount/dismount, MPM and optional
camera/weapon mods. Check both integrated and dedicated-server sessions for
unchanged aim, targeting and tactical wall limits.

These are manual acceptance checks. Source/math checks and compilation do not
establish in-game behavior or cross-mod renderer ordering. High-frequency API
vibration can be undersampled at low FPS; its underlying frequency and envelope
remain game-time based rather than frame-count based.
