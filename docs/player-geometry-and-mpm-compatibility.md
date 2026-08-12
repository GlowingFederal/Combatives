# Authoritative player geometry and MPM+ compatibility

## Verified MPM+ 4.2 mapping and optional loading

The integration is a late, client-only GTNHMixins integration offered only when
FML reports the `moreplayermodels` mod id. It uses `@Pseudo` and a string target,
so common code and dedicated servers have no symbolic MPM client dependency.
Every injector has `require = 0`; an unknown revision remains loadable, emits no
`MPM TARGET COMPAT ACTIVE` line, and retains native MPM targeting.

The vendored source declares `EntityRendererAlt#getMouseOver(float)`. ForgeGradle
reobfuscates that Minecraft override in the 4.2 production class to the SRG
`func_78473_a(F)V`; its super call is
`INVOKESPECIAL EntityRenderer.func_78473_a(F)V`, and LaunchWrapper's transformed
class retains that SRG member. The former pseudo-mixin had `remap = false` on its
target but selected `getMouseOver(F)V`. Its generated refmap therefore could not
turn that selector into SRG, exactly matching the production validation failure.
No MPM jar or Mixin export is present in this checkout; the available evidence is
the validation output, vendored source/bytecode order, and the 1.7.10 MCP-to-SRG
mapping. The repaired selectors and invocation target are explicitly SRG and
explicitly `remap = false`, so their behavior does not depend on pseudo-target
refmap remapping.

## Geometry ownership and interception

`PlayerGeometryResolver` owns immutable posture-base `EffectivePlayerGeometry`: standing is
`0.6 x 1.8` with eye `1.62`, crouching is `0.6 x 1.5` with eye `1.54`, and
crawl/swim is `0.6 x 0.6` with eye `0.28`. Eye values are offsets above
`boundingBox.minY`, never absolute world coordinates. Clearance, collision,
physical camera base, and vanilla targeting use this same geometry. The legacy
1.7.10 `getEyeHeight()` API is converted at the boundary to an offset from
`posY`; see [the coordinate-contract audit](legacy-player-coordinate-contract.md).
Mutable MPM `yOffset` and part scale remain presentation-only. MPM's synchronized
entity disguise is now a geometry input: Combatives asks MPM for its cached
`EntityLivingBase` and derives independent width, height, and eye ratios from
the entity's initialized vanilla/modded dimensions. This covers models such as
silverfish as well as living entities supplied by other mods without keeping a
hard-coded entity table. The disguise ratios are composed with the active
Combatives posture, so crawling remains a low pose instead of resetting to the
disguise's standing height. Invalid or unavailable entity dimensions fall back
to ordinary player proportions.

MPM's whole-model `ModelData.size` is also a geometry input: when the independent
`enableMpmHitboxScaling` option is enabled, Combatives resolves its uniform
render factor (`size / 5`) on the server, synchronizes its resolved scale tuple,
and multiplies posture width, height, and box-relative eye height on each client
from that authoritative tuple. MPM defaults `size` to 5,
accepts 1 through 10, serializes it in the complete model NBT, and applies the
same factor to all three GL axes in `RenderMPM#preRenderCallback`. Independent
head/body/arm/leg X/Y/Z scales are deliberately excluded because they do not
describe one coherent physical body.

The optional boundary uses FML mod detection and reflection, so no MPM class is
linked when the mod is absent. Server-side `ModelData.getData(player)` selects
MPM's saved model-data controller; Combatives sends the resolved gameplay tuple
through its own packet rather than trusting MPM's separately timed client
cache. Missing data, reflection failures, values outside MPM's legal 1–10
range, and non-finite/non-positive resolved factors fall back to player
geometry. The server resolves the synchronized `entityClass` dimensions and
sends their numeric proportions, so clients need not instantiate the disguise
to agree on collision geometry.

Combatives is the sole final-dimension owner. Its player tick resolves posture
bases first, multiplies by the current MPM factor, and rebuilds a centered box
at the existing `boundingBox.minY`; it never adjusts position samples. An
expansion is accepted only when the complete requested box has no block
collisions. If obstructed, the previous applied geometry remains in force and
the next tick retries. Shrinking is immediate. This also replaces the former
server-only `setSize` enforcement that could overwrite a separately composed
dimension.

The accepted result is retained as one complete `EffectivePlayerGeometry`, not
reconstructed later from collision width. This distinction matters for entity
disguises: a silverfish or dwarf can have different width, height, and eye
ratios. Reusing the width ratio for all three values made the physical box
correct while moving the rendered camera and gameplay ray to a different
height. Camera selection, legacy `getEyeHeight()`, the block ray, and the entity
sweep now all consume the exact width/height/eye tuple that was accepted for
the AABB. If expansion is obstructed, they continue using the previous accepted
tuple until the complete new box can be installed.

Targeting does not reuse the previous frame's rendered-camera sample. Vanilla
calls `getMouseOver` before `orientCamera`, so the current ray and current base
camera independently reconstruct the same origin from the interpolated AABB
floor and accepted eye offset. MPM's temporary position mutation cancels out of
the floor calculation, and no MPM offset or entity position is written.

At the SRG wrapper HEAD, Combatives captures genuine world `posY`, `prevPosY`,
and `lastTickPosY`. The vendored order proves this is before MPM computes and
adds its POV mutation. Immediately before the exact SRG super invocation,
Combatives saves the now-mutated values and installs the genuine values. Vanilla
then consumes Combatives' coordinate-converted `getEyeHeight()`. Immediately after the
super call, Combatives reinstalls the mutated values so MPM's own subtraction
can clean up normally. No RETURN restoration remains: restoring originals there
would be redundant/stale, while leaving originals installed before MPM cleanup
would cause MPM to subtract its mutation twice.

## Targeting and rendered-camera ownership

MPM derives one model-dependent displacement and applies the same displacement
to the legacy render camera through `yOffset` and to targeting through the three
position samples. Combatives already replaces the render-camera local with its
authoritative physical eye for every Combatives player pose. The compatibility
hook therefore removes MPM's paired targeting displacement for every
`ICombativesPlayerPose`, not merely low poses. It restores MPM's mutated samples
after the vanilla call so MPM cleanup remains balanced. See the
[rendered-camera alignment audit](rendered-camera-targeting-alignment.md) for the
complete formula and the `1.62`/`1.66` trace.

## Diagnostics and manual verification

With camera or movement debugging enabled, the actual vanilla targeting method
logs once per 20 ticks: renderer, pose, resolved geometry, box minimum, eye
height, all position samples, partial tick and interpolated Y, expected geometry
origin, actual vanilla origin, physical camera base and delta, look vector,
reach, and resulting hit position. The MPM boundary logs `MPM TARGET COMPAT
ACTIVE`, original/mutated/restored/post-target samples, and mutation amount.
This numerically tests `targetOriginY - physicalCameraBaseY ~= 0` before bob,
landing, freefall, mount motion, shake, and other visual-only effects.

### Dedicated-server regression and ownership

The dedicated-server failure was not a second ray-selection defect. The first
MPM hitbox implementation independently called `ModelData.getData(player)` on
each logical side. On a client that method reads `ClientDataController`; on a
server it reads the saved `ModelDataController` entry. Those stores are updated
by different MPM packets and at different lifecycle points. Local integrated
testing hid that ownership error because both logical sides live in one process
and normally receive the same model data quickly. A dedicated client could
therefore construct its `EntityClientPlayerMP` (and remote player targets) from
client render data while `EntityPlayerMP` was still using another size,
disguise, or the default geometry. Vanilla movement packets do not synchronize
entity width, height, AABB bounds, MPM size, or disguise-derived dimensions.

Combatives now resolves MPM model data only on the authoritative server and
sends the resulting width, height, and eye scale tuple to the owning client and
tracking clients. The packet is sent on entity construction/login, whenever the
server-observed MPM tuple changes, and when a player starts tracking the entity.
Respawn and dimension change construct or retrack an entity and therefore
repeat the handshake. Client-local MPM transformations remain available to the
renderer but can no longer replace synchronized gameplay geometry. This also
makes remote-player target AABBs consume the same tuple as server collision.

The resize path preserves the accepted physical AABB floor without writing
`posY`, `prevPosY`, or `lastTickPosY`. Those samples belong to movement and C03
network synchronization; changing them during a size-only operation alters the
server position used for digging-distance validation. The box-relative eye
conversion already bridges the legacy position and physical floor. See the
[block interaction pipeline audit](block-interaction-coordinate-pipeline.md).

With verbose movement diagnostics enabled, a five-second server sample and
each geometry transition are headed `SERVER PLAYER GEOMETRY`. The ray-time
client sample is headed `CLIENT TARGET GEOMETRY`. Both include position
samples, AABB bounds and dimensions, entity dimensions, `yOffset`, `ySize`, eye
height, physical eye offset, pose/crouch/crawl/swim state, and the server-owned
MPM scale tuple. The ray sample additionally records interpolated position and
the final authoritative origin, allowing one dedicated run to identify the
first differing value and its lifecycle transition.

Runtime testing was intentionally not performed. Test standing, sneaking,
crawling and swimming targeting in all directions and at maximum reach, with MPM
POV on/off and default/small/large sizes. Also verify collision/clearance and a
dedicated server. Test raw sizes 1, 5, and 10, remote-player interception,
rapid live size changes, expansion under low ceilings/beside walls, respawn,
dimension changes, reconnect, mounts, and transitions among crawl, swim, and
standing. The authoritative ray remains anchored to the scaled effective
geometry relative to `boundingBox.minY`; no MPM position-mutation hook is used.

## First-person hand and held item

MPM+ 4.2 deliberately cancels Forge's complete `RenderHandEvent` whenever an
entity disguise is active. That event encloses both the first-person arm and
held-item pass, which explains why even a held item disappeared; it was not a
Combatives camera transform. The optional late compatibility mixin now
uncancels only that entity-disguise case after MPM's handler returns. Vanilla's
normal first-person item renderer (including Combatives' visual-only hand
camera transform) then runs. MPM's separate cancellation for sleeping and its
empty-handed bow animation remains untouched when no disguise is selected.
