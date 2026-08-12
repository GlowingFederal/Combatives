# Minecraft 1.7.10 player coordinate contract and ray-origin repair

## Source trace

This audit follows the MCP/Forge 1.7.10 implementations rather than applying modern player-position assumptions:

* `Entity#setPosition` builds the box with its lower face at `posY - yOffset + ySize`.
* Movement's bounding-box-to-position synchronization performs the inverse: `posY = boundingBox.minY + yOffset - ySize`.
* `yOffset` is therefore a legacy entity-origin displacement and `ySize` is the transient vertical collision adjustment. Neither is an eye height.
* `Entity#getPosition(partialTicks)` interpolates the entity position and adds `getEyeHeight()` to Y. There is no separate `getPositionEyes` contract in the 1.7.10 targeting path; names with that meaning in newer mappings must not be projected onto this version.
* `EntityRenderer#getMouseOver` uses that vector as the common block and entity ray start, then extends `getLook(partialTicks)` by controller reach.
* `EntityPlayer` deliberately uses the legacy position convention inherited by `EntityPlayerSP` and `EntityPlayerMP`. Thus `posY` is **not generally the player's feet**. For the normal local player it is maintained above the box floor by `yOffset - ySize`.
* `EntityPlayer#getEyeHeight()` is an offset from this legacy `posY`, not an offset from `boundingBox.minY`. Forge's `getDefaultEyeHeight()` and modern pose geometry expressed above the feet are different APIs.
* `EntityRenderer#orientCamera` independently starts with `yOffset - 1.62F` and subtracts that local from interpolated `posY`. It does not make `getEyeHeight()` a feet-relative value.

Consequently the general targeting invariant for a desired pose eye offset is:

```text
legacyEyeHeight = boundingBox.minY + desiredEyeAboveMinY - posY
rayOriginY      = interpolatedPosY + legacyEyeHeight
                = interpolatedBoundingBoxMinY + desiredEyeAboveMinY
```

This conversion intentionally contains no measured `1.58` compensation. At a sample where `yOffset - ySize` is `1.58`, `posY - minY` naturally is `1.58`; the same term cancels algebraically at the legacy API boundary.

## Combatives violation and repair

`EffectivePlayerGeometry.eyeAboveMinY` correctly stores a feet/box-relative value (`1.62` standing and `0.28` prone). `EntityPlayerMixin#getEyeHeight`, however, returned that value directly. The vanilla ray consequently became `posY + 1.62`, after `posY` already contained `yOffset - ySize`—the observed second `~1.58` contribution. This was a deterministic standing bug and was independent of MPM.

The mixin now converts the pose value to the legacy pos-relative return value. The dedicated-server subclass no longer replaces swimming eye height with a second feet-relative constant. Size shrinking also keeps the old box floor; the former `minY = posY` assignment confused the same two coordinate spaces and could move authoritative collision geometry during pose changes.

First-person pose camera interpolation remains a render-only conversion from the same box-relative geometry. Procedural transforms remain visual, and neither movement nor mouse deltas are changed. Third-person targeting uses the same corrected vanilla ray start.

## Server digging validation

The client sends the block coordinates selected by its authoritative view ray;
the dedicated server does not construct a replacement ray. It does, however,
apply vanilla's preliminary digging-distance guard from a hard-coded
`player.posY + 1.5` origin. That standing-only approximation disagrees with the
client for crawl, swim, and scaled-model eyes and can reject a block which the
client legitimately selected.

Combatives now substitutes an equivalent legacy Y position only while that
guard reads `EntityPlayerMP.posY`. After vanilla adds `1.5`, the resulting
validation origin is `boundingBox.minY + effectiveGeometry.eyeAboveMinY`, the
same accepted physical eye used by client targeting and synchronized server
geometry. Packet action handling, the selected coordinates, vanilla's distance
tolerance, spawn protection, and game-mode checks remain unchanged.

## Diagnostics and verification matrix

The throttled targeting line now includes `posY`, all interpolation samples, both box Y faces, `yOffset`, `ySize`, entity height, legacy `getEyeHeight()`, resolved eye-above-minimum, expected origin, computed vanilla origin, delta, look, reach, and hit result. Camera diagnostics separately report the base and final procedural origin and pose transition.

For standing, walking, sprinting, jumping, falling, landing, crawl enter/idle/move/exit, swimming, mounting, and dismounting, the numerical acceptance check is `actualVanillaOriginY - expectedTargetOriginY ~= 0` (apart from normal interpolation timing). Collision checks must retain a stable box floor while shrinking and reject expansion into blocks. Test the same matrix on an integrated client and dedicated server.

MPM remains a separate compatibility layer: default or scaled MPM can still apply its intentional temporary POV translation around vanilla targeting. Combatives' existing low-pose wrapper neutralizes that MPM-owned translation only for Combatives physical crawl/swim geometry. It is not part of this core coordinate repair; any discrepancy with default/altered MPM after the invariant above passes must be reported as a second MPM issue.
