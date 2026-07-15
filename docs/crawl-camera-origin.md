# Crawl camera origin audit

## Vanilla 1.7.10 `EntityRenderer#orientCamera` path

The 1.7.10 first-person camera is based on the render-view entity and the local variable initialized as `entity.yOffset - 1.62F`. Vanilla interpolates `posX`, `posY`, and `posZ` from previous/current entity position with `partialTicks`, then computes the camera Y used for fog/raycast camera context as `interpolatedPosY - (entity.yOffset - 1.62F)`.

Sleeping is a special path: vanilla adds `1.0F` to that local offset, translates the view by `0.3F`, then applies the bed-orientation yaw/pitch path. Third person also starts from the same interpolated camera position, but ray traces behind the player to clamp the third-person distance before applying third-person rotations/translations. In normal first person, vanilla only applies the small `-0.1F` forward translation and then rotates by interpolated pitch/yaw.

Important 1.7.10 detail: this camera-origin local is derived from `yOffset`, not directly from `EntityPlayer#getEyeHeight()`. Therefore a pose can have a correct `getEyeHeight()` value but still fail to move the first-person camera unless `orientCamera` consumes a pose-derived replacement for the `yOffset - 1.62F` local.

## Combatives root cause

The crawl pose, collision size, and server/client pose synchronization were changing, but the first-person base camera origin was not reliably refreshed from pose state. The prior low-pose camera hook could still depend on stale cached interpolation state and collision height timing, while the local player's `combativesEyeHeight` was only recalculated on selected watcher/update paths. As a result, entering or exiting crawl could update the body pose without making `orientCamera` consume the new pose-dependent base camera offset on that render frame; in the failing case the body/bounding box lowered while `posY` still represented the old standing camera origin.

## Current ownership model

- Physical first-person crawl height is owned by `EntityRendererMixin` replacing vanilla's `yOffset - 1.62F` local with a value computed from the current client pose every render frame; low poses return that computed value directly rather than through the cached transition interpolator.
- Standing and vanilla sneaking return vanilla's original local camera offset unchanged.
- Crawling/swimming compute the local camera offset required to place the base camera at `boundingBox.minY + 0.28`, so the first-person origin lowers even if `posY` has not yet been rebuilt around the low `yOffset`.
- `EntityPlayerMixin#setPose` immediately updates client `yOffset` and recalculates the Combatives eye-height cache when the pose changes.
- Local crawl key prediction immediately selects the low pose on the client, so the next render frame can lower the camera before waiting for an unrelated later size or watcher update.
- `CameraController` still applies only small visual translations after the base camera origin has already been selected.

## Diagnostics

With `debugCamera=true`, Combatives logs one camera-origin line when entering or exiting the low pose. The line includes player class, pose, partial ticks, interpolated `posY`, `yOffset`, `getEyeHeight()`, base camera Y before procedural effects, pose camera offset, procedural translation Y, and final approximate camera Y after procedural translation. It also warns if the pose changed but base camera Y did not change, or if a standing baseline ever diverges from vanilla's original camera offset.
