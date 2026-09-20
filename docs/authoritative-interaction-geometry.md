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

Each C07 or C08 packet owns exactly one block target. The server passes the
packet's coordinates, face, and (for C08) relative hit coordinates unchanged
into vanilla handling. This includes all C07 start, abort, and finish packets,
so the stages of a mining sequence cannot be split by a later server ray. It
also preserves C08's negative-coordinate air-use sentinel. Vanilla reach,
build-height, spawn-protection, game-mode, and harvest checks remain the
authority for whether that packet is accepted.

The server may independently trace the synchronized interaction geometry when
verbose diagnostics are enabled. That result is comparison data only: it does
not replace or reject the packet target when player state or world state has
advanced since the client selected the block.

## Initial presses and held-button continuation

Minecraft 1.7.10 treats the initial press and held continuation separately.
An initial left click enters `Minecraft#clickMouse` and
`PlayerControllerMP#clickBlock`. While the button remains down,
`Minecraft#sendClickBlockToController` continues through
`PlayerControllerMP#onPlayerDamageBlock`; that continuation must remain active
for ordinary survival mining. An initial right click enters
`Minecraft#rightClickMouse`, and a held right button may enter it again only
after the vanilla `rightClickDelayTimer` expires. Combatives neither adds a
controller call nor alters that timer.

A later vanilla call is a separate action and takes its own current
`objectMouseOver` snapshot. If the first action changes the world, that later
call can legitimately select a neighboring block and send a second C07 or C08
packet. This is normal repeat behavior, not one ray selecting two blocks. A
Combatives duplication would instead mean an extra controller call in one tick,
a hook registered twice, or one packet executed at a target other than its own.
The mixin JSON lists remain empty and `CombativesCorePlugin` remains the sole
registration owner.

## Synchronization and revisions

The server increments a geometry revision whenever an accepted pose/size/eye
tuple changes. Geometry packets carry that revision with the server-resolved
MPM scale tuple. Clients accept monotonically newer revisions and calculate
their target/camera from the synchronized tuple. State-change comparison keeps
the existing packet stream transition-based rather than sending geometry every
tick.

Verbose interaction diagnostics label initial and held controller calls plus
C07/C08 send and receive boundaries. Every record includes the player tick,
phase, action, complete available packet target, and current client
`objectMouseOver`. Receive records include the independent server ray target.
The additional server trace is not performed while verbose diagnostics are
disabled. Two send records on later ticks demonstrate vanilla held-input
repetition; one receive record with differing packet and ray targets
demonstrates timing disagreement without changing packet ownership.

## Compatibility boundary

This design contains no model-specific targeting constants. Arbitrary MPM
sizes and disguise proportions are converted once into accepted gameplay
geometry. Standing, crouching, crawl/prone, swimming, flight/spin, sleeping,
and dying poses all use the same box-floor anchor rule. Optional MPM reflection
remains isolated from client rendering types, so dedicated-server classloading
does not link Minecraft client or MPM renderer classes.

## Vehicle-owned transition

The shared mount handoff state is consulted by client targeting. While mounted,
or while MC Heli can still commit its bounded final post-detach exit, native
vehicle camera/ray behavior wins. After exit writes are quiet, the synchronized
Combatives view ray resumes. This ownership transition never changes a C07 or
C08 after the client has created it; every packet retains its own logical target.
