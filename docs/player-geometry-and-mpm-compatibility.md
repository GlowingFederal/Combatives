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

`PlayerGeometryResolver` owns immutable `EffectivePlayerGeometry`: standing is
`0.6 x 1.8` with eye `1.62`, crouching is `0.6 x 1.5` with eye `1.54`, and
crawl/swim is `0.6 x 0.6` with eye `0.28`. Eye values are offsets above
`boundingBox.minY`, never absolute world coordinates. Clearance, collision,
physical camera base, and vanilla targeting use this same geometry. The legacy
1.7.10 `getEyeHeight()` API is converted at the boundary to an offset from
`posY`; see [the coordinate-contract audit](legacy-player-coordinate-contract.md).
Mutable MPM `yOffset`, model size, part scale, and disguise data remain
presentation-only; gameplay scale remains one.

At the SRG wrapper HEAD, Combatives captures genuine world `posY`, `prevPosY`,
and `lastTickPosY`. The vendored order proves this is before MPM computes and
adds its POV mutation. Immediately before the exact SRG super invocation,
Combatives saves the now-mutated values and installs the genuine values. Vanilla
then consumes Combatives' coordinate-converted `getEyeHeight()`. Immediately after the
super call, Combatives reinstalls the mutated values so MPM's own subtraction
can clean up normally. No RETURN restoration remains: restoring originals there
would be redundant/stale, while leaving originals installed before MPM cleanup
would cause MPM to subtract its mutation twice.

## Targeting math

For default size (`size = 1`, zero model offset), MPM computes
`offset = -0 - (-1.615 + 1 * 0.315) = 1.30`, then adds `-offset = -1.30` to all
three samples. MPM crawling/sleeping forces `offset = 1.18`, a `-1.18` mutation.
Before repair, origins relative to genuine interpolated Y were consequently:

* standing: `-1.30 + 1.62 = 0.32`;
* sneaking: `-1.30 + 1.54 = 0.24`;
* MPM crawling: `-1.18 + 0.28 = -0.90`;
* Combatives swimming without MPM's crawling animation: `-1.30 + 0.28 = -1.02`.

MPM's blocked-camera clamp can alter standing/sneaking. The repaired physical
low-pose pass removes the MPM term instead of compensating for it, yielding
`interpolated boundingBox.minY + 0.28`. Standing/default MPM behavior is left
unchanged.

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
dedicated server. MPM size must not affect Combatives-owned gameplay geometry.
