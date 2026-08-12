# Authoritative interaction geometry

## Investigation and 1.7.10 pipeline

Vanilla client targeting is populated by `EntityRenderer#getMouseOver`. Its
block path calls `EntityLivingBase#rayTrace`, while the entity sweep separately
uses `getPosition(partialTicks)` and `getLook(partialTicks)`. Previously,
Combatives replaced those inputs from a client-only `AuthoritativeViewRay`, but
the block redirect lived in a common mixin which imported that client class.
The rendered camera also independently reconstructed a height during
`orientCamera`. Those were competing implementations rather than one gameplay
definition.

In 1.7.10 the server does not normally ray trace a C07 or C08 target. Verified
workspace mappings are `NetHandlerPlayServer#processPlayerDigging`, C07 getters
`func_149505_c`, `func_149503_d`, and `func_149502_e`, and
`processPlayerBlockPlacement` with C08 getters `func_149576_c`, `func_149571_d`,
`func_149570_e`, `func_149568_f`, `func_149573_h`, `func_149569_i`, and
`func_149575_j`. Digging then reaches `ItemInWorldManager#onBlockClicked` and
`#uncheckedTryHarvestBlock`; there is no mapped `blockRemoving` method in this
workspace. Entity packets identify an entity and vanilla applies reach checks;
they do not carry an eye origin.

Combatives rebuilds player AABBs in `EntityPlayerMixin#recalculateSize`. MPM's
whole-model size and disguise dimensions enter through the optional,
common-side reflective `MpmCompatibility` adapter. MPM client render
translations and `yOffset` changes are not gameplay inputs. Dedicated servers
resolve saved MPM state, and `PacketPlayerGeometryS2C` sends the accepted scale
tuple to owning and tracking clients rather than relying on client MPM caches.

## Single semantic model

`EffectivePlayerGeometry` is the accepted gameplay shape: pose, width, height,
and interaction anchor above `boundingBox.minY`. `InteractionRay` is now the
only common-side conversion from that shape and orientation to an origin and
direction. Its authoritative variant uses the current AABB floor and current
rotation. Its render variant interpolates position, AABB floor, yaw, and pitch,
but uses the same accepted anchor. Neither variant reads `getEyeHeight`,
`yOffset`, `ySize`, an MPM renderer translation, or a previous camera sample.

The client wrapper exists only to scope the current render pass and to preserve
vanilla fallbacks for non-player camera entities. Both the block ray and entity
sweep consume the shared origin/direction. The first-person base camera uses
the same accepted box-relative anchor; procedural bob, lean, shake, and other
presentation transforms remain outside gameplay aim.

The server independently constructs the authoritative ray for C07 start-dig
and C08 block-use packets. The traced block replaces packet coordinates before
vanilla validation and manager calls; C08 face and relative hit coordinates are
reconstructed from the same intercept. Stop/abort digging retains vanilla's
ongoing-mining coordinates, and air-use retains its sentinel packet semantics.
The packet result is therefore diagnostic input, not authority. Vanilla reach,
game-mode, protection, and harvest checks remain intact and reach is obtained
from the server's `ItemInWorldManager`; no tolerance is increased.

## Synchronization and revisions

The server increments a geometry revision whenever an accepted pose/size/eye
tuple changes. Geometry packets carry that revision with the server-resolved
MPM scale tuple. Clients accept monotonically newer revisions and calculate
their target/camera from the synchronized tuple. State-change comparison keeps
the existing packet stream transition-based rather than sending geometry every
tick.

Verbose movement diagnostics now include tick and geometry revision in the
general geometry record. Server interaction records include player, tick,
revision, pose, position/AABB/size, anchor, yaw/pitch, ray origin/direction,
reach, independently traced block, packet block, and agreement. Existing
client targeting logs provide the rendered/interpolated counterpart and final
block/entity intercept. A revision mismatch identifies synchronization; equal
revisions with different origins identifies position/orientation timing; equal
rays with different hits identifies tracing/world disagreement; and a packet
mismatch identifies the controller/packet boundary.

## Compatibility boundary

This design contains no model-specific targeting constants. Arbitrary MPM
sizes and disguise proportions are converted once into accepted gameplay
geometry. Standing, crouching, crawl/prone, swimming, flight/spin, sleeping,
and dying poses all use the same box-floor anchor rule. Optional MPM reflection
remains isolated from client rendering types, so dedicated-server classloading
does not link Minecraft client or MPM renderer classes.
