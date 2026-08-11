# Rendered camera and targeting alignment audit

## Vanilla 1.7.10 path

`updateCameraAndRender` runs mouse input and `getMouseOver`, then `renderWorld`; `renderWorld` calls `setupCameraTransform`, which establishes the centered perspective projection and calls `orientCamera`. In first person, `orientCamera` initializes its legacy local to `yOffset - 1.62F` and translates the view from interpolated entity position to:

```text
cameraX = lerp(prevPosX, posX, partialTicks)
cameraY = lerp(prevPosY, posY, partialTicks) - (yOffset - 1.62)
cameraZ = lerp(prevPosZ, posZ, partialTicks)
yaw     = lerp(prevRotationYaw, rotationYaw, partialTicks)
pitch   = lerp(prevRotationPitch, rotationPitch, partialTicks)
```

The targeting origin is the `Vec3` returned directly by
`renderViewEntity.getPosition(partialTicks)`. Minecraft 1.7.10's
`EntityLivingBase#getPosition` is only a position interpolator. Its non-unit
partial-tick branch computes each coordinate from `prevPos` and `pos`; in Y the
source expression is:

```text
rayOriginY = prevPosY + (posY - prevPosY) * partialTicks
```

The `partialTicks == 1.0F` branch returns current `posY`. Neither branch reads
`yOffset`, `ySize`, the bounding box, or `getEyeHeight()`. In this legacy
coordinate system the entity position is already offset from the AABB floor.
For the observed ordinary standing state this gives:

```text
minY                         = 72.0
posY                         = 73.62000000476837
desired eye above minY       = 1.62
legacy getEyeHeight          = minY + 1.62 - posY ~= 0.0 (not read here)
vanilla target origin Y      = lerp(prevPosY, posY, partialTicks) ~= 73.62
vanilla camera local         = yOffset - 1.62 = 0.0
vanilla camera origin Y      = interpolated posY - camera local ~= 73.62
```

Thus `1.62` is the AABB-relative gameplay eye, not the correct value to return
unchanged from this version's position-relative `getEyeHeight()` API.

Sleeping and third-person branches add their own vanilla transforms. View bobbing is applied later in camera setup and changes the model-view matrix but not `Entity#getLook`. The projection is symmetric; FOV changes its scale, not its optical center. The GUI crosshair is centered, so absent later model-view rotations its direction is the camera forward direction.

## MPM renderer

The vendored `EntityRendererAlt` overrides only `updateCameraAndRender` and `getMouseOver`. It does not override `renderWorld`, `setupCameraTransform`, `orientCamera`, FOV, projection, pitch, or yaw, and its model-render GL transforms do not surround world-camera setup.

Let:

```text
A = model.offsetY + (-1.615 + model.size * 0.315)
```

Around rendering MPM performs `yOffset -= A`, calls vanilla, and restores `yOffset = 1.62`. Vanilla camera Y therefore becomes `interpolatedPosY + A`. Around targeting it computes `offset = -A`, adds `-offset` (therefore `A`) to `posY`, `prevPosY`, and `lastTickPosY`, calls vanilla targeting, then subtracts `A`. The two MPM paths are intentionally paired: both shift the POV by `A` without changing direction.

A sampled `yOffset = 1.66` means the value entering the wrapper was `1.62` and `A = -0.04`; MPM's render wrapper is the writer. It is not evidence of a pitch change. MPM's unconditional post-render `1.62` explains alternating samples. Blocked, sitting, sleeping, and crawling branches can replace or clamp this value, so `0.04` is not a universal correction.

## Combatives ownership boundary

`eyeAboveMinY` remains authoritative gameplay geometry, but it is not a blanket
replacement for Minecraft 1.7.10's rendered-camera local. Standing now passes
that local through unchanged. This preserves vanilla semantics and, when MPM is
installed, preserves MPM's intentionally paired `yOffset` camera displacement.
Combatives replaces the camera local only when its applied physical pose differs
from standing. That includes its resized sneaking geometry as well as crawl/swim
states that legacy Minecraft and MPM do not understand.

The previous all-pose policy discarded MPM's camera half in `orientCamera` and
discarded its target half at the targeting boundary. Runtime diagnostics showed
the discarded values were the same transformation: with default `size=5` and
`offsetY=0`, `A = -1.615 + 5 * 0.315 = -0.04`. The camera base and targeting
samples both move down by that amount. Restoring target samples in standing
while relying on an AABB-derived camera base was therefore the wrong ownership
boundary, not evidence that either path needed a numeric compensation.

The subsequent runtime trace exposed a second interaction. Combatives was
recomputing the legacy eye offset from **live** `posY` every time
`getEyeHeight()` was called. During MPM targeting, raw `posY` was temporarily
`73.58` while the AABB remained at `72`; the calculation therefore returned
`72 + 1.62 - 73.58 ~= 0.04`. That live derivation was invalid because it made
the legacy API vary during MPM's temporary mutation. The actual 1.7.10
`getPosition` ray expression itself does not call that API.

The legacy conversion is now cached when physical pose geometry is recalculated,
instead of being derived from compatibility renderers' temporary position
mutations. For ordinary standing it remains approximately zero. MPM can then
move the actual target vector by its paired transformation without that movement
being silently canceled by `getEyeHeight()`.

The compatibility boundary is pose-specific:

* standing retains vanilla/MPM camera and targeting semantics;
* sneaking uses Combatives ownership because its 1.5-block physical AABB and
  eye differ from ordinary 1.7.10 standing geometry;
* crawl and swim use the Combatives AABB-relative physical eye for both camera
  and targeting, so the hook temporarily removes MPM's unrecognized POV shift;
* MPM's samples are restored immediately after the nested vanilla targeting
  call, leaving MPM's cleanup balanced.

No literal `0.04` compensation was added. The corrected legacy
`boundingBox.minY + eyeAboveMinY - posY` conversion and bounding-box resizing
remain unchanged.

## Source of the reported `-0.12`

The reported pair `posY=78.4531575` and
`ACTUAL_TARGET_ORIGIN.y=78.3331575` compares unlike values: MPM's diagnostic
presents the **current** `posY`, whereas both vanilla rays consume the
partial-tick result of `EntityLivingBase#getPosition`. There is no Y adjustment
after that return value in either ray construction. `EntityRenderer#getMouseOver`
uses it unchanged as the entity-intersection segment start, and
`EntityLivingBase#rayTrace` uses it unchanged as the block-ray segment start.

Consequently the complete Y accounting between the MPM-mutated samples and
both recorded origins is exactly one operation:

```text
MPM: prevPosY += A; posY += A
vanilla: originY = mutatedPrevPosY
                 + (mutatedPosY - mutatedPrevPosY) * partialTicks
later Y adjustments: none
```

The measured difference therefore equals
`(1 - partialTicks) * (posY - prevPosY)` (with the sign implied by that
subtraction). A stable `0.12` means that interpolation term was stable; stability
alone cannot rule interpolation out. MPM owns the common `A` translation, but
because it applies `A` to both endpoints it cancels out of the interpolation
gap. Combatives does not introduce this value. The trace now prints
`vanillaPrevPosInterpolationY`, `currentPosMinusInterpolationY`, and
`actualMinusInterpolationY` at both call sites. For this case the first must
equal the actual origin, the second must be approximately `+0.12`, and the last
must be zero. This directly distinguishes the vanilla interpolation term from
any downstream adjustment without changing targeting behavior.

## Diagnostics and manual checks

Set `debugMpmPov=true` for focused samples every five seconds. `MPM POV
MUTATION` now labels its position triplets explicitly as raw mutation state; it
does not claim that those values are ray origins. `BASE POV TRACE` works with or
without MPM and intercepts the exact `EntityLivingBase#getPosition` and `EntityLivingBase#getLook`
return values consumed by vanilla `getMouseOver`. The camera sample is emitted
from the modified `orientCamera` local before procedural tail transforms. Target
and camera records share a monotonically increasing frame ID, and only the
camera record reports a same-frame difference.

### Verified 1.7.10 targeting call owners

The methods used by this project's 1.7.10 `EntityRenderer#getMouseOver` are
declared on `EntityLivingBase`, not `Entity`:

| Purpose | MCP call and descriptor | SRG name |
| --- | --- | --- |
| Entity-ray origin | `EntityLivingBase#getPosition(F)Lnet/minecraft/util/Vec3;` | `func_70666_h` |
| Entity-ray direction | `EntityLivingBase#getLook(F)Lnet/minecraft/util/Vec3;` | `func_70676_i` |
| Block ray | `EntityLivingBase#rayTrace(DF)Lnet/minecraft/util/MovingObjectPosition;` | `func_70614_a` |

`getMouseOver` obtains the entity-intersection start from `getPosition`, obtains
the direction from `getLook`, constructs its reach endpoint with
`Vec3#addVector`, and passes that segment to each candidate bounding box's
`calculateIntercept`. The block hit is obtained through `rayTrace`; that method
independently calls the same `EntityLivingBase#getPosition` and `getLook`
methods. The hooks therefore intercept the two direct calls in `getMouseOver`
and the two calls inside `rayTrace`, substituting one shared authoritative pair
during an active first-person targeting pass.

Runtime testing is intentionally left to the tester. Compare horizontal/up/down and near/five-block targets while standing, sneaking, crawling, and entity targeting; repeat with MPM POV on/off and default/small/large sizes; then repeat with Combatives effects and vanilla/procedural bobbing toggled. A zero-effects pass should show zero base position and angular deltas. Nonzero procedural or vanilla bob values are visual-only and should be evaluated separately.

## Authoritative center-view ray

The interpolation trace explained the `0.12` arithmetic but also confirmed the
root bug: the interpolated legacy entity position consumed by `getMouseOver` is
not necessarily the base origin selected by `orientCamera`. Preserving that
position exactly therefore cannot guarantee crosshair alignment.

Combatives now records the base first-person camera selected by
`orientCamera`, before `CameraController` applies presentation transforms. The
height is stored relative to the interpolated bounding-box floor, not `posY`.
At `getMouseOver`, the origin is reconstructed from the current interpolated
physical floor. MPM's temporary translation cancels between the interpolated Y
and the `boundingBox.minY - posY` anchor, requiring neither a model constant nor
any entity-field mutation by Combatives.

The authoritative direction remains the view entity's interpolated
`getLook(partialTicks)` vector: the centered symmetric projection adds no
angular offset. Procedural translation, shake, bob, lean, and presentation
recoil happen after the captured base and intentionally do not affect gameplay
aim.

The existing vanilla algorithm remains in charge. Its entity-ray origin/look
calls and the matching calls inside `EntityLivingBase#rayTrace` receive the
authoritative pair; reach, block tracing, candidate collection, expanded AABBs,
intercept calculations, nearest-hit ordering, extended reach, and result fields
are untouched. Sleeping, third-person, and the initial pass before a camera has
been sampled fall back to vanilla. Diagnostics report the authoritative camera
and target pairs, their deltas, and the resulting block/entity selection.
