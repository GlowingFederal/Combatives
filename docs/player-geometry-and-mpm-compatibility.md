# Authoritative player geometry and MPM+ compatibility

Combatives gameplay geometry is owned by `PlayerGeometryResolver`.  Its immutable
`EffectivePlayerGeometry` values contain the physical width, height, pose, eye
height above the bounding-box minimum, and the operation which constructs a
clearance box.  The active value describes the geometry successfully installed
on the entity; a rejected resize retains the previous value and is retried on a
later pose evaluation.

Clearance, collision resizing, `getEyeHeight()`, first-person base camera origin,
and vanilla targeting consequently use the same physical representation.  The
camera base is `boundingBox.minY + eyeAboveMinY` (with position interpolation);
camera bob, landing and shake remain later visual transforms and do not move the
interaction ray.  Mutable `Entity#yOffset` is not a Combatives geometry input.

## MorePlayerModels+ boundary

MPM+ model `size`, body-part scales, model offsets and disguises are presentation
properties.  They do **not** alter Combatives gameplay hitboxes; gameplay scale
is 1 on both axes.  The optional, client-only pseudo-mixin for
`noppes.mpm.client.EntityRendererAlt` restores the player's position samples
immediately before its call to vanilla `EntityRenderer#getMouseOver`.  This
prevents MPM's independent POV adjustment from moving the complete block/entity
targeting pass while leaving vanilla reach, block ray and entity sweep intact.
The samples are restored again on return as a defensive measure.

No MPM class is referenced by common code.  The pseudo-mixin is offered only on
the physical client and tolerates an absent target, so dedicated servers and
Combatives-only clients do not load MPM client classes.

Existing MPM humanoid crawling rendering is intentionally unchanged.  The
current `RenderPlayer`/`ModelBiped` inheritance composition explains why the
Combatives pose already reaches ordinary MPM humanoids.  Potential custom
disguise grounding, first-person arms, nameplates, and double prone animation
remain deferred until a reproducible rendering defect exists.

## Crouching eye investigation

The old `0.35` crouching eye came from Aqua Acrobatics source.  In that source,
however, the `getEyeHeight` injection which would consume the cached value is
commented out.  Combatives enabled the consumer while copying the transitional
constant, accidentally turning it into the gameplay eye height for ordinary
sneaking.  `CROUCHING` is normal sneaking in this port, not a second prone pose,
so the resolver now uses `1.54` (the vanilla 1.62 eye lowered by the 0.08 sneak
camera displacement).

## Manual verification still required

Runtime game testing was intentionally not performed in this change.  Verify
standing, sneaking, crawling, swimming, jumping/falling/landing; one-block,
slab, stair and partial-block clearance; crawl block/entity interaction above,
at and below the crosshair including maximum reach; first/third person with MPM
POV and Combatives camera effects toggled; default and LOTR humanoids at small
and large sizes and altered part scales; and a dedicated server with both mods
on both sides.  In every MPM size case the physical hitbox must remain the
resolved vanilla/Combatives size.
