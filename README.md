# Combatives

Combatives is a Battlefield-inspired movement and camera overhaul foundation for Minecraft Forge 1.7.10.

## Aqua Acrobatics Legacy port foundation

Combatives now ports and refactors the working modern swimming and crawling behavior from Aqua Acrobatics Legacy into the active `com.glowingfederal.combatives` package tree. Aqua Acrobatics Legacy is included in this repository as public-domain (Unlicense) reference material and port foundation, but it is not required at runtime and no old Aqua mod ID or package is used by compiled Combatives source.

The port is intentionally scoped to player pose, collision bounds, eye-height state, water movement, crawl input/keybind handling, client-to-server crawl state networking, standing-space checks, and server-safe DataWatcher synchronization needed for swimming/crawling. Aqua build logic, metadata, branding, resources, optional compatibility systems, and unrelated aquatics features remain unported.

## Current foundation

- Mod name: Combatives
- Mod ID: `combatives`
- Root package: `com.glowingfederal.combatives`
- Forge target: `1.7.10-10.13.4.1614`
- UniMixins-compatible Combatives movement mixins are queued from `mixins.combatives.common.json` by the core plugin, with client-only render/input/model mixins side-gated in `CombativesCorePlugin`
- Common/client proxy structure plus a Combatives early mixin loader for common player pose-state injection
- Always-on modern swimming and crawling behavior, refactored from the Aqua Acrobatics Legacy reference source
- Dedicated crawl keybind (`C` by default) with common-preInit Combatives networking for authoritative server-side crawl state; client-side access to the Minecraft singleton uses an SRG-first helper so production jars do not call an unmapped `Minecraft.getMinecraft()` name
- Explicit multiplayer pose synchronization from the owning client to the server and from the server to tracking clients, including join/respawn/dimension/tracking lifecycle resends
- Lightweight horizontal momentum controller that shapes `motionX`/`motionZ` from input intent toward vanilla velocity targets while keeping collision, gravity, jumping, step handling, and `moveEntity` authoritative; normal movement is shaped through the vanilla `moveFlying` hook, while crawl and swim/water movement are shaped only in their dedicated Combatives branches, and the production redirect invokes vanilla movement through an Entity invoker so non-player entities keep vanilla travel without calling an unmapped deobfuscated method name
- Client/server movement snapshots for future camera work, exposing velocity, acceleration, normalized speed, wish direction, grounded/sprint/sneak/crawl/swim/underwater/airborne flags, landing impact, and turn values
- Client-only first-person camera foundation with read-only movement sampling, subtle procedural camera bob, recreated vanilla-style first-person arm/item bob, movement lean, movement FOV, a damped shake impulse framework, render-time camera state sampling, vanilla `S27PacketExplosion` client feedback, client-side previous-tick landing feedback, low-level `Entity#setAngles` spike diagnostics for player look rotation, hard-clamped visual-only pitch/yaw/roll/translation transforms, with API yaw applied only in the Combatives-owned render transform and never written to player look rotation
- Initial config options:
  - `enableCombativesCamera = true`
  - `enableProceduralBob = true`
  - `enableMovementLean = true`
  - `enableMovementFov = true`
  - `enableCameraRotations = true`
  - `enableCameraShake = true`
  - `maxCameraYawDegrees = 4.0`
  - `enableLandingCameraFeedback = true`
  - `enableExplosionCameraFeedback = true`
  - `debugMovement = false`
  - `verboseMovementDebug = false`
  - `debugCamera = false`
  - `verboseCameraDebug = false`

## Movement diagnostics

Combatives movement diagnostics are split into three levels so normal crawling and swimming stay quiet by default:

- Off/default (`debugMovement = false`, `verboseMovementDebug = false`): only warnings, errors, invalid state, rejected unsafe actions, and missing required runtime state are logged.
- General (`debugMovement = true`): high-level lifecycle diagnostics such as crawl/swim enter/exit, rejected crawl exits due to clearance, server authority rejections, and one-time Combatives movement mixin summaries.
- Verbose (`verboseMovementDebug = true`): per-frame/per-tick movement diagnostics, including render/model hook traces, grounding values, DataWatcher pose changes, and repeated eye-height/size recalculations. Verbose movement diagnostics imply the general movement diagnostics stream.

## Future work

Future work will build on the camera foundation with weapon-specific first-person behavior and additional camera impulses.



## Production mixin smoke test

For production validation, build a reobfuscated jar and run it in a real Forge 1.7.10 client without `-Dmixin.env.disableRefMap=true`. The packaged jar must include `mixins.combatives.refmap.json` at the jar root, both Combatives mixin configs must reference that exact refmap name, and the client log must not contain `No refMap loaded` or `InvalidMixinException` for Combatives mixins. This smoke test specifically protects the `EntityPlayerMixin` pose interface from failing before `EntityPlayerMPMixin` or client pose hooks use it.

## Development notes

See `docs/aqua-port-map.md` for the inspected Aqua source map and port/skipped-class rationale. Do not compile anything inside the reference folder. Do not compile or add generated binary files to commits or pull requests. Common and client Combatives movement mixins are loaded through `com.glowingfederal.combatives.loading.CombativesCorePlugin`, with the client render/input/model mixins added only on the physical client side. Do not port unrelated Aqua Acrobatics systems unless they are required for Combatives swimming/crawling behavior to function.

## Build outputs

A production `assemble` or `build` now creates both supported distribution jars from the same source tree:

- `build/libs/Combatives-<version>.jar` is the standard build and keeps the existing configuration behavior.
- `build/libs/Combatives-Fairplay-<version>.jar` is the official Fairplay build variant.

The Fairplay jar carries the display name `Combatives Fairplay` and contains build metadata that makes `BuildInfo.FAIRPLAY_BUILD` resolve to `true` at runtime. The standard jar carries the normal `Combatives` display name and resolves the same flag to `false`.

## Fairplay build behavior

Fairplay is a build/distribution variant only. It does not add multiplayer verification, networking, handshakes, hashing, anti-tamper behavior, or server enforcement.

When the Fairplay jar starts, Combatives logs one informational message stating that Fairplay mode is active and gameplay/camera settings are locked. It then resolves gameplay and camera settings from the canonical defaults used by the standard config, instead of reading user-provided gameplay or camera config values.

Fairplay uses a separate `Combatives-Fairplay.cfg` file so an existing standard Combatives config cannot interfere. That file intentionally exposes only:

- `debug`
- `verboseDebug`

These two options enable the existing movement and camera diagnostic streams for debugging while leaving gameplay and camera behavior locked to the canonical defaults.
