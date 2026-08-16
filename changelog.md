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
