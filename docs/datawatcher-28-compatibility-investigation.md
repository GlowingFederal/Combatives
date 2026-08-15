# EntityPlayer DataWatcher 28 compatibility investigation

> **Implementation status:** Resolved. Combatives now keeps pose in its owned
> per-player field and synchronizes it through the existing explicit pose packet
> lifecycle; it no longer allocates or reads a private DataWatcher slot for pose.

## Scope and conclusion

This document records the source audit of the Combatives 1.0.7.1
player-construction crash `Duplicate id value for 28`. The investigation itself
did not implement a runtime fix; the resolution noted above was applied later.

The narrowest robust fix is to remove Combatives' private pose value from
`EntityPlayer`'s `DataWatcher`, retain it in the existing `combativesPose` field,
and make the already-existing Combatives pose packets the only cross-side source
of pose synchronization. No replacement watcher ID is needed. This removes the
global slot collision while preserving server authority, tracker initialization,
geometry updates, and operation without EndlessIDs.

## Root cause and meaning

`EntityPlayerMixin#combatives$constructed` runs at `EntityPlayer.<init>` return.
It initializes Combatives' geometry caches and calls
`getDataWatcher().addObject(28, Pose.STANDING.ordinal())`. `DataWatcher#addObject`
rejects a duplicate key, so construction fails if an earlier constructor hook
has inserted ID 28.

Vanilla 1.7.10 `EntityPlayer` uses IDs 16 (flags), 17 (absorption), and 18
(score), while its superclasses use lower IDs. Thus 28 is unused by vanilla,
which explains the choice, but vanilla-unused is not a Forge namespace. Coremods
and mixins mutate the same per-entity map. Another constructor injection may run
first and choose the same slot; mixin ordering changes who throws, not whether
two owners can share it. The exception proves prior occupancy, but neither the
stack nor this repository identifies the first registrant.

ID 28 is an `Integer` containing Combatives' `Pose` ordinal, initially standing.
Pose drives crawl/swim sizing, clearance, eye height, camera/render decisions,
animation, movement, and authoritative interaction geometry. It is not MPM data:
MPM scale/disguise geometry is separately resolved and sent by
`PlayerGeometrySync`, then composed with pose.

Swimming also uses entity flag bit 6 through vanilla's existing flags watcher
(object ID 0). `setFlag(6, ...)` changes a bit; it does not allocate watcher ID 6.
Crawl-key state is the private `crawlKeyDown` field and Combatives packets.

## Complete lifecycle

1. **Construction:** fields default to standing. At constructor return, size,
   eye, applied-geometry, and pose caches are initialized; watcher 28 is added as
   integer zero and `combativesPoseWatcherReady` becomes true. The crash precedes
   that last assignment.
2. **Writes:** movement/crawl logic calls `setPose`, which reads the old pose,
   assigns `combativesPose`, and updates watcher 28. Server crawl handling and
   `PoseSync.applyAuthoritativePose` use that setter.
3. **Reads:** `getPose` reads watcher 28, validates the ordinal, mirrors it into
   `combativesPose`, and returns it. Before readiness, or after a caught lookup
   exception, it falls back to the private field. Consumers use this API; none
   depends on the numeric ID.
4. **Vanilla metadata:** a server watcher change can travel in normal entity
   metadata. The overridden `func_145781_i` recognizes key 28 client-side and
   recalculates size (unless riding).
5. **Combatives metadata (already present):** `PacketPlayerPoseS2C` carries entity
   ID, pose, swimming, and crawl-key state. Its handler applies the authoritative
   state and recalculates geometry. State broadcasts reach nearby players and
   optionally the owner; `StartTracking` initializes a new observer; login,
   respawn, and dimension-change events send again. This covers dedicated and
   integrated servers, owners, remote players, and late trackers.
6. **Authority:** crawl toggles are server-handled requests. The legacy C2S pose
   handler explicitly refuses client replacement/cancellation of active server
   state. Geometry has a separate revisioned S2C packet and tracking path.
7. **Redundant callback:** after S2C application, `PoseSync` directly calls
   `player.func_145781_i(28)`. This emulates watcher dirtiness even though pose
   application already recalculates. It is the only fixed-ID assumption outside
   the mixin.

DataWatcher synchronization is therefore redundant in the current architecture.
Removing the watcher while retaining direct state and every explicit packet/event
path does not silently remove synchronization.

## EndlessIDs assessment

EndlessIDs' DataWatcher module is relevant to supported metadata ID capacity and
packet encoding, not evidence that it owns 28. Expanding the vanilla range does
not namespace allocations, reserve 28, or permit two objects at one key; the
duplicate check remains correct.

There is no evidence in the crash or checked-in sources that EndlessIDs claimed
28. It may merely enable expanded watcher functionality while another player
mixin owns the slot. CoreTweaks diagnostics might identify the registrant in a
complete runtime log, but attribution requires that log or exact pack source and
configuration. The recommended fix neither requires nor special-cases EndlessIDs.

## Allocation audit

| Entity | ID | State/type | Purpose | Risk and disposition |
| --- | ---: | --- | --- | --- |
| `EntityPlayer` | 28 | `Integer` `Pose.ordinal()` | Crawl/swim posture and resulting geometry/render state | High fixed-slot collision risk; remove it and use owned state/packets. |

This is the only Combatives `DataWatcher#addObject` allocation and the only
Combatives `updateObject`/`getWatchableObject*` use. Flag bit 6 is within vanilla
watcher 0, not another allocation. `PoseSync`'s literal 28 is the callback for
the same watcher. No non-player Combatives entity allocates metadata.

## Options and tradeoffs

### A. Owned field plus existing packets — recommended

Make `combativesPose` authoritative, remove watcher registration and callbacks,
and retain the existing pose packets and explicit recalculation.

This is deterministic, dependency-free, needs no packet negotiation or user
action, and changes no gameplay algorithm. The risk is a missed entity lifecycle
send leaving a remote player standing; existing tracking, login, respawn,
dimension, transition broadcast, and owner paths already cover those cases and
must be regression-tested.

### B. Dynamically allocate a free ID

Vanilla exposes no stable allocation API. Typed-getter probing is exception-driven
and incomplete; reflecting the private map couples the mod to coremod changes.
Server and clients construct separate entities under potentially different
mixin order and client-only mods, so they may choose different IDs. Metadata
packets carry an ID/value, not an allocation manifest, and receivers must already
have the same object/type. Later constructor hooks can also take a scanned slot.

Synchronizing a selection adds bootstrap ordering, per-player mapping, reconnect
and tracker state, and packet-decoding races. EndlessIDs can enlarge the range but
does not solve agreement or ownership. This is less safe than the custom packet.

### C. Configurable ID

This works only when every client/server has the same setting, the slot is free
for every player, and the active encoding supports it. It transfers conflict
resolution to users and supplies no safe default. It is at most an emergency
escape hatch, not a robust architecture.

### D. Another fixed ID

Forge 1.7.10 has no watcher-slot registry. Aqua's reference source makes pose and
crawl IDs 30 and 31 configurable, illustrating contention rather than defining
safe alternatives. Any other vanilla-compatible constant is musical chairs;
an expanded-only value would improperly require EndlessIDs.

### E. Optional EndlessIDs ID/API

Even a documented expanded-ID adapter would require identical endpoint setup and
collision ownership, and would maintain two sync architectures where Combatives'
channel already suffices. It offers no benefit to ordinary Forge installations.

### F. Extended properties, NBT, or maps

1.7.10 extended entity properties can own server state but still need custom
remote sync. A weak map complicates lifetime. NBT is wrong for transient pose:
login/respawn should re-evaluate crawl/swim. The mixin already provides the
per-instance `combativesPose` field, making it the cleanest owned storage.

## Concrete implementation plan

1. Remove `POSE_WATCHER_ID`, `combativesPoseWatcherReady`, and constructor
   `addObject`; preserve all other geometry initialization.
2. Make `setPose` null-normalize and assign only `combativesPose`; make `getPose`
   return it with standing fallback, eliminating broad lookup exception handling.
3. Remove the key-28 behavior from `func_145781_i`; retain its superclass behavior
   or remove the override if nothing remains.
4. Remove `func_145781_i(28)` from `PoseSync` and update diagnostics. Keep explicit
   `recalculateSize()`; do not change camera, movement, interaction, animation,
   MPM, rendering, or geometry algorithms.
5. Keep packet schema/discriminators, crawl validation, server authority, range
   broadcasts, owner sends, tracking sends, and geometry revisions unchanged.
6. Add source checks against Combatives watcher allocation and packet ordinal
   round-trip/fallback tests where the legacy harness permits.
7. Validate dedicated and integrated login; crawl/swim transitions; late tracking;
   respawn/dimension change; obstructed exit; server dig/use rays; MPM/MPM+ default,
   scaled, and disguise geometry; and large packs with and without EndlessIDs.

The future patch should be limited to redundant metadata storage and callbacks.
It should not alter unrelated movement, camera, targeting, animation, or
compatibility behavior.
