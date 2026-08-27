# Mounted camera ownership audit

## Runtime call flow and the modified local

Minecraft 1.7.10's `EntityRenderer#orientCamera` initializes its first float local from
`renderViewEntity.yOffset - 1.62F`. The sleeping branch adjusts that local before vanilla
reads `prevPosX`, `prevPosY`, and `prevPosZ` to interpolate the camera origin. Combatives'
`@ModifyVariable` is placed at that first `prevPosX` read, so its input is not an abstract
player eye height: it is the **live, position-relative vertical camera offset selected by
the camera pipeline up to that point**. A coremod, mixin, renderer wrapper, or vehicle that
changes the local or the fields used to form it before this read can therefore legitimately
change the incoming value.

The local is subtracted from interpolated `posY`. Standing normally supplies the legacy
zero-like offset. Combatives' crawl/swim geometry instead selects the offset that places the
camera at interpolated AABB minimum Y plus `eyeAboveMinY`. Sleeping retains vanilla's special
path. The tail injection adds procedural bob, lean, shake, and other presentation transforms
after the base origin has been chosen.

## Confirmed regression

Combatives recalculated this local for every `ICombativesPlayerPose`. Mounted pose lifecycle
already clears crawl/swim and selects `STANDING`, but `STANDING` did not mean that Combatives
owned the camera: it merely described the rider's body. Replacing the incoming local with
standing geometry discarded a vehicle's already-selected cockpit/seat offset. MCHeli exposed
the bug because its rider camera has a visible seat-specific vertical displacement; the same
ownership violation was possible for vanilla horses and any custom mount.

## Ownership after the fix

* An independently moving Combatives player uses accepted effective geometry for the base
  camera and authoritative center-view interaction ray.
* While `Entity#isRiding()` is true, the incoming `orientCamera` local is passed through
  unchanged. The vanilla or modded mount camera pipeline owns the mounted base position.
* Mounted targeting is also passed through to vanilla/mod hooks instead of being replaced by
  Combatives' AABB-derived `InteractionRay`. This preserves paired vehicle camera/targeting
  changes and avoids creating a new crosshair mismatch.
* Combatives' procedural tail transforms remain additive while mounted, including registered
  horse/vehicle camera behaviors. They do not take ownership of the base seat position.
* On dismount, `isRiding()` becomes false and ownership returns immediately to current accepted
  player geometry. The existing dismount handoff remains responsible for standing clearance;
  there is no cached mounted offset to leak into the next frame.

The normal boundary remains the vanilla riding relationship, so it applies equally to horses,
MCHeli's direct rider view, and custom mounts. MCHeli also has a distinct gunner/always-camera
path which cannot be identified by that relationship and is handled by the optional boundary
below.

## MCHeli's two camera paths

The bundled MCHeli sources confirm that planes and tanks share the broad tick-handler pattern,
but each can select one of two render view entities. In the ordinary pilot path,
`MCP_ClientPlaneTickHandler` / `MCH_ClientTankTickHandler` restores the real client player as
`renderViewEntity`. The player is directly riding the aircraft (or an MCHeli seat), so the
generic mounted pass-through retains the offset which reaches vanilla `orientCamera`. This is
the path on which the F-22 height was already corrected.

In gunner mode, camera-id views, or a vehicle configured with `isAlwaysCameraView`, those tick
handlers instead call `MCH_ViewEntityDummy.update(vehicle.camera)` and install that dummy as
`renderViewEntity`. The tank updates `vehicle.camera` from its vehicle/seat transform before the
client tick handler copies its exact position and rotation into the dummy. The dummy subclasses
`EntityPlayerSP`, but deliberately has no `ridingEntity`. Consequently it enters vanilla
`orientCamera`, where the old Combatives rule saw an unmounted player and replaced the incoming
MCHeli camera local with standing player geometry. The resulting upward shift was not a tank
renderer `glTranslate`: `MCH_RenderTank` only positions tank model geometry after the world
camera has already been established.

`compat.mcheli.MCHeliCameraCompat` now recognizes the exact discovered dummy class by name. It
does not load, link, reflect over, or mix into any MCHeli class, so Combatives remains safe when
MCHeli is absent. The check is centralized rather than scattered through general camera code.
For that dummy, Combatives preserves the incoming `orientCamera` local just as it does for a
direct rider and labels debug ownership `MCHELI`. Procedural transforms are still composed at
the `orientCamera` tail, after MCHeli's base. Combatives also leaves `getMouseOver` on its native
path for the dummy, avoiding an AABB-derived ray from a synthetic player; MCHeli weapon/gunner
aiming remains entirely MCHeli-owned.

## Debugging

With `debugCamera=true`, a pose or mount-ownership transition logs rider and riding-entity
classes, incoming offset, calculated pose offset, selected offset and owner, pose, current and
interpolated `posY`, AABB minimum Y, `yOffset`, `getEyeHeight()`, effective eye-above-minimum,
base camera Y, and procedural/final Y. Normal users receive no additional logging.

## Manual regression checklist

1. Verify first-person height and targeting while standing, sprinting, and crouching.
2. Enter, remain in, and exit crawling; repeat for swimming and crawl transitions.
3. Mount a vanilla horse, ride/turn/jump it, target blocks/entities, and dismount under open
   space and near constrained clearance.
4. Enter an MCHeli pilot seat and each applicable passenger seat; compare cockpit height with
   Combatives absent, then enter/leave repeatedly and target blocks/entities before, during,
   and after riding.
5. Confirm the frame immediately after each dismount uses player geometry, with no retained
   seat offset or camera jump beyond the mount's native transition.
6. Repeat representative checks with MPM geometry/default, scaled, and disguise states.
7. Repeat with procedural bob, lean, shake, horse behavior, and FOV effects enabled and
   disabled; effects should compose after, rather than replace, the mounted base camera.
8. On a dedicated server, repeat interaction checks before and after riding and confirm server
   hit results agree with the client crosshair.
9. Test an F-22 and another aircraft in pilot, camera-id, and gunner modes; switch first/third
   person and enter/exit repeatedly while watching for a one-frame height jump.
10. Repeat with a tank and another ground vehicle as driver and, where supported, passenger or
    gunner. Exercise vehicle weapon aiming and confirm ordinary block targeting after dismount.
