# Riding and crawling camera design

Both features are entity camera behaviors, not standalone animations. `EntityCameraBehaviorManager` selects either the unmounted local player or its current mount, owns provider attach/detach, samples motion once per tick, and routes render contributions into `CameraEffectManager`. The existing controller then combines this output with bob, lean, shake, FOV, and the sole render transform.

## Horse

The built-in registration matches `EntityHorse` assignably. A continuous normalized speed curve drives smoothly filtered weight and cadence: idle is nearly still, walking is gentle, and faster strides become progressively heavier without gait thresholds. Render-interpolated limb swing drives asymmetric vertical, pitch, and fore/aft loading without yaw; acceleration and deceleration add restrained head inertia. Sampled turn rate is low-pass filtered and emitted as roll only, with a two-degree template limit. Supported vertical acceleration may emit a tiny terrain impulse; an air-to-ground transition derives energy from preserved descent and fall distance and submits a shared landing-style compression impulse.

## Crawl

The player pose provider distinguishes crawling from swimming. Enter and exit move one fixed step per tick toward their target, derived from the configured 150–250 ms duration, so they are monotonic and cannot overshoot. The envelope supplies a small downward/forward posture contribution. Horizontal speed controls crawl phase, which emits small pitch, vertical, and forward/back frames with no roll. Each half-cycle may submit a tiny downward/forward pull impulse.

## Lifecycle and performance

Attach, detach, disabled state, and motion discontinuities reset all cached values. Thus mounting, dismounting, teleporting, dimension/player changes, death/respawn camera reset, and camera disabling cannot retain stale contributions. Fixed render-frame intents are cached statically; only infrequent terrain, landing, and crawl-cycle impulses enter the allocating impulse path. All final damping, stacking, nonlinear saturation, and hard clamps remain centralized.

## Diagnostic trace

With `verboseCameraDebug`, a crawl trace now reports the authoritative pose, swim flag, water state, transition weight, speed envelope, phase, generated wave/strength, and whether the frame sink accepted output. Existing provider matching/lifecycle/execution logs establish registration and sampling; sink logs establish submission; `CameraEffectManager` active/final-output logs establish accumulation; and `CameraController`'s final-transform log establishes rendering. This end-to-end trace found that crawl detection incorrectly rejected `isActuallySwimming()`, although that method deliberately returns true for every prone `SWIMMING` pose, including land crawling. Crawl detection now follows the movement system's authoritative distinction: prone pose, no swim flag, and not in water.

Horse stride output uses the mount's render-interpolated `limbSwing` and `limbSwingAmount` when available instead of advancing an unrelated perfect oscillator. Speed continuously shapes cadence and quadratically shapes weight, forward acceleration adds restrained head inertia, and an asymmetric loading curve plus second harmonic makes compression deeper than recovery. There are still no named gait switches or speed thresholds between gait states.

## Third-person crawl model

The physical crawl continues to use `Pose.SWIMMING`; collision, movement,
camera, eye height, and targeting are unchanged. The client now distinguishes a
land crawl for rendering when the synchronized pose is `SWIMMING`, the actual
swim flag is clear, and the player is not in water. This also covers a forced
low pose after the crawl key is released under an obstruction. Actual swimming
continues through the original swimming stroke.

The vanilla biped has rigid arms and legs with shoulder and hip pivots, so it
cannot display real bent elbows or knees. The dedicated crawl animator instead
uses a restrained forearm-support silhouette: arms rest forward and outward,
legs rest level with the torso with a small opposing spread, and motion amplitude scales with
vanilla's render-interpolated `limbSwingAmount`. A sinusoidal diagonal cycle
pairs each arm with the opposite leg. The head retains vanilla look yaw and
pitch before the renderer applies the whole-player prone transform, avoiding
the swimming-specific forced head pitch during a land crawl.

The prone renderer remains the single owner of crawl grounding. Its land-only
correction translates the model downward by four model pixels before applying
the prone rotation. Applying it in that order is required by OpenGL's
post-multiplied transform stack: a Y translation after the rotation would move
along prone-local Z instead of lowering the model in world space. The limb
animation adds no world-space offsets, so it cannot affect camera or
authoritative player geometry. Models derived from `ModelBiped` receive the
pose through the existing mixin, while renderers that replace the vanilla biped
model remain responsible for mapping Combatives' synchronized pose visually.
