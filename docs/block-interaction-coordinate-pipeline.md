# Block interaction coordinate pipeline audit

## Vanilla 1.7.10 ownership chain

This audit starts after rendering has populated `Minecraft.objectMouseOver`; it
does not derive another ray. `Minecraft#clickMouse` reads the current
`MovingObjectPosition.blockX/blockY/blockZ/sideHit` and calls
`PlayerControllerMP#clickBlock`. Holding the button reaches
`Minecraft#sendClickBlockToController`, which reads the then-current
`objectMouseOver` and calls `PlayerControllerMP#onPlayerDamageBlock`.

`PlayerControllerMP#clickBlock` records the initial damaged coordinates and
constructs `C07PacketPlayerDigging` with `START_DESTROY_BLOCK`. Continued damage
uses that controller state; completion sends `STOP_DESTROY_BLOCK` for the same
coordinates, while aborting/changing targets sends `ABORT_DESTROY_BLOCK` and
starts the new `objectMouseOver` target. The packet contains only its action,
integer X/Y/Z, and face. It contains no ray, eye height, AABB, or hit vector.

`NetHandlerPlayServer#processPlayerDigging` reads those same immutable packet
fields. Vanilla 1.7.10 performs build-height and squared-distance checks against
the server player position, but it does **not** perform a replacement ray trace
and cannot select a neighboring grass block. Accepted start/stop requests pass
the packet X/Y/Z to `ItemInWorldManager#onBlockClicked` or
`#uncheckedTryHarvestBlock`; the latter invokes `#tryHarvestBlock` with those
coordinates.
Consequently, if the manager truly damages grass at a different coordinate,
the first divergence must already be on the client between the displayed
`objectMouseOver` sample and the controller/packet construction sample (or be
introduced by another packet transformer). Server rejection can explain no
damage and block-state correction, but cannot rewrite wood XYZ into grass XYZ.

## Combatives audit and correction

Combatives has no second block-interaction ray. Its targeting hooks replace the
origin/direction used while vanilla populates `objectMouseOver`; its only prior
`PlayerControllerMP` hook substitutes actual key-sneak state during right-click
handling. It did not intercept digging packets, server digging handling, or
`ItemInWorldManager`.

The earlier dedicated-geometry patch did, however, translate `posY`,
`prevPosY`, and `lastTickPosY` when rebuilding an AABB. That is outside the
resize operation's ownership and changes both C03 movement coordinates and the
position used by the server's digging-distance validation. It cannot rewrite a
C07 target, but it can make an otherwise correct request fail validation or
produce a server position correction. The translation is removed: resizing
continues to preserve the accepted physical AABB floor, while position samples
remain owned by movement. The existing box-relative eye conversion bridges
those coordinate systems without an arbitrary offset.

## Diagnostic chain

Enable `verboseMovementDebug` for one dedicated-server reproduction. The trace
uses values consumed or constructed by each method; it never reconstructs a
target from player geometry:

1. `CLIENT OBJECT_MOUSE_OVER` logs type, block XYZ, `sideHit`, hit vector, and
   the client block when `clickBlock`, continued damage, right-click, or entity
   attack reaches the controller.
2. `CLIENT CONTROLLER BLOCK TARGET` distinguishes initial and held-button
   controller calls and their exact arguments.
3. `CLIENT DIG REQUEST` runs after the real C07 constructor and logs its action,
   serialized XYZ/face fields, and the client block at those coordinates.
4. `SERVER DIG PACKET RECEIVED` runs at `processPlayerDigging` entry and logs
   the received fields, server block, player position/AABB, and legacy eye Y.
5. `SERVER BLOCK DAMAGE` logs the arguments received by `onBlockClicked`,
   `uncheckedTryHarvestBlock`, and `tryHarvestBlock`, plus the harvest result.

The first unequal adjacent pair identifies the owning boundary. Initial click,
continued mining, completion, target changes, and abort packets are distinct in
the action/phase fields. Equivalent controller-boundary samples cover block
activation and entity attack, establishing whether a reproduction is digging
specific. Right-click placement details continue through vanilla's normal
packet implementation; no behavior is replaced by these diagnostics.

Runtime testing was intentionally not performed. A dedicated run should compare
the logged XYZ values literally and should also note whether the server omits a
`SERVER BLOCK DAMAGE` line after receiving a packet, which indicates validation
rejection rather than coordinate substitution.
