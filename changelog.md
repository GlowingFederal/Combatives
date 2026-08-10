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
