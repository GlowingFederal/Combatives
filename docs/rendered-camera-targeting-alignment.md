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

## Combatives ownership conflict and fix

Combatives replaces vanilla's orient-camera local with a value that produces `boundingBox.minY + eyeAboveMinY` for every Combatives player, including standing. It then optionally cancels vanilla bobbing, applies procedural translation/rotation at `orientCamera` tail, and modifies symmetric FOV. With all reported procedural values zero, it changes neither direction nor optical center.

Before this change, the MPM hook suppressed MPM's paired targeting shift only for low poses, while Combatives suppressed MPM's rendered `yOffset` shift for every pose. Standing could therefore render from the Combatives physical eye but target from MPM's `+A` origin. That is a positional, not source-level angular, disagreement. The supplied zero target-origin delta proves the core ray geometry; it does not show which MPM wrapper sample was active for the rendered frame.

The compatibility boundary now follows the existing camera ownership: for every `ICombativesPlayerPose`, it temporarily restores genuine samples for the vanilla targeting call and returns MPM-mutated samples immediately afterward so MPM cleanup remains balanced. Model rendering and scale are untouched. The obsolete low-pose-only predicate was removed. No eye geometry, ray compensation, mouse input, projection, or authoritative movement changed.

## Diagnostics and manual checks

Every throttled targeting sample now records target origin/vector and derived target yaw/pitch. At `orientCamera` tail, after procedural transforms are known, the paired camera sample records calculated rendered base XYZ, authoritative eye Y, target XYZ, position delta, interpolated base yaw/pitch, procedural yaw/pitch/roll, angular deltas, visual translation, and FOV modifier. These are calculated from the exact values supplied to the vanilla camera path; OpenGL does not expose a world-space camera position directly after matrix composition.

Runtime testing is intentionally left to the tester. Compare horizontal/up/down and near/five-block targets while standing, sneaking, crawling, and entity targeting; repeat with MPM POV on/off and default/small/large sizes; then repeat with Combatives effects and vanilla/procedural bobbing toggled. A zero-effects pass should show zero base position and angular deltas. Nonzero procedural or vanilla bob values are visual-only and should be evaluated separately.
