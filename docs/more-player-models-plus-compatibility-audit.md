# MorePlayerModels+ compatibility audit

## Scope and confidence

This is a source audit of the MorePlayerModels+ (MPM+) snapshot under
`referenceSRC/MorePlayerModels-Plus-SRC` and the current Combatives sources. It
does not implement compatibility code. “Confirmed” means the two source paths
provably write or derive the same runtime state incompatibly; “likely” means a
runtime/load-order or visual outcome still needs an instrumented client or
dedicated-server test.

The most important finding is that this MPM+ snapshot does **not** implement
gameplay player scaling. Its `size` and per-part `scaleX/Y/Z` values are model
and point-of-view inputs. It never calls `Entity#setSize`, writes player
`width`, `height`, `boundingBox`, `stepHeight`, capabilities, attributes, or
motion. The only gameplay-adjacent geometry modification is client-side and
temporary: `EntityRendererAlt#getMouseOver` moves the three player Y position
samples around vanilla targeting. Treating MPM+ model scale as an already
server-authoritative collision scale would therefore invent behavior MPM+ does
not currently have.

## MPM+ behavior map

### Data and synchronization

| Path | Relevant members | Effect and side |
|---|---|---|
| `noppes.mpm.ModelData` | `size` (default 5, valid 1–10), `animation`, `modelType`, `getNBT`/`setNBT`, `getData` | Extended player data on both sides. NBT synchronizes model size, animation, model choice, part configuration and cosmetics; it does not apply entity dimensions. |
| `noppes.mpm.ModelDataShared` | `head/body/arms/legs`, `offsetY`, `getBodyY`, `getLegsY`, optional `entityClass` | Derives visual/model origin offsets. For a normal biped `offsetY()` is `-getBodyY()`; for an entity disguise it is the cached render entity's height minus 1.8. These values are not a player bounding-box API. |
| `noppes.mpm.ModelPartConfig` | independent `scaleX`, `scaleY`, `scaleZ`, clamped 0.5–1.5 | Per-body-part anisotropic **render** scaling. X/Y/Z do not describe one coherent player width/height and cannot safely drive collision. |
| `GuiCreationScale`, `CommandScale` | part and axis controls | Edit the above visual part scales. Commands broadcast the resulting complete NBT. |
| `CommandSize` | `ModelData.size` | Edits whole-model render/POV size (1–10); broadcasts complete NBT. |
| `PacketHandlerServer` | `UPDATE_PLAYER_DATA`, `ANIMATION` | Server accepts model NBT, persists it, and broadcasts it. It never resizes the server player. |
| `PacketHandlerClient` | `LOGIN`, `SEND_PLAYER_DATA`, `PLAY_ANIMATION` | Applies synchronized NBT/animation to client `ModelData`. The server ping distinguishes server-backed data from client-only fallback. |
| `ServerTickHandler` | login/logout/load/save and `checkAnimation` | Sends initial data and cancels emotes on movement, jumping, riding, sleeping, or sneaking. It samples motion but does not modify movement. |

MPM+ has separate width/height/depth controls only for each rendered body part.
It has no independently supported gameplay width/height. Whole `size` is a
uniform render and POV factor `size / 5`; part scale is not included in that
whole-model POV factor except through `offsetY`, `getBodyY`, and `getLegsY`.

### Camera and targeting

`ClientEventHandler#onRenderTick` replaces `Minecraft.entityRenderer` with one
long-lived `EntityRendererAlt` whenever “Enable POV” is true, retaining the
previous renderer for restoration. This is replacement, not an event or API.

`EntityRendererAlt#updateCameraAndRender` temporarily rewrites local-player
`yOffset` before calling `super`:

* normal model: subtracts `data.offsetY() + (-1.615 + size * 0.315)`;
* MPM sitting: adds a legs-derived term;
* MPM sleeping/crawling: assigns a fixed `2.8` (minus vanilla sneak adjustment);
* obstruction fallback: clamps values below 1.4 when the block at
  `floor(posY)+1` is not air;
* after rendering: unconditionally assigns `yOffset = 1.62`.

`EntityRendererAlt#getMouseOver` separately derives an MPM offset, temporarily
changes `posY`, `prevPosY`, and `lastTickPosY`, invokes vanilla
`EntityRenderer#getMouseOver`, then restores the samples:

* normal model offset is `-offsetY - (-1.615 + size * 0.315)`;
* sitting adds the legs-derived term;
* MPM sleeping/crawling assigns `1.18`;
* obstruction clamps offsets below -0.2.

That superclass method owns both the block ray and entity sweep and writes
`Minecraft.objectMouseOver` and `pointedEntity`. MPM+ does not modify
`EntityPlayer#getEyeHeight`, `Entity#getPositionEyes`, `Entity#getLook`, reach
attributes, `PlayerControllerMP`, or a ray-trace method. It changes their
effective origin indirectly by moving all interpolated Y samples for the
duration of `getMouseOver`.

### Rendering and animations

* `ClientProxy#registerRenderers` registers `RenderMPM` for players and
  registers `RenderEvent`.
* `RenderEvent#pre` runs at `LOWEST`, installs MPM models into the active player
  renderer, replaces render-manager mappings, selects disguise models/textures,
  and resets the local player's `yOffset` to 1.62. `special` suppresses Forge's
  held-item and helmet passes and renders both through MPM. `hand` cancels the
  hand for disguises and selected emotes. Optional name hiding temporarily
  abuses `riddenByEntity`.
* `RenderMPM#preRenderCallback` uniformly scales the entire rendered player by
  `size / 5`. Its model methods add per-part scaling, body/leg origin offsets,
  custom armor, held-item, helmet, cape, and back-item transforms.
* `RenderMPM#shouldRotateCorpse` supplies its own sitting, sleeping, crawling,
  and riding transforms. `AniCrawling` and `ModelMPM#setRotationAngles` supply
  MPM animation rotations after/coupled to vanilla model angles.
* Chat/name content is rendered at `y + 0.7 + player.height`; because MPM does
  not resize `player.height`, this anchor does not follow large/small visual
  models. MPM's optional player-name suppression is independent of Combatives'
  prone nameplate cancellation.

All GL transforms, part scales, animation rotations, arm/item work, disguise
rendering, and name positioning are client-only/purely visual. Model NBT and
animation are synchronized, but their render consequences remain client-only.

### Movement, collision, and mounts

The audited MPM+ source has no player hooks into `moveEntity`, `moveFlying`,
jump, gravity, acceleration, sprinting, swimming, sneaking speed, step height,
attributes, capabilities, suffocation, push-out, or collision resolution. Its
crawl/sit/sleep states are synchronized emotes, not physical poses. Riding only
cancels an MPM emote in `ServerTickHandler` and applies a visual legs translation
in `RenderMPM`. Consequently, MPM size currently has no server-side collision,
clearance, or safe-dismount meaning.

## Targeting trace and confirmed crawl bug

### Four paths

1. **Vanilla:** `EntityRenderer#getMouseOver` obtains the render-view entity's
   interpolated eye position (ultimately position samples plus
   `getEyeHeight()`), extends it by `getLook(partialTicks)` to controller reach,
   block-ray-traces it, then sweeps the same segment for entities and assigns
   `Minecraft.objectMouseOver`. `orientCamera` separately derives its base Y
   from interpolated position and `yOffset - 1.62`; it does not directly consume
   `getEyeHeight()` in 1.7.10.
2. **Combatives alone:** `EntityPlayerMixin#getEyeHeight` returns the cached
   active-pose eye height (`0.28` for `Pose.SWIMMING`), so vanilla block/entity
   targeting follows the low pose. `EntityRendererMixin#orientCamera` replaces
   the camera-local value every frame so the physical crawl camera is
   `boundingBox.minY + 0.28`, then applies procedural camera transforms. Thus
   camera and targeting share the pose, although intentional camera-effect GL
   translations do not move the gameplay ray.
3. **MPM+ alone:** `EntityRendererAlt#updateCameraAndRender` makes the visible
   camera use MPM model size/origin. `EntityRendererAlt#getMouseOver` moves the
   three interpolated Y samples by MPM's separately calculated offset while
   vanilla computes both block and entity targets, then restores them. Default
   size/parts are near vanilla; non-default models target from an MPM-adjusted
   POV without changing the server hitbox.
4. **Both:** Combatives still supplies the pose eye height and camera mixin to
   the superclass, but MPM's overriding `getMouseOver` wraps the complete
   targeting pass in an additional position mutation computed solely from MPM
   model data/MPM emote state. MPM's render tick and render-pre handler also
   rewrite `yOffset` independently of Combatives pose state. There is no single
   effective-eye owner.

### Root cause

The compatibility defect is **not** a bad Combatives crawl amplitude. MPM+
replaces the renderer and establishes two parallel POV algorithms: a `yOffset`
camera algorithm and a `posY/prevPosY/lastTickPosY` targeting algorithm. Neither
knows Combatives pose state. Combatives establishes a third algorithm in
`orientCamera` and `getEyeHeight`. Therefore the rendered camera and interaction
ray are calculated from different mutable inputs and update schedules.

The direct interception responsible for the interaction discrepancy is
`EntityRendererAlt#getMouseOver`, specifically its temporary position-sample
mutation around `super.getMouseOver`; both block and entity targeting are
affected. `getLook` and reach are not replaced. MPM's unconditional `yOffset`
reset compounds the camera ownership conflict. A fix must make both camera and
targeting consume one effective geometry result and bypass/neutralize MPM's
independent wrapper for a Combatives pose—not add a compensating constant.

One nuance should guide runtime diagnostics: with the exact current source,
default MPM size/part values make the normal MPM offset close to vanilla, while
Combatives' injected `getEyeHeight()` remains 0.28. If a run shows a *precisely*
standing-height ray, log the actual loaded `EntityRenderer` class, MPM data,
pose, eye height, and pre/post position samples; that exact magnitude can also
indicate a different built MPM+ revision or a failed mixin. The source-proven
conflict and correct repair point remain the overriding MPM wrapper, not an
assumed 1.34-block compensation.

## Effective geometry by state

In the present code, MPM has no effect on physical values. On client and server,
Combatives selects fixed vanilla-derived sizes:

| State | Physical width × height | Eye height / camera base | MPM effect |
|---|---:|---:|---|
| Standing, jumping, falling, landing | 0.6 × 1.8 | standing cache, normally 1.62 | model uniformly scales by `size/5`; MPM POV wrapper offsets camera/ray client-side |
| Sneaking / `CROUCHING` | 0.6 × 1.5 | 0.35 in current Combatives | same visual/POV behavior; no MPM sneak dimensions |
| Crawling | 0.6 × 0.6 | 0.28; camera base `box.minY+0.28` | MPM does not recognize Combatives crawl; its own crawl emote is separate |
| Swimming | 0.6 × 0.6 | 0.28; same low base | MPM has no swimming state or geometry |
| Mounted/riding | restored standing 0.6 × 1.8 | standing | MPM visually offsets legs and cancels emotes |
| Sleeping/dying | 0.2 × 0.2 | 0.2 | MPM sleep emote/camera can independently override client POV |

Jump/fall/landing do not select distinct Combatives sizes, so the active standing
or low-pose geometry persists. Combatives' `recalculateSize` directly writes
`width`, `height`, and the bounding box. It deliberately refuses resizing when
current fields/box do not resemble the last Combatives pose dimensions. That
guard protects another mod's geometry, but it is not compositional: if a future
MPM build (or another mod) applies real scale, Combatives may leave the old box
unchanged while still replacing its internal `combativesSize` and eye height.

Update ordering is split. Pose changes and pose packets call
`recalculateSize`; player post-tick updates pose again. MPM's server tick does no
resize. Client render ticks install `EntityRendererAlt`, its render call changes
`yOffset`, and MPM render-pre resets `yOffset`. Thus physical boxes currently
cannot be overwritten by MPM, but camera inputs can be overwritten every frame.

## Confirmed conflicts

### C1 — split camera and targeting origin (critical; crawl/swim; client)

* **MPM+:** `client/EntityRendererAlt#updateCameraAndRender`, `#getMouseOver`;
  `client/ClientEventHandler#onRenderTick`.
* **Combatives:** `mixin/EntityRendererMixin#orientCamera` injections;
  `mixin/EntityPlayerMixin#getEyeHeight`, `setPose`.
* **Cause:** renderer replacement plus independent temporary mutations of
  `yOffset` and interpolated position samples. MPM cannot see Combatives pose.
  Camera, block ray and entity sweep have multiple owners.

### C2 — local `yOffset` is last-writer-wins (high; all poses; client)

* **MPM+:** `EntityRendererAlt` mutates/restores it; `RenderEvent#pre` assigns
  1.62.
* **Combatives:** `EntityPlayerMixin#setPose` assigns 0.28 for its low pose and
  1.62 otherwise.
* **Cause:** MPM erases the pose value during rendering regardless of event/mixin
  order. This can make any vanilla or third-party camera/render code observing
  `yOffset` see a standing value while the Combatives box is prone.

### C3 — render pose algorithms compete (high cosmetic; crawl/swim; client)

* **MPM+:** `RenderMPM#shouldRotateCorpse`, `ModelMPM` animation dispatch,
  `AniCrawling`.
* **Combatives:** `RenderPlayerMixin#rotateCorpse`, `ModelBipedMixin` swim/crawl
  angles.
* **Cause:** MPM replaces the renderer/model and applies its animation transforms
  while Combatives targets vanilla `RenderPlayer`/`ModelBiped`. Inheritance lets
  some injections run, but transform order can rotate/translate twice; custom
  disguise models may not implement `ICombativesModelBipedSwimming` at all.

### C4 — MPM visual size disagrees with gameplay geometry (high; non-default size; both perceived, server authoritative)

* **MPM+:** `RenderMPM#preRenderCallback` scales only GL; part renderers add
  anisotropic visual scale; networking sends only model data.
* **Combatives:** fixed `SIZE_BY_POSE`, fixed clearance boxes, pose eye heights.
* **Cause:** a size 1 or size 10 character remains a 0.6 × 1.8/0.6 collision
  body on the server, while MPM adjusts client POV. This is existing MPM design,
  not Combatives discarding an MPM gameplay scale. Compatibility must choose and
  document whether to preserve it or deliberately introduce synchronized
  gameplay scale.

### C5 — first-person hand ownership (medium cosmetic; first person; client)

* **MPM+:** `RenderEvent#hand` can cancel the entire hand; MPM specials suppress
  normal held item/helmet and render replacements.
* **Combatives:** `EntityRendererMixin#renderHand` applies procedural hand
  transforms and `RenderPlayerMixin#renderFirstPersonArm` resets swim animation.
* **Cause:** cancellation/replacement can prevent Combatives hand transforms or
  apply them to a different model path. Gameplay targeting is unaffected.

### C6 — nameplate anchors/visibility have independent owners (medium cosmetic; prone/scaled; client)

* **MPM+:** `RenderEvent` hide-name `riddenByEntity` sentinel and
  `RenderMPM` chat anchor based on unscaled `player.height`.
* **Combatives:** `RenderPlayerMixin#func_96449_a` cancels crawl labels.
* **Cause:** MPM can bypass/cancel a different render path; scale is absent from
  the anchor. Prone hiding may work for vanilla nameplates but not MPM chat.

## Likely conflicts requiring runtime tests

* **Mixin dispatch/load order:** verify injections into `EntityRenderer`,
  `RenderPlayer`, and `ModelBiped` execute through MPM subclasses and swapped
  model instances. A transformer exclusion or a non-biped disguise changes the
  result from double transforms to missing transforms.
* **Obstruction heuristic:** MPM checks only whether one integer block is air,
  not the scaled/posed AABB. Slabs, partial blocks, one-block tunnels, large
  models, and fluid blocks can clamp POV incorrectly.
* **MPM emote overlap:** simultaneously enabling MPM `CRAWLING`, sitting, or
  sleeping and a Combatives low pose selects MPM fixed POV constants and
  Combatives low geometry. MPM server tick can cancel the emote after movement,
  causing a one-tick discontinuity.
* **Clearance and third-party gameplay scale:** Combatives standing-exit checks
  use fixed pose boxes. A separate real-size mod or future MPM+ gameplay-scale
  implementation would make crawl entry/exit, forced crawl, suffocation and
  push-out disagree. `isResizingAllowed` may then refuse the physical resize but
  advance cached pose geometry.
* **Mount/dismount:** current MPM size cannot affect safe placement, but visual
  size can intersect mount/terrain. A future gameplay scale must be included in
  Combatives' dismount handoff/clearance boxes and recomputed immediately after
  model changes.
* **Lifecycle stale data:** MPM data is loaded/broadcast separately from
  Combatives DataWatcher and packets. Login, respawn, dimension change, and a
  scale/model edit while crawling can render one frame with old MPM POV and new
  pose. Cached disguise entity height makes `offsetY` another invalidation
  concern.
* **Sneak eye value:** Combatives currently uses 0.35 for `CROUCHING`; test this
  independently of MPM because it is far below vanilla 1.7.10 sneak eye height
  and may represent port semantics rather than intended local behavior.
* **FOV:** MPM has no FOV multiplier; its renderer inherits Combatives'
  `getFOVModifier` injection. Runtime test confirms renderer swapping does not
  lose mixin state. No source-supported double-FOV transform exists.

## Existing compatibility (do not rewrite)

* Default MPM models do not overwrite Combatives width, height or bounding box,
  explaining why crawling into a one-block space works.
* MPM does not alter movement, jump, fall, landing, swimming physics, step height
  or attributes. Combatives remains authoritative for those systems.
* MPM's single targeting wrapper encloses vanilla `getMouseOver`, so block and
  entity targeting move together; there is no separate entity-ray conflict to
  fix after centralizing the origin.
* Combatives pose networking remains authoritative and independent. MPM model
  NBT is also server-broadcast when the server mod is installed. Packet IDs and
  channels do not overlap.
* MPM whole-model scale is uniform, while per-part anisotropic scale is visual.
  Keeping part scale out of collision is correct.
* Combatives already restores standing geometry for mounting and manages a
  dismount handoff. MPM never overwrites that physical restoration.
* MPM's renderer subclasses `RenderPlayer` and its model subclasses/wraps biped
  machinery, so some vanilla-targeted Combatives mixins can compose. Prefer
  narrow compatibility hooks over replacing all rendering.

## Recommended compatibility architecture and hooks

A centralized abstraction is warranted, but it should distinguish **model
presentation geometry** from **gameplay geometry**. A single
`PlayerGeometryAdapter` returning part scales would incorrectly imply that MPM's
head/arm/body axes form a collision body. Prefer:

1. `PlayerGeometryResolver` (common, deterministic): accepts player, pose and
   base profile; returns immutable `EffectivePlayerGeometry` containing width,
   height, eye-above-box-minY, standing-clearance AABB parameters, and a revision.
   Collision, clearance, eye height, mount/dismount and networking diagnostics
   consume this one result.
2. `GameplayScaleProvider` (common): vanilla returns (1,1). The MPM adapter for
   the audited version must also return (1,1), unless a new Combatives option and
   protocol intentionally promote synchronized `ModelData.size` to gameplay
   scale. Never infer gameplay scale from per-part values.
3. `ModelPresentationProvider` (client): exposes MPM uniform render scale,
   body/leg visual offsets, disguise status, and animation identity for camera
   and render integration only. Reflection/optional class loading must be
   isolated behind this adapter so dedicated servers never load client classes.
4. `TargetOriginProvider` (client): returns the same interpolated world-space
   eye origin derived from `EffectivePlayerGeometry`. A compatibility injection
   into `EntityRendererAlt#getMouseOver` should prevent its temporary player
   mutation when Combatives owns an active pose (ideally for all Combatives
   geometry), or redirect its offset query to this origin. Both block and entity
   targeting then remain in the vanilla superclass.
5. `PosePresentationBridge` (client): exposes Combatives pose/swim blend to MPM
   renderer/model code, declares which system owns corpse rotation, and prevents
   MPM crawl/sleep transforms from stacking on a Combatives pose. It also
   supplies nameplate/chat and held-item anchors from effective geometry.
6. `GeometryRevisionSync` (common networking): only needed if gameplay scale is
   introduced. Server validates model-derived scale, resolves geometry, and
   sends scale/revision with authoritative pose. Clients must not resize from
   unsanctioned local MPM presets.

Evaluation must occur on construction/spawn, authoritative pose change, MPM
model/scale update, respawn/dimension transfer, mount/dismount, and before any
clearance query. Do not cache without a revision that combines pose and base
geometry. The server owns physical dimensions and clearance; the client predicts
the same deterministic values and owns interpolation/GL presentation.

## Scaling model

### Compatibility with the audited MPM+ behavior

Use gameplay scales `Sx = 1`, `Sy = 1`. Preserve MPM's `R = size/5` and per-part
scales only in rendering/MPM POV presentation. Effective physical dimensions
remain Combatives pose dimensions. This exactly represents current MPM+
semantics and avoids granting small models extra access or large models unfair
collision without an explicit feature decision.

### If gameplay scaling is deliberately added later

Define a server-validated base standing body `(Wb, Hb, Eb)` and scale pair
`(Sx, Sy)`; do not use head/arm/leg part scale. Compose pose using documented
ratios from Combatives' base profile:

```
effectiveWidth(pose)  = Wpose / Wstanding * Wb * Sx
effectiveHeight(pose) = Hpose / Hstanding * Hb * Sy
eyeAboveMinY(pose)    = Epose / Hstanding * Hb * Sy
```

For the current profile, `Wstanding=0.6`, `Hstanding=1.8`, with pose values
defined in `SIZE_BY_POSE` and eye values defined by `getStandingEyeHeight`.
Ratios, rather than new constants, preserve the intentional pose proportions.
Clamp eye-above-minY into the effective box only if the profile contract demands
it. Camera base and target origin must both be
`interpolated(box.minY) + eyeAboveMinY`; camera-effect translations remain
visual and intentionally do not bend the interaction ray.

The current 0.6-wide crawl means width ratio 1; crawl height ratio is 1/3. A
uniform gameplay scale multiplies both axes. A supported anisotropic gameplay
scale multiplies horizontal width by `Sx` and all vertical dimensions/eye and
clearance by `Sy`. MPM part `scaleX/Y/Z` remains excluded because different
parts have contradictory extents and no gameplay contract.

## Collision and lifecycle requirements

The resolver's effective box must be used by crawl entry/exit, forced crawl,
standing and sneak clearance, one-block traversal, collision/suffocation probes,
push-out behavior, and safe dismount. `recalculateSize` must compare actual
geometry to the last resolved result rather than fixed pose constants and must
not advance its cache when resize is refused. Preserve `stepHeight=0.6` unless a
separate movement-scale design explicitly changes it.

On login/respawn/dimension change, resolve only after authoritative scale is
available, otherwise use vanilla gameplay scale and revise on receipt. On death
and sleep, select their pose profile; on mount restore resolved standing rider
geometry; after dismount resolve the chosen safe pose at the destination. A
model/scale change while crawling must atomically re-run low and standing
clearance on the server, keep crawl if standing is obstructed, publish the new
revision, then update client camera/target/render together.

## Prioritized implementation plan

### P0 — correctness

1. Add common `geometry` classes (`PlayerGeometryResolver`,
   `EffectivePlayerGeometry`, `GameplayScaleProvider`) and migrate fixed
   `SIZE_BY_POSE`, eye height, AABB construction and `isPoseClear` out of
   `EntityPlayerMixin` without changing values.
2. Add optional MPM detection/adapters with no hard client linkage on the
   dedicated server. For this source version return gameplay scale 1.
3. Add a targeted optional mixin/plugin entry for
   `noppes.mpm.client.EntityRendererAlt#getMouseOver` so MPM does not translate
   player position behind the resolver. Have vanilla block and entity selection
   consume the resolver eye origin; instrument camera/ray equality before and
   after the pass.
4. Stop sharing `yOffset` as geometry state. Update `EntityRendererMixin` to
   consume resolved eye-above-box-minY directly and ensure MPM cannot reset a
   value used by targeting. Retain Combatives procedural transforms after base
   origin selection.
5. Extend pose sync with a geometry revision only if non-unit gameplay scaling
   is adopted; validate and compute on server first.

Likely files: `EntityPlayerMixin`, `EntityRendererMixin`, `ICombativesPlayerPose`,
`PoseSync`, pose packets, mixin JSON/plugin, and new `geometry`/`compat.mpm`
packages.

### P1 — integration

1. Route crawl/stand/sneak clearance, resize guards, obstruction checks and
   mount/dismount handoff through resolved AABBs.
2. Add invalidation for login, respawn, dimension change, death/sleep,
   mount/dismount and MPM NBT updates; test changing size/model while low.
3. Explicitly keep movement acceleration, jump/fall/landing, gravity and step
   height independent of MPM visual size, or design them as a separate
   server-authoritative feature. Do not silently scale attributes.
4. Ensure `CameraController`/FOV remains the final Combatives-owned effect layer
   over the resolved base eye. MPM has no separate FOV calculation to preserve.

Likely files: `EntityPlayerMixin`, `EntityMixin`, `PlayerStepHeight`,
`CrawlingSystem`, `SwimmingSystem`, mount lifecycle code, `CameraController`,
`EntityRendererMixin`, and new lifecycle/event adapters.

### P2 — presentation

1. Bridge Combatives pose/blend into `RenderMPM`/`ModelMPM`; choose exactly one
   owner for prone corpse/body rotation and suppress MPM `CRAWLING` stacking.
2. Adapt custom biped parts, armor, held items, back items and first-person arms;
   define graceful behavior for non-biped disguises.
3. Use effective/presentation geometry for name/chat anchors and preserve
   Combatives prone-name hiding across MPM render paths.
4. Verify third-person grounding and mount-relative visual offsets at all MPM
   sizes.

Likely files: `RenderPlayerMixin`, `ModelBipedMixin`,
`CombativesVisualPoseHelper`, optional mixins for `RenderMPM`, `ModelMPM` and
`RenderEvent`, plus client-only presentation adapters.

## Practical test matrix

Run each relevant row in first and third person, integrated server and dedicated
server with two clients (local and remote observation). Record client/server
width, height, full AABB, eye-above-minY, camera world Y, block-ray start,
entity-ray start, pose/revision, `yOffset`, renderer class and MPM model data.

| Model case | States/actions | Required assertions |
|---|---|---|
| Default MPM (`size=5`, all parts 1) | stand, sneak, crawl, swim | Physical values match Combatives-only; camera, block ray, entity ray agree; remote pose matches server. |
| Small whole model (`size=1`) | all core states | Under current policy collision remains unscaled and this is documented; MPM render/POV does not offset ray away from resolved eye. |
| Large whole model (`size=10`) | all core states | Same policy; no camera clipping/double scale; name/item anchors reviewed. |
| Independent part axes (body/head/legs X/Y/Z at 0.5 and 1.5) | stand, sneak, crawl, swim | Part scale is visual only; no physical/eye changes; armor/items track parts acceptably. |
| Stand → jump → fall → land | each model case | No stale pose/size, motion unchanged, landing camera fires once, FOV/bob not doubled. |
| Crawl under full block, slab and partial/mod block | enter, move, release crawl | Entry succeeds only for resolved low AABB; exit remains forced until resolved standing AABB clears; MPM air-block heuristic cannot move POV. |
| Low-pose interaction | near/far blocks above/below crosshair and entities | `objectMouseOver` block/entity agrees with rendered base crosshair; server accepts interaction/attack; reach length unchanged. |
| Swim surface/submerged | enter/exit sprint swim | 0.6-high box and resolved eye agree both sides; water-eye test and camera transition agree. |
| Mount/ride/dismount | horse, boat/minecart, one custom vehicle; clear and obstructed exits | Standing geometry restored while mounted; safe dismount uses resolved standing/low boxes; no stale low eye/MPM emote. |
| Live model update while crawling | size, each part axis, model type, disguise on/off | Server policy applied atomically; no one-frame standing ray, stale cached disguise height, illegal stand, or client/server box mismatch. |
| Lifecycle | login already configured, death/respawn, portal/dimension, logout/rejoin, sleep/wake | Default geometry only until authoritative data; then one revision update; pose/model render and ray converge. |
| MPM emote overlap | MPM crawl/sit/sleep during Combatives stand/crawl/swim | Physical pose remains server-owned; incompatible visual transform is suppressed or explicitly defined; targeting never uses emote constants. |
| Feature toggles | MPM POV on/off; Combatives camera/FOV/bob on/off | Renderer restoration is safe, no lost mixin state, no double transform, targeting origin invariant. |

For multiplayer, compare an attacking client's chosen entity with the server
damage result and compare the remote observer's pose/AABB diagnostics. Include
latency during pose and model changes. A test passes only when visible base eye,
interaction start and server-authoritative geometry agree; matching the
crosshair cosmetically while retaining a displaced ray is not sufficient.
