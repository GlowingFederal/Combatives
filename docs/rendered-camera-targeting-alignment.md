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

## Diagnostics and manual checks

Set `debugMpmPov=true` for one coherent sample every five seconds. `MPM POV
SAMPLE` records pose and legacy coordinates, reflected MPM `size`/`offsetY`, the
derived `A`, unmodified/MPM/Combatives camera and target origins, all three MPM
sample triplets, both ownership decisions, and the shared partial tick. This is
separate from verbose provider lifecycle diagnostics.

Runtime testing is intentionally left to the tester. Compare horizontal/up/down and near/five-block targets while standing, sneaking, crawling, and entity targeting; repeat with MPM POV on/off and default/small/large sizes; then repeat with Combatives effects and vanilla/procedural bobbing toggled. A zero-effects pass should show zero base position and angular deltas. Nonzero procedural or vanilla bob values are visual-only and should be evaluated separately.
