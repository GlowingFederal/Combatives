# Changelog

## Add lightweight momentum controller

- Added a horizontal `MovementController` that shapes player `motionX`/`motionZ` toward vanilla movement intent while leaving collision, gravity, jumping, step handling, and `moveEntity` authority intact.
- Added movement profiles for standing, sprinting, sneaking, crawling, swimming, and airborne movement with acceleration, deceleration, turn acceleration, drag, and air-control tuning.
- Added a client/server `MovementSnapshot` cache exposing velocity, acceleration, normalized speed, wish direction, grounded/sprint/sneak/crawl/swim/underwater/airborne state, landing impact, and turn values for future camera hooks.

## Fix multiplayer authoritative pose sync

- Added explicit client-to-server and server-to-client pose packets carrying pose, swimming, and crawl/forced-down state so tracking clients receive authoritative crawl/swim updates without waiting for collision correction.
- Added server lifecycle sync for join, respawn, dimension change, and start-tracking events, plus immediate rebroadcasts when crawl or pose state changes.
- Gated repeated size enforcement and bounding-box diagnostics so unchanged pose sizes do not spam movement logs every tick.

## Fix mixin shadows and construction-safe pose state

- Removed invalid inherited-method shadows from `EntityPlayerSPMixin` and call sprint/water/item/riding methods through the concrete client player instance instead.
- Added construction-safe pose fallback state so `getPose()` returns standing until the pose DataWatcher entry exists.
- Changed `setPose()` to update local pose state first and only write the DataWatcher after registration.

## Verify Aqua interaction and visual overlay call sites

- Ported Aqua's `PlayerControllerMP#onPlayerRightClick` actual-sneak redirect so forced crawl/crouch pose does not corrupt right-click interaction semantics.
- Added model animation diagnostics to prove when crawl/swim animation reaches `ModelBiped`.
- Updated the Aqua port map with exact method-level proof for `PlayerControllerMPMixin`, `FluidAccessor`, `BlockLiquidMixin`, and `ItemRendererMixin`.
- Documented that in-game debug capture could not be performed in this non-interactive container, so the new logs identify the runtime revert point when tested in-game with `debugMovement=true`.

## Port Aqua render, collision, and server pose parity

- Ported the Aqua renderer/model pose hooks for swim animation, player body transforms, first-person arm reset, and camera eye-height interpolation.
- Ported Aqua actual-sneak movement hooks, swimming water-probe adjustment, explicit EntityPlayerMP server eye-height/size enforcement, and local exact-collision push-out helpers.
- Restored Aqua-style crawl toggle semantics so key press toggles crawl forcing and key release does not clear the crawl state.
- Updated the Aqua port map with exact replacements and call-site-based skip reasons for previously suspicious omissions.

## Port complete Aqua swim and crawl system

- Expanded the Aqua source-level audit table to cover every inspected swim/crawl-adjacent source file and document ported versus intentionally skipped systems.
- Ported the connected Aqua local-player swim/crawl input flow, including movement input storage, actual sneak separation, forced-down movement correction, water sprint persistence, water sneak sinking, and sprint stop reasons.
- Added client/server bridge mixins for sending actual sneak state and correcting remote-player y-offset after pose synchronization.
- Added hard debug diagnostics for pose writes, DataWatcher corrections, swim-state changes, collision selection, bounding-box recalculation, and eye-height recalculation.

## Fix crawl key registration and persistent swimming

- Added client proxy and crawl key registration startup logs so the Controls-menu registration path is visible during startup.
- Added debug movement diagnostics for crawl key registration, crawl packet sends, swim-state entry/exit, and explicit swim cancellation reasons.
- Ported the missing Aqua local-player water sprint persistence hook so vanilla sprint cancellation no longer immediately reverts the swimming state.
- Updated the Aqua port map to document the additional client-side sprint/swim dependency.

## Complete Combatives swimming and crawling port

- Added the missing crawl keybind, localization, client tick input handling, and client-to-server crawl state packet/channel registration.
- Extended the player pose mixin with hold-to-crawl state, server-side crawl validation, standing-space blocking, low-ceiling crawl retention, and an eye-height hook used by crawl/swim poses.
- Expanded debug movement diagnostics for crawl input, crawl request acceptance/rejection, pose collision blocking, and client/server pose sync.
- Added `docs/aqua-port-map.md` documenting Aqua reference classes inspected, ported, rewritten, and intentionally skipped.

## Port Aqua swimming and crawling behavior into Combatives

- Ported/refactored Aqua Acrobatics Legacy swimming and crawling pose behavior into Combatives packages.
- Added player pose synchronization, bounding-box resizing, crawl/swim eye-height state, water movement, and scoped movement diagnostics.
- Registered the required player mixin in `mixins.combatives.json` without introducing an Angelica hard dependency.
- Documented that Aqua Acrobatics Legacy is public-domain/Unlicense reference material and that the runtime implementation is refactored under Combatives.

## Initial Combatives foundation

- Renamed the example mod foundation to Combatives with mod ID `combatives` and root package `com.glowingfederal.combatives`.
- Added UniMixins-compatible manifest metadata and `mixins.combatives.json`.
- Added common/client proxy classes, an initial config with startup logging, and placeholder future-system packages.
- Updated README documentation for Combatives scope, reference-only Aqua Acrobatics Legacy policy, non-compiled reference source, and planned future movement/camera work.

## Production-grade Gradle versioning

- Added `version.properties` with manually managed `mod_version` and automatically managed `build_number` values.
- Added Gradle version loading so the project exposes `modVersion`, `buildNumber`, and `fullVersion`, with JARs using `mod_version.build_number`.
- Added production-build build number incrementing with failed-build rollback while preserving `version.properties` comments and formatting.
