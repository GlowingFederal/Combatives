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

No MCHeli type, class name, reflection, or optional dependency is involved. The boundary is the
vanilla riding relationship, so it applies equally to horses, MCHeli seats, and custom mounts.

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
