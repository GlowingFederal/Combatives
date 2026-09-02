# Leaning architecture (sliding deferred)

Minecraft 1.7.10 has no useful vanilla pose state for crawling. Combatives keeps
`Pose.SWIMMING` as the established low collision geometry. Sprint plus crawl is
an ordinary crawl request: the crawl packet has no slide-entry path. Legacy
slide state/configuration remains readable for compatibility but cannot begin a
new slide; sliding is deferred and its default is disabled.

Death, respawn, dimension change, and explicit server teleport use the existing forced
pose reset, which now also clears slide ticks and lean. Mounts, sleep, flight,
ladders, water, and damage terminate the state through the ordinary lifecycle.
Low-level `NetHandlerPlayServer.setPlayerLocation` corrections are not pose
lifecycle events: vanilla can use that path to reconcile an ordinary C03 packet.
Explicit `EntityPlayerMP.setPositionAndUpdate` relocation still resets the pose
and broadcasts the accepted standing state to the owning client and trackers.
The low geometry continues through `PlayerGeometryResolver`, including optional
MorePlayerModels scaling; sliding does not add a parallel bounding box.

Lean is a normalized `-1..1` gameplay value: negative is left and positive is
right through input, prediction, networking, and accepted state. The yaw-relative
camera/ray basis performs the one required sign conversion. The client predicts key changes
for responsiveness, but the server validates compatible state and broadcasts
the accepted value. It is enabled while standing or crouching and disabled for
crawl, swim, slide, sleep, riding, flight, ladders, and death. Both common-side
`InteractionRay` and the first-person camera use the same lateral offset. A
center-to-desired-position block trace reserves a small wall margin and clamps
the offset, so the server's authoritative dig/use ray cannot originate beyond
the blocking wall. Lean never moves or resizes the player's collision box.

On login the server sends one versioned gameplay-config snapshot containing
lean enablement, maximum physical lean distance, and MPM hitbox-scaling
enablement. Common gameplay access reads the local configuration on the server
and the snapshot in a remote client world. It never writes the client's config
file. There is no config hot-reload hook, so updates take effect for a client on
its next connection. This path is identical for dedicated and integrated
servers; shared JVM statics are not used as the client authority.

`maxLeanRoll` and `leanInterpolation` remain client-only presentation settings.
All other camera, bob, shake, FOV, mouse, horse, and diagnostic settings are
also presentation-only. Lean enablement and distance affect aim and are server
authoritative. Player pose dimensions/eye anchors are fixed gameplay rules,
while optional MPM hitbox scaling is server authoritative and its resolved
geometry continues to use the existing geometry packet.

Client presentation interpolates the server-approved lateral camera travel and
adds cosmetic roll. Lean applies one bounded additive roll to the animated
body, head, arms, and legs. Each leg receives 75% of the torso roll, a very
small mirrored brace, and a subtle lateral X-pivot shift. Existing X-axis walk
animation is untouched. Every changed angle and pivot is captured after vanilla
animation and restored after the model render, including armor `ModelBiped`
instances, so transforms do not accumulate.

Dedicated-server, latency, modded-block collision, and animation appearance
still require in-game validation; this implementation was validated by source
inspection and call-path tracing only.
