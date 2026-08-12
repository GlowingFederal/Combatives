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
