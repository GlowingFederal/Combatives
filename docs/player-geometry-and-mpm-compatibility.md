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
Mutable MPM `yOffset`, part scale, and disguise data remain presentation-only.
The exception is MPM's whole-model `ModelData.size`: when the independent
`enableMpmHitboxScaling` option is enabled, Combatives resolves its uniform
render factor (`size / 5`) on both logical sides and multiplies posture width,
height, and box-relative eye height by that factor. MPM defaults `size` to 5,
accepts 1 through 10, serializes it in the complete model NBT, and applies the
same factor to all three GL axes in `RenderMPM#preRenderCallback`. Independent
head/body/arm/leg X/Y/Z scales are deliberately excluded because they do not
describe one coherent physical body.

The optional boundary uses FML mod detection and reflection, so no MPM class is
linked when the mod is absent. `ModelData.getData(player)` selects MPM's client
cache for local and remote client players and its server model-data controller
for server players. MPM broadcasts the complete NBT on changes; consequently,
the same raw value is available for client interception and server-authoritative
collision. Missing data, reflection failures, values outside MPM's legal 1–10
range, and non-finite/non-positive resolved factors fall back to scale 1.

Combatives is the sole final-dimension owner. Its player tick resolves posture
bases first, multiplies by the current MPM factor, and rebuilds a centered box
at the existing `boundingBox.minY`; it never adjusts position samples. An
expansion is accepted only when the complete requested box has no block
collisions. If obstructed, the previous applied geometry remains in force and
the next tick retries. Shrinking is immediate. This also replaces the former
server-only `setSize` enforcement that could overwrite a separately composed
dimension.

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

Runtime testing was intentionally not performed. Test standing, sneaking,
crawling and swimming targeting in all directions and at maximum reach, with MPM
POV on/off and default/small/large sizes. Also verify collision/clearance and a
dedicated server. Test raw sizes 1, 5, and 10, remote-player interception,
rapid live size changes, expansion under low ceilings/beside walls, respawn,
dimension changes, reconnect, mounts, and transitions among crawl, swim, and
standing. The authoritative ray remains anchored to the scaled effective
geometry relative to `boundingBox.minY`; no MPM position-mutation hook is used.
