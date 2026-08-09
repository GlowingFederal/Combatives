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
