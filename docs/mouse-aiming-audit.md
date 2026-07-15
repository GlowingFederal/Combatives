# Mouse aiming ownership audit

## Vanilla 1.7.10 aiming path

Vanilla aiming is owned by `EntityRenderer#updateCameraAndRender`: one render-frame call invokes `MouseHelper#mouseXYChange`, reads `deltaX` and `deltaY`, computes the sensitivity cubic from `GameSettings.mouseSensitivity`, optionally smooths through the vanilla `MouseFilter`s when `smoothCamera` is enabled, and finally calls `EntityPlayerSP#setAngles`. Combatives should not call `mouseXYChange`, read raw LWJGL `Mouse.getDX()` / `Mouse.getDY()`, rewrite `MouseHelper.deltaX` / `deltaY`, or call `setAngles` outside diagnostics.

## Combatives findings

- `CombativesCorePlugin` now offers `EntityRendererMixin` for visual camera effects but no longer offers `MouseHelperMixin`, so Combatives has no active mixin that rewrites raw mouse deltas before vanilla sensitivity scaling.
- The dormant `MouseHelperMixin` source remains available for future diagnostics, but it is not registered in either the GTNHMixins early-loader list or the legacy client mixin JSON. If it is ever re-enabled, it must be treated as an input-path change because it writes `deltaX` and `deltaY` after `mouseXYChange`.
- `EntityRendererMixin` does not invoke `mouseXYChange`, read `Mouse.getDX()` / `Mouse.getDY()`, or call `setAngles`; it applies visual-only GL transforms after vanilla camera orientation and low-pose eye-height interpolation inside `orientCamera`.
- `EntitySetAnglesDiagnosticsMixin` is not registered. If aiming spikes need live proof later, prefer temporarily registering diagnostics that observe `setAngles` rather than any hook that mutates mouse deltas.

## Angelica comparison

- Angelica zoom does not directly consume `MouseHelper.deltaX` / `deltaY` or call `MouseHelper#mouseXYChange`; its aiming-affecting hook modifies the vanilla `8.0F` sensitivity multiplier in `EntityRenderer#updateCameraAndRender` while the zoom key is held.
- Angelica also redirects `GameSettings.smoothCamera` reads in `updateCameraAndRender` and `updateRenderer` so zoom can use vanilla mouse filters. That changes vanilla smoothing only during zoom, but it still relies on vanilla's single `updateCameraAndRender` mouse-consumption path.
- Angelica's iconify guard redirects the `Minecraft#runGameLoop` call to `EntityRenderer#updateCameraAndRender` and either skips it for tiny framebuffers or calls it once; it does not add a second camera update.

## Risk conclusions

1. The strongest Combatives-side sensitivity risk was the raw mouse-delta clamp, because it rewrote the same deltas vanilla later scales. It is now inactive by registration, not merely disabled by config.
2. The height interpolation fix should not change sensitivity because it only changes the eye-height local used by `orientCamera`; it never touches mouse deltas, `mouseSensitivity`, mouse filters, or `setAngles`.
3. Angelica can intentionally change sensitivity while its zoom key is held, and can route smooth-camera behavior during zoom, but the inspected reference code does not double-consume mouse input.
4. If weird aiming remains with Combatives' mouse hook unregistered and Angelica zoom not active, next suspects are visual-only camera rotations/FOV changing perceived aim rather than actual yaw/pitch, or a separate mod injecting into `EntityRenderer#updateCameraAndRender` / `MouseHelper#mouseXYChange`.
