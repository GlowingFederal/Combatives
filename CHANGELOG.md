(PR: Restore packet-owned held interactions)

- Confirmed that holding a mouse button can produce later vanilla controller
  calls and a second C07 or C08 packet after the first action changes the
  current client target; one ray trace still owns only one target.
- Removed all server redirects that replaced C07/C08 coordinates, faces, and
  hit values with a later independent server ray, keeping complete digging
  sequences and placement packets bound to their client-selected targets.
- Kept vanilla survival mining continuation, right-click delay, reach,
  protection, build-height, game-mode, harvest, air-use, vehicle targeting, and
  bounded post-dismount behavior unchanged.
- Added verbose phase/tick diagnostics at controller, packet send, and packet
  receive boundaries; the server ray is now read-only diagnostic data and is
  only calculated when verbose diagnostics are enabled.

(0bc1e19 Investigate MorePlayerModels+ compatibility for Combatives)

- Added a source-based MPM+ behavior map, conflict audit, geometry architecture,
  prioritized implementation plan, and multiplayer compatibility test matrix.
- Linked the audit from the project development documentation.

(10b6e7e Centralize player geometry and add MPM compatibility)

- Centralized authoritative player dimensions, pose clearance boxes, eye height,
  camera base origin, and targeting geometry.
- Added optional client-only MorePlayerModels+ targeting compatibility, fixing
  crawl/swim block and entity rays without changing MPM rendering.
- Removed mutable `yOffset` from Combatives gameplay geometry and corrected the
  resize cache so rejected physical resizes retain the actually applied state.
- Corrected the crouching eye from an accidentally activated Aqua transitional
  value to the normal sneaking eye height.
- MorePlayerModels+ model size and body-part scaling remain visual and do not
  alter Combatives gameplay hitboxes.

(f80c83f Fix MPM renderer compatibility loading)

- Moved the optional MorePlayerModels+ renderer integration from the early
  vanilla mixin phase to a client-only, mod-gated GTNHMixins late configuration.
- Corrected production remapping for the vanilla `EntityRenderer#getMouseOver`
  call while retaining the exact MPM+ 4.2 `getMouseOver(float)` target.
- Limited MPM targeting-offset suppression to Combatives crawl/swim geometry;
  unsupported MPM method revisions now retain native MPM targeting instead of
  making the optional injector mandatory.

(c6a0472 Fix MPM+ Targeting Origin — Current Compatibility Hook Does Not Apply)

- Corrected the optional MPM+ 4.2 pseudo-mixin to target the production SRG
  `func_78473_a(F)V` wrapper and its exact SRG vanilla super invocation.
- Scoped genuine-position restoration to the vanilla targeting call, then
  returned MPM's mutated samples so MPM remains the sole owner of its cleanup.
- Added throttled camera/target-origin, ray, reach, hit, and MPM mutation
  diagnostics for numerical manual verification.
- Documented the verified mapping failure, default-size targeting math,
  authoritative geometry invariant, and revised compatibility lifecycle.

(e351ec1 Restore legacy player ray-origin invariant)

- Restored Minecraft 1.7.10's pos-relative `getEyeHeight()` contract by converting
  Combatives' box-relative pose eye height at the vanilla API boundary.
- Kept the bounding-box floor stable during pose shrinking and removed the
  dedicated-server swimming override that repeated the modern coordinate error.
- Expanded targeting diagnostics and documented the vanilla coordinate trace,
  pose-transition verification matrix, and separate MPM compatibility boundary.

(ba5c4fc Align MPM targeting with rendered camera ownership)

- Traced vanilla, MPM+, and Combatives world-camera composition and identified
  MPM's paired `yOffset`/position-sample POV displacement.
- Made the MPM targeting boundary follow Combatives' existing all-pose physical
  camera ownership instead of retaining a standing-only position disagreement.
- Added paired camera/target position, direction, procedural transform, and FOV
  diagnostics plus a manual crosshair-alignment verification matrix.

(2e16cbc Preserve paired MPM standing camera and targeting ownership)

- Preserved vanilla/MPM legacy camera and targeting semantics for standing
  instead of forcing AABB-relative gameplay geometry into every camera.
- Narrowed Combatives camera and MPM targeting replacement to poses with
  Combatives-owned physical geometry (resized sneak, crawl, and swim).
- Added a focused, heavily throttled MPM POV diagnostic with model inputs,
  paired camera/target calculations, mutation samples, and ownership decisions.

(4cddc90 Trace vanilla-consumed camera and ray origins)

- Captured the exact target origin and look vectors returned to vanilla
  `getMouseOver`, correlated with the base `orientCamera` origin by frame ID.
- Corrected the standing legacy eye conversion so MPM's temporary targeting
  position no longer changes `getEyeHeight()` and cancels MPM's ray movement.
- Replaced inferred MPM camera/target comparisons with explicitly labeled raw
  mutation diagnostics and added a non-MPM vanilla control trace.

(e9733fa Correct 1.7.10 targeting diagnostic mappings)

- Corrected target-origin and look-vector redirects to the verified
  `EntityLivingBase` owner used by Minecraft 1.7.10.
- Added direct capture of the independent origin and look vectors consumed by
  `EntityLivingBase#rayTrace` for block targeting.
- Preserved frame-correlated entity-ray, block-ray, and base-camera diagnostics
  without changing the cached legacy-eye coordinate fix.

(701f9c0 Trace vanilla targeting interpolation offset)

- Identified the reported `-0.12` as the difference between MPM's current
  `posY` sample and Minecraft 1.7.10's `prevPosY`/`posY` partial-tick
  interpolation, common to both block and entity targeting origins.
- Added direct interpolation-term diagnostics at both vanilla targeting call
  sites without changing ray behavior, camera behavior, player geometry, or
  the cached legacy `getEyeHeight()` conversion.
- Corrected the targeting audit's source-level account of
  `EntityLivingBase#getPosition` and documented every Y operation through both
  ray paths.

(c6f294d Align targeting with rendered camera ray)

- Added a Combatives-owned authoritative first-person view ray captured from
  the pre-presentation `orientCamera` base and anchored to physical geometry.
- Routed vanilla block and entity ray inputs through that shared origin and
  direction without replacing reach, intercept, precedence, or result logic.
- Removed the MPM-specific position-mutation targeting hook and replaced its
  diagnostic spam with camera/ray delta and selected-intercept tracing.
- Documented the interpolation root cause, visual/gameplay transform boundary,
  vanilla fallbacks, and compatibility validation cases.

(a1e79e0 Add MorePlayerModels+ hitbox resizing compatibility)

- Derived physical scale from MPM+'s synchronized whole-model `size / 5`
  renderer transform and kept anisotropic body-part scales visual-only.
- Composed MPM scale with Combatives posture width, height, and box-relative eye
  geometry on both logical sides through an optional reflection boundary.
- Preserved the bounding-box floor, rejected obstructed expansion without
  moving player coordinates, and added verbose transition diagnostics.
- Added an independent default-enabled compatibility option and documented
  fallback behavior, synchronization, ownership, and manual validation cases.

(d3a8d96 Scale MPM disguise hitboxes and restore first-person items)

- Derived width, height, and eye proportions from MPM's synchronized vanilla or
  modded entity disguise and composed them with whole-model size and pose.
- Restored the vanilla first-person arm and held-item render pass for MPM entity
  disguises after tracing the absence to MPM's canceled `RenderHandEvent`.
- Documented compatibility behavior, graceful fallbacks, and ownership.

(cf147ac Align transformed camera and targeting geometry)

- Retained the complete accepted MPM disguise geometry instead of reconstructing
  height and eye position from the disguise's width ratio.
- Kept the rendered first-person camera, authoritative block ray, and entity
  targeting sweep aligned for transformed players with non-uniform proportions.
- Preserved the prior accepted camera and ray geometry while an enlarged
  disguise hitbox is obstructed and waiting for clearance.

(ae92111 Restore authoritative first-person targeting ray)

- Fixed the all-player crosshair regression caused by using an
  `orientCamera` sample from the previous render pass during the current
  pass's earlier `getMouseOver` call.
- Derived both the rendered base camera and current gameplay ray from the same
  interpolated AABB floor plus accepted eye offset for every player pose.
- Preserved vanilla block/entity selection and reach while keeping visual-only
  camera transforms out of gameplay aim.

(d258ccd Fix dedicated-server MPM targeting geometry)

- Made the dedicated server authoritative for MPM-derived player width, height,
  and eye scale, and synchronized that tuple to owning and tracking clients on
  login/construction, live model changes, and tracking transitions.
- Restored the vanilla 1.7.10 relationship between the resized AABB floor and
  all vertical position samples so movement packets and server corrections
  cannot reconstruct the player at a different vertical anchor.
- Added paired `CLIENT TARGET GEOMETRY` and `SERVER PLAYER GEOMETRY` diagnostics
  covering position history, AABB dimensions, eye geometry, MPM data, and pose.
- Documented the integrated-versus-dedicated ownership error, synchronization
  lifecycle, remote-player behavior, and movement-anchor fix.

(1a4fde4 Establish authoritative interaction geometry ray)

- Added one common-side interaction-ray API derived exclusively from accepted
  AABB-floor-relative gameplay geometry and player orientation, with a clearly
  separated interpolated rendering variant.
- Versioned server-owned geometry transitions and synchronized revisions with
  MPM scale tuples so dedicated client/server logs can identify stale state.
- Made server start-digging and block-use handling independently ray trace the
  authoritative geometry instead of treating client-selected coordinates as
  authority, while retaining vanilla reach and interaction validation.
- Removed common-side linkage to client targeting classes and documented the
  verified 1.7.10 packet pipeline, obsolete calculations, synchronization
  boundary, and diagnostic comparison procedure.

(1f499be Rewrite README as player project page)

- Reorganized the landing page around player-facing movement, camera,
  compatibility, configuration, and installation information.
- Added a concise contributor path for the legacy ForgeGradle workspace and a
  public Camera API overview with links to the focused technical documentation.
- Replaced obsolete implementation-status prose with current source-available
  core, Apache-2.0 Camera API, dependency, compatibility, and attribution
  guidance verified against the repository.

(4f6143c Improve Combatives player crawling animation and pose)

- Added a rendering-only land-crawl distinction while retaining the shared
  swimming pose for authoritative mechanics and forced low-clearance crawling.
- Replaced the land swimming stroke with a restrained, movement-scaled diagonal
  crawl cycle and preserved the existing animation for actual swimming.
- Preserved crawl look direction, grounding ownership, camera, targeting,
  collision, movement, and third-party renderer compatibility boundaries.

(6482e8e Investigate DataWatcher pose collision)

- Documented the root cause and complete lifecycle of the colliding player pose
  watcher without changing runtime behavior.
- Audited every Combatives DataWatcher allocation and evaluated fixed,
  configurable, dynamic, EndlessIDs-assisted, and watcher-free alternatives.
- Recommended moving pose into existing Combatives-owned state and packet
  synchronization, with a narrow implementation and compatibility test plan.

(f46e0f1 Remove pose DataWatcher allocation)

- Removed Combatives' fixed EntityPlayer pose DataWatcher slot and metadata callback so other mods may own watcher ID 28 without collision.
- Made the owned per-player pose field the sole local pose store while retaining null-safe standing defaults and explicit geometry recalculation.
- Preserved the existing pose packet broadcasts, owner/tracker lifecycle synchronization, and independent MPM geometry composition.

(a1ec44b Lower and level the crawl pose)

- Moved the four-pixel land-crawl grounding correction ahead of the prone
  rotation so OpenGL transform composition lowers the rendered player in world
  space instead of shifting it along the rotated model's local depth axis.
- Leveled the legs with the torso at rest while retaining the alternating,
  movement-scaled leg drive and opposing spread during crawling.
- Documented the grounding transform order and revised resting leg silhouette.

(3994975 Correct crawl head pitch above terrain)

- Counter-rotated the head and headwear against the crawl body's blended prone
  pitch so a straight-ahead gaze remains forward and above level terrain.
- Kept the existing world-space torso grounding, limb animation, collision,
  swimming, camera, targeting, synchronization, and MPM geometry behavior.
- Documented why the head clipped independently of the correctly grounded body
  and how the render-only correction preserves look pitch through transitions.

(507e052 Restore pose state across sleep and crawl rendering)

- Committed standing physical/effective geometry before vanilla wake placement
  and resampled the final legacy eye anchor afterward, preventing bed-sized
  pose state from surviving wake-up in collision, camera, and targeting paths.
- Scoped crawl-authored leg rotations to the current render by restoring the
  vanilla animation values captured for that frame instead of zeroing axes or
  relying on a later animation path to overwrite them.
- Documented the vanilla/Combatives sleep lifecycle, transition invariants, and
  additive An Extra Touch camera pipeline; no compatibility code is required.

(ee707c3 Preserve mounted camera ownership)

- Preserved the camera offset already selected by vanilla or a vehicle mod while
  the view player is riding, rather than replacing a seat position with standing
  Combatives geometry.
- Returned mounted block/entity targeting to the native vehicle-compatible path
  while retaining Combatives' authoritative pose ray when independently moving.
- Added mount-transition camera diagnostics and documented the generic ownership
  boundary, dismount behavior, compatibility risks, and in-game regression matrix.

(4e28df8 Preserve MCHeli synthetic camera ownership)

- Preserved MCHeli's `MCH_ViewEntityDummy` camera origin for gunner,
  always-camera, and camera-id vehicle views without linking against MCHeli.
- Kept Combatives procedural effects additive after the MCHeli base transform
  while returning synthetic-camera targeting to MCHeli's native path.
- Documented the real-player aircraft path, synthetic tank/ground-vehicle path,
  confirmed ownership failure, transform order, and expanded manual test matrix.

2026-08-31 02:19 — Reset crawl geometry across death and respawn

- Removed the pose packet's post-resize `yOffset` mutation, which broke the
  legacy `posY`/bounding-box anchor and was reapplied as a downward displacement
  during a later crawl resize.
- Made player death clear crawl, swim, movement, animation, and geometry caches
  on both logical sides, and made server respawn plus the matching client pose
  transition forcibly restore canonical standing geometry and legacy anchors.
- Kept the client remote-player zero-offset convention while making its one-time
  normalization preserve the AABB floor and interpolation samples.
- Left ordinary dimension changes, teleports, crawl clearance, and the existing
  sleep/wake lifecycle unchanged. Dedicated-client and repeated respawn runtime
  validation remains required because project execution was intentionally not
  performed in this environment.

2026-09-01 00:00 — Add authoritative tactical lean and sprint sliding

- Added explicit synchronized crawl/swim/slide locomotion state, server-owned
  slide entry, inherited momentum, tick deceleration, deterministic termination,
  and reuse of the existing low geometry and standing-clearance paths.
- Added held lean controls, server validation, wall-clamped shared camera and
  interaction-ray displacement, smooth camera roll, and synchronized remote
  torso/head presentation without moving the player collision box.
- Added a distinct asymmetric slide animation, restrained slide camera settling,
  movement configuration, and lifecycle clearing for death, respawn, teleport,
  dimension change, mounts, water, flight, ladders, falling, impacts, and damage.
- Source paths and packet payloads were inspected without compiling or running
  the project; dedicated-server latency, visual tuning, slopes, and compatibility
  with installed mod combinations still require in-game validation.

2026-09-01 21:30 — Correct lean, crawl toggle, and slide entry regressions

- Standardized lean on negative-left/positive-right semantics, corrected the
  shared camera/ray basis and roll direction, and changed defaults from conflicting
  Q/E to configurable Z/X controls.
- Made upper-body lean a once-per-render additive transform over the current body,
  head, and arm animation, restoring base angles afterward to prevent accumulation.
- Restored authoritative crawl-to-stand toggling with standing-clearance enforcement
  and carried sprint state captured at the crawl press edge into server slide
  eligibility so vanilla sprint cancellation cannot erase a valid entry attempt.
- Added one diagnostic record per slide attempt with predicate values and rejection
  reason. Changes were source-inspected only; runtime validation remains required.

2026-09-01 22:15 — Anchor low-profile geometry to collision state

- Made pose geometry application idempotent: unchanged crawl and slide geometry no
  longer reconstructs the player AABB or rewrites vertical interpolation samples on
  every pose-selection tick; real pose/scale changes still preserve the AABB floor
  and the legacy `posY - minY == yOffset - ySize` entity anchor.
- Added movement-debug transition records for locomotion, pose, ground state, and
  one-shot anchor disagreements, including the complete vertical/collision state
  needed to distinguish entity, AABB, and stale-ground failures.
- Kept slide travel in the existing single collision-aware movement pass; no render
  offset or forced ground state was added. Source inspection only was performed, so
  flat ground, edges, steps, lifecycle transitions, and multiplayer correction still
  require in-game validation.

2026-09-01 23:10 — Separate movement correction from teleport lifecycle

- Stopped classifying every server `setPlayerLocation` call as a teleport pose
  lifecycle event; vanilla movement-packet correction can use that low-level path
  and previously cleared server crawl/slide state without reconciling the owner.
- Moved teleport reset to explicit `EntityPlayerMP.setPositionAndUpdate`
  relocations and broadcast the resulting authoritative standing state to the
  owning client as well as trackers.
- Added verbose, threshold-gated C03 before/packet/after diagnostics and falling
  client tick/packet diagnostics to identify whether a stale client AABB floor is
  the writer restoring server Y. Source inspection confirms the candidate call
  path, but the exact runtime writer and old/new values still require a new log;
  no build or in-game validation was performed.

2026-09-01 23:45 — Make sprint-to-crawl slides forgiving and brace lean poses

- Added a restrained, mirrored leg counterbalance on top of each frame's animated
  leg pose, with the same render-once restoration lifecycle as the upper-body lean.
- Carried forward-input state with the sprint snapshot on the crawl press edge,
  lowered the default entry-speed safety floor to crawl-exit speed, and made slide
  attempt, start, and end diagnostics concise event records.
- Removed per-frame crawl model/render hook messages. Source inspection and packet,
  state, and movement call-path tracing were performed; build and in-game visual,
  timing, collision, and dedicated-server validation remain required.

2026-09-02 00:00 — Strengthen leg lean and synchronize gameplay configuration

- Changed both animated legs from a slight opposite brace to 75% of the torso's
  lateral roll, retained only a small asymmetric stance, and added/restored a
  subtle lateral leg-pivot shift so body and armor models form a coherent lean
  without replacing vanilla walk-axis animation.
- Added a versioned server-to-client gameplay-config snapshot at login and a
  small runtime access boundary. Lean enablement, physical lean distance, and
  optional MPM hitbox scaling now come from server configuration in gameplay
  paths, while cosmetic roll/interpolation and other camera preferences remain
  local. The local configuration file is never overwritten; runtime config
  reload is not supported, so reconnecting refreshes the snapshot.
- Removed slide entry from crawl requests and disabled sliding by default, so
  sprint plus crawl behaves as ordinary crawl. Gated continuous targeting and
  rendered-camera diagnostics behind verbose flags.
- Changes were validated through source inspection, call-site searches, and
  packet/config tracing only. Compilation and Minecraft runtime validation were
  intentionally not performed; pose appearance, armor alignment, wall clamps,
  reconnects, and dedicated/integrated multiplayer behavior still require
  in-game testing.

2026-09-02 00:46 — Correct cardinal lean geometry and Flan's armour pose lifecycle

- Centralized Minecraft-yaw forward/right basis conversion so authoritative wall clamps, interaction rays, interpolated client rays, and camera projection use the same player-relative convention (`negative = left`, `positive = right`) at every heading.
- Kept server interaction geometry on current authoritative yaw while making interpolated client geometry consistently use interpolated yaw and position.
- Extracted the shared visual lean pose and added an optional generic `ModelCustomArmour` render-lifecycle adapter. Flan's independently rendered TurboModel part arrays now consume the biped parent pose and restore reusable model state after rendering without item-specific checks or accumulated transforms.
- Source-level call-site and cardinal-vector validation was performed; dedicated-server and Tyrants and Plebians in-game validation remains required.

2026-09-09 02:59 — Resolve ADS arms after crawl and swim animation

- Apply movement animation before ModelBiped's existing aimedBow block, then apply visual lean. HMG's bow-style ADS arms now survive crawl/swim animation and Flan's armour copies the resolved biped pose without duplicate weapon angles.
- Restore captured lean followed by crawl legs at the existing renderer cleanup boundary and before the next angle calculation, including Flan's render override; retain the pose through held-item rendering.
- Java 8 Gradle compileJava succeeded; generated refmap maps the aimedBow injection target to field_78118_o. No reobfuscation, packaging or in-game validation was performed. Standing/leaning, crawl transitions, optional-mod absence and integrated/dedicated testing remain required; a separate standing-only armour mismatch was not established by source inspection.

2026-09-09 03:07 — Align custom armour crawl and swim draw transforms

- Correct Flan's Turbo Y-Z-X draw order to the resolved biped Z-Y-X order around the copied pivot during crawl/swim/lean. The optional armour adapter scopes the correction to its own parts and skirts, preserves copied fields and balances the added matrix in finally; ordinary unposed rendering is unchanged.
- Move existing head-pitch preparation from ModelBiped.render into inherited setRotationAngles so Flan's override receives it too, including the head pose used by ADS. Preserve movement-before-ADS and lean-after-ADS ordering.
- Confirmed the previous lifecycle change already fixes the skipped crawl-leg cleanup and stale roll capture; all Turbo limb/skirt fields are overwritten before each draw. No redundant leg reset or Flan's-specific pose angles were added.
- Java 8 compileJava and Mixin annotation processing passed. Inspected reference source and supplied Turbo bytecode, checked matrix composition and repeated-call state transitions. Reobfuscation, runtime injection matching and in-game crawl/swim/standing transitions on integrated/dedicated servers remain untested.

2026-09-09 07:24 — Correct first-person tactical lean roll direction

- Corrected the first-person view-matrix roll so negative/left and positive/right
  semantic lean now produce the same visible side as the established player model.
  The already-correct inverse world translation, shared player-local basis,
  wall-clamped interaction-ray origin, packet sign, and model pose were preserved.
- Documented why camera translation, camera roll, and model roll require different
  numeric signs in their respective coordinate spaces. Validation was limited to
  source tracing, call-site searches, and cardinal/diagonal vector calculations;
  in-game visual and integrated/dedicated multiplayer checks remain required.

2026-09-09 07:39 — Keep tactical lean on the vanilla render timeline

- Changed interpolated interaction geometry to use Minecraft's render position
  samples (`lastTickPos` to `pos`) rather than the independent `prevPos` samples.
- Kept the camera lean as a player-relative legal offset through collision
  validation and OpenGL translation instead of reconstructing that small value
  by subtracting two absolute world origins. Authoritative current-tick rays,
  lean direction, wall margins, server settings, models, and compatibility paths
  are unchanged.
- Validation was limited to source inspection, call-site tracing, diff checks,
  and render/authority invariant reasoning. Walking, sprinting, diagonal/yaw
  movement, block-boundary crossings, and wall transitions still require the
  requested integrated- and dedicated-server runtime checks.

2026-09-09 08:02 — Keep third-person lean in model-local space

- Removed the lean-only replacement of the living renderer's interpolated body
  and head yaw. The authored biped roll and pivot values now receive only
  vanilla's existing model-to-world body orientation instead of mixing entity
  facing into the renderer while the model is already locally posed.
- Preserved the complete authored body/head/arm/leg pose, accepted lean amount,
  animation and armour cleanup, and all camera, interaction, collision,
  networking, and authority paths.
- Source inspection confirms that all eight headings now feed identical scalar
  pose values into `ModelBiped`; visual appearance, body/head turning during a
  lean, and optional armour rendering still require in-game verification.

2026-09-09 03:51 — Connect third-person lean at the pelvis

- Rotate animated head, shoulder and hip attachments with the torso around the pelvis instead of leaving leg roots behind the torso's rolled lower edge. Add restrained opposite-leg bracing, near-leg follow and slight hip overlap; preserve underlying walk and ADS pitch/yaw.
- Keep one shared LeanVisualPose, mirrored left/right formulas, and capture/restore every changed X/Y pivot at existing cleanup boundaries. Flan's armour consumes the same biped parts; gameplay snapshot values, lean authority, camera, networking and crawl/swim ordering remain unchanged.
- Java 8 offline compileJava and Mixin annotation processing passed. Source/diff and symmetry checks performed; no runtime launch, packaging or in-game claims. Standing, walking, sprinting, turning, release/toggling, HMG ADS and Flan's armour need visual testing on both sides.


2026-09-10 03:02 — Share jump rejection with optional HMG weight policy

- Added an initialization-time common predicate registry and resolved optional blockers in the existing jump HEAD hook. HMG can register through reflection without a Combatives dependency on HMG classes; successful registration makes Combatives own HMG jump rejection.
- Preserved crawl-key release/standing clearance even when weight also rejects the jump, and retained vanilla velocity and existing horizontal/pose/network behavior. Added tick, logical-side, airborne and flight fields to existing verbose vertical diagnostics.
- Documented API ownership and HMG fallback/synchronization requirements. Java 8 offline compileJava and Mixin annotation processing passed. No packaging, reobfuscation, launch or in-game checks; combined heavy/crawl, creative flight, switching, transformer order and dedicated/integrated behavior require runtime validation.

2026-09-13 18:47 — Preserve mount dismount ownership and resynchronize movement state

- Prevented expected rider/mount overlap from becoming Combatives' forced-crouch `isSneaking()`
  result. While riding, only the real movement input is exposed, allowing MC Heli to suppress it
  during its native hold-to-dismount window while vanilla mounts retain normal Sneak dismounting.
- Made the existing generic riding transition clear crawl, swim, slide, lean, movement snapshot,
  and pose-animation state on both sides. Dismount now rebases interpolation and camera history to
  the player coordinates already selected by the mount without calling a positioning method,
  changing the AABB, checking a mod class, or implementing a dismount timer.
- Updated mounted-ownership documentation and the manual regression matrix. Java 8 `compileJava`
  and Mixin annotation processing passed; repeated MC Heli/vanilla mount behavior and
  integrated/dedicated in-game synchronization still require runtime validation.

2026-09-13 23:38 — Follow mount-owned position writes through dismount completion

- Confirmed MC Heli may apply its configured custom exit with `setPosition` after detaching and
  repeat that authoritative placement for five vehicle ticks. The earlier one-shot dismount reset
  could therefore leave Combatives history behind a later MC Heli-owned write, exposed by jumping
  or entering flight.
- Added a generic post-dismount observer for player `setPosition` calls. While position writes keep
  arriving, Combatives rebases entity interpolation/walk history, clears its derived movement
  snapshot, and revision-resets the client camera motion sampler to the position already accepted
  by the entity; two quiet player ticks close the observer. No mount class, exit coordinates, key
  timing, position, AABB, velocity, or ground state is overridden.
- Audited current HMG jump handling: it only rejects at jump entry and observes landing cooldowns,
  and explicitly bypasses riding and flight without position, velocity, or ground-state writes.
  Java 8 `compileJava` and Mixin annotation processing passed. In-game MC Heli/HMG/vanilla-mount
  validation remains to be performed.
2026-09-14 04:17 — Give the early loader exclusive interaction-hook ownership

- Removed the duplicate static common/client mixin declarations from the legacy JSON configs. The
  GTNHMixins `IEarlyMixinLoader` list is now the only registration source for vanilla client and
  server hooks, preventing a second bootstrap path from applying the same player-controller,
  targeting, movement, and packet handlers to one physical input.
- Preserved the exact dynamically selected mixin set, side rules, vanilla hold behavior, targeting
  corrections, crawl, lean, and optional-mod compatibility. Validation was limited to JSON parsing,
  registration-source searches, and call-site tracing; single-click and hold behavior still require
  the requested integrated/dedicated in-game matrix.

2026-09-20 — Fix MC Heli dismount collision, targeting, and falling handoff regressions

- Added a cached, optional MC Heli collision boundary which distinguishes composite damage/ray
  boxes from physical movement collision. Aircraft composite boxes no longer reject pose
  clearance; real blocks, unrelated entity collision, and ship deck collision remain solid.
- Exposed a transition-based common mount handoff state. Mounted and exit-position phases retain
  vehicle camera/packet ownership, while Combatives targeting resumes only after the exit writes
  become quiet. Both position observation and clearance fallback are bounded to six player ticks.
- Applied the ownership decision to both EntityLivingBase ray redirects and server C07/C08 target
  selection. Start-dig and block-use retain their packet target during the unstable exit phase;
  stop/abort digging and the C08 air-use sentinel remain unchanged.
- Kept MC Heli's final exit placement authoritative and retained the 1.7.10 packet/AABB coordinate
  contract. Once the bounded handoff closes, ordinary falling no longer rebases position history
  in response to later corrections or participates in mount handoff logic.
- Reconciled pre-existing changelog conflict markers while moving the project history to the
  requested `CHANGELOG.md` filename.
