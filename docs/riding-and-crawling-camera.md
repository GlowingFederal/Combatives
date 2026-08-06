# Riding and crawling camera design

Both features are entity camera behaviors, not standalone animations. `EntityCameraBehaviorManager` selects either the unmounted local player or its current mount, owns provider attach/detach, samples motion once per tick, and routes render contributions into `CameraEffectManager`. The existing controller then combines this output with bob, lean, shake, FOV, and the sole render transform.

## Horse

The built-in registration matches `EntityHorse` assignably. A continuous normalized speed curve drives smoothly filtered amplitude and frequency: idle is nearly still, walk is gentle, trot reaches the quickest cadence, and gallop grows larger while its cadence eases. A phase oscillator emits vertical, pitch, and fore/aft frames without yaw. Sampled turn rate is low-pass filtered and emitted as roll only, with a two-degree template limit. Supported vertical acceleration may emit a tiny terrain impulse; an air-to-ground transition derives energy from preserved descent and fall distance and submits a shared landing-style compression impulse.

## Crawl

The player pose provider distinguishes crawling from swimming. Enter and exit move one fixed step per tick toward their target, derived from the configured 150–250 ms duration, so they are monotonic and cannot overshoot. The envelope supplies a small downward/forward posture contribution. Horizontal speed controls crawl phase, which emits small pitch, vertical, and forward/back frames with no roll. Each half-cycle may submit a tiny downward/forward pull impulse.

## Lifecycle and performance

Attach, detach, disabled state, and motion discontinuities reset all cached values. Thus mounting, dismounting, teleporting, dimension/player changes, death/respawn camera reset, and camera disabling cannot retain stale contributions. Fixed render-frame intents are cached statically; only infrequent terrain, landing, and crawl-cycle impulses enter the allocating impulse path. All final damping, stacking, nonlinear saturation, and hard clamps remain centralized.
