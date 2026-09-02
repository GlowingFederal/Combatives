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

## Player-local lean basis and Flan's armour

Gameplay lean keeps the semantic contract `lean < 0 = player left` and `lean > 0 = player right`. `PlayerLocalBasis` is the canonical Minecraft-yaw conversion used by both wall clamping and interaction/camera origins. Its right vector is `(-cos(yaw), -sin(yaw))` (with forward `(-sin(yaw), cos(yaw))`). Therefore the expected horizontal vectors are:

| Yaw | LEFT (X, Z) | RIGHT (X, Z) |
| --- | --- | --- |
| 0° | (+1, 0) | (-1, 0) |
| 90° | (0, +1) | (0, -1) |
| 180° | (-1, 0) | (+1, 0) |
| 270° / -90° | (0, -1) | (0, +1) |

The authoritative ray uses the server player's current `rotationYaw`. The interpolated client ray and camera projection use the same interpolated yaw and position, preventing current-tick and render-tick coordinate spaces from being mixed.

Flan's `ItemTeamArmour#getArmorModel` returns a reusable `ModelCustomArmour`. That class extends `ModelBiped`, but overrides `render`: it calls inherited `setRotationAngles`, then copies each biped parent part's rotations and pivots into its `ModelRendererTurbo` head/body/arm/leg arrays before rendering them independently. Thus the normal `ModelBiped.render` return hook does not run for Flan's armour. Combatives applies its one shared biped lean pose before those copies and a narrow optional Flan's adapter restores the captured animated biped state when the custom render returns. This supports armour using Flan's `ModelCustomArmour` architecture without item-name checks, leaves custom child geometry/animation intact, and prevents reusable model state from accumulating lean.

Sliding remains disabled by default and is unchanged by this compatibility path.
