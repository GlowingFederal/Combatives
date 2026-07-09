# Changelog

## Rework Combatives bob to vanilla-style camera and hand motion

- Replaced the custom procedural bob phase with a vanilla-style walk-distance, camera-yaw, and camera-pitch bob calculation with only subtle Combatives intensity scaling.
- Kept vanilla `setupViewBobbing` canceled while applying a recreated first-person hand/item bob directly before item rendering.
- Removed the failed render-hand bob passthrough so camera bob ownership no longer depends on vanilla bob rendering remaining active.

## Preserve vanilla first-person arm bob while owning camera bob

- Limited Combatives vanilla view-bob cancellation to the world camera path so the first-person arm/item render can still receive vanilla hand bobbing.
- Added render-hand state tracking around `EntityRenderer#renderHand` without changing movement, crawl, swim, hitboxes, or pose sync.

## Fix Combatives camera bob ownership

- Added explicit vanilla view bobbing cancellation whenever the Combatives camera and procedural bob are enabled, without changing the user's vanilla View Bobbing option.
- Kept Angelica bob ownership behind the existing optional compatibility flags while separating vanilla bob cancellation from Angelica ownership.
- Consolidated camera Angelica compatibility config generation under the compatibility category so there is only one camera Angelica enable option.

## Combatives first-person camera overhaul foundation

- Added the client-only Combatives camera controller stack for movement state sampling, subtle inertial lean, continuous procedural bob, movement FOV, and damped camera shake impulses.
- Routed EntityRenderer camera update, final transform application, vanilla bob ownership cancellation, and movement FOV composition through UniMixins without ASM or renderer replacement.
- Added camera config toggles and safe Angelica camera ownership compatibility checks with quiet default logging.
- Kept the camera foundation read-only with respect to movement, crawl/swim sync, hitboxes, weapons, recoil, and ADS behavior.

## Final movement diagnostics cleanup

- Added separate `debugMovement` and `verboseMovementDebug` movement diagnostic levels so normal crawling/swimming remains quiet by default while general lifecycle events and verbose per-frame traces can be enabled independently.
- Moved render/model hook traces, grounding dumps, DataWatcher pose messages, nameplate hook traces, and repeated size/eye recalculations behind verbose movement diagnostics.
- Renamed runtime movement mixin and render/model diagnostics to Combatives wording so logs do not imply Aqua code, classes, or dependencies are bundled at runtime.
- Documented the off/general/verbose movement diagnostic behavior and the fact that verbose movement diagnostics imply general movement diagnostics.

## Fix remote crawl nameplate hiding hook path

- Added player-specific `RenderPlayer#func_96449_a` cancellation with the 1.7.10 player label signature so remote crawling players cancel before the visible player nameplate is drawn.
- Kept shared `RendererLivingEntity` label cancellation as a fallback and moved the base `Render#func_147906_a` fallback to a `Render` mixin because that method is inherited from the base renderer.
- Added crawl nameplate debug logs for hook name, entity class, player name, crawl/swim/pose flags, distance, and cancellation attempts.

## Add shared land-crawl grounding correction

- Kept Aqua's single local/remote prone transform path and added a small internal land-crawl-only downward render correction after the Aqua rotation/translation calculation.
- Left water swimming translation unchanged and expanded crawl render diagnostics to include base, correction, and final Y translation values once per local/remote crawl render session.

## Restore Aqua crawl grounding diagnostics and nameplate cancel path

- Reverted the experimental local/remote crawl render offset split and restored Aqua's single `RenderPlayer` prone translation for crawl/swim models.
- Restored Aqua's remote-player `yOffset` relationship by keeping remote `EntityOtherPlayerMP` yOffset at `0.0F` after update instead of applying the local `0.28F` low-pose offset.
- Added crawl-only render grounding diagnostics for local/remote identity, vertical positions, interpolation, yOffset/ySize, size, pose flags, and render translation values.
- Moved crawl nameplate hiding to the shared `RendererLivingEntity#func_147906_a` label draw hook, which is the lowest label drawing path used by player renderers.

## Polish crawl render height, jump exit, and nametags

- Split land-crawl `RenderPlayer` translation for local and remote players so the local model is nudged down while remote crawlers are lifted out of the ground without touching water swimming translation.
- Changed crawl jump handling so local jump input cancels vanilla jumping, requests crawl exit only when standing clearance is available, and the server rejects blocked crawl exits authoritatively.
- Hid RenderPlayer nameplates while the synced crawl flag is active, matching sneak-style visibility without changing vanilla sneaking state.

## Fix Aqua-style crawl toggle request handling

- Changed the crawl key packet semantics from a pressed/released state to a toggle request while keeping the existing packet registration path intact.
- Kept release handling client-only for debounce reset, with no release packet and no crawl=false prediction on key release.
- Simplified server handling so each received crawl toggle flips server crawl state once, immediately selects SWIMMING only when toggled on, and otherwise lets `updatePose` resolve the exit pose.

## Restore sticky crawl/swim pose priority

- Reworked player pose priority so active valid swim or crawl state selects and holds the shared SWIMMING pose before sneak, standing, or clearance fallback logic can run.
- Prevented client pose prediction packets from applying STANDING/CROUCHING over an active server-side crawl/swim state, and made accepted crawl toggles select SWIMMING immediately before authoritative broadcast.
- Reduced noisy render diagnostics and kept targeted logs for SWIMMING selection or cancellation, including crawl/swim flags and exit validity.
- Increased standing and sprinting movement responsiveness again so normal walking feels closer to vanilla while leaving crawl/swim profiles unchanged.

## Fix visual crawl/swim pose application

- Routed first-person camera eye-height interpolation through the Combatives pose eye height so SWIMMING/crawl renders with a low camera instead of vanilla standing height.
- Preserved the shared SWIMMING pose for swimming and crawling while applying low third-person/remote-player y-offset and keeping model/render pose hooks on Combatives pose state.
- Marked client authoritative pose applications as render-dirty after DataWatcher pose updates and tuned standing/sprinting movement profiles closer to vanilla with only subtle inertia.

## Fix Combatives pose-state interface injection on the server

- Added a Combatives `IFMLLoadingPlugin`/`IEarlyMixinLoader` so common player pose mixins are queued by GTNHMixins on both physical client and dedicated server.
- Split common pose/player state mixins from client-only rendering, camera, and input mixins so server `EntityPlayerMP` receives the Combatives pose-state interface without loading client mixins.
- Added startup and login/runtime diagnostics for common mixin config loading and player pose-state interface availability.

## Fix Combatives crawl/swimming networking

- Moved Combatives SimpleNetworkWrapper creation and packet registration into common preInit so both dedicated/integrated servers initialize the channel before client-only setup.
- Replaced incremental packet discriminator assignment with explicit stable discriminator constants, keeping the crawl key packet registered uniquely on SERVER.
- Added crawl packet debug diagnostics for channel setup, client sends, server receipt, player resolution, and server pose state before/after handling.

## Fix momentum hook crawl and water ownership

- Restricted the generic `moveFlying` momentum hook to normal walking, sprinting, sneaking, and airborne movement so custom crawl, swim, water, noClip, riding, sleeping, death, ladder, and creative-flying paths bypass it.
- Moved crawl shaping into a dedicated crawl movement branch and kept swim/water shaping inside the existing custom swim/water branch to prevent double-processing.
- Preserved dedicated crawl/swim `MovementSnapshot` emission without changing existing vertical swim behavior, water drag, collision, limb swing, or pose ownership.

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

## Restore Aqua authoritative crawl and swim state machine

- Replaced the local pose-selection rewrite with Aqua-style priority ordering so active swimming or crawl forcing selects the shared low SWIMMING pose before sneaking or standing clearance can run.
- Removed client pose rebroadcast authority from the player tick path and made server-side handling of client pose packets preserve active server crawl/swim state instead of accepting STANDING or CROUCHING downgrades.
- Restored Aqua low-pose render offsets and swim/crawl eye-height sizing behavior while keeping crawl toggle packet semantics server-authoritative.

## Restore Aqua visual crawl and swim model transforms

- Added a client visual-pose helper so local, remote, and armor-model rendering can identify crawl/swim from the synced crawl flag, swim flag, or shared SWIMMING pose.
- Routed RenderPlayer and ModelBiped through the helper, applying Aqua's prone/swim model rotation, translation, limb rotations, and temporary render/model debug logs whenever an active low pose is rendered.
- Reverted the previous eye-height adjustment and kept this change focused on player model transforms rather than camera behavior.

## Restore Aqua client render mixin loading

- Removed the non-reference visual helper path and restored RenderPlayer/ModelBiped to the direct Aqua swim-animation transform flow.
- Fixed the early mixin loader so Combatives client render/model/input mixins are actually returned on the physical client side instead of sitting in an unqueued client config.
- Updated documentation to clarify that the core plugin side-gates both common and client Combatives crawl/swim mixins.
