# Leaning and sliding architecture

Minecraft 1.7.10 has no useful vanilla pose state for these features. Combatives
therefore keeps `Pose.SWIMMING` as the established low collision geometry while
`LocomotionState` distinguishes a crawl, swim, and time-bounded slide. The
server accepts slide entry from the existing crawl activation packet, changes
to low geometry immediately, owns deceleration and termination, and broadcasts
the locomotion value alongside the existing pose packet.

A slide requires enabled configuration, sprinting, ground contact, at least the
minimum horizontal speed, clear low geometry, and no water, ladder, flight,
mount, sleep, death, or noclip restriction. It inherits current horizontal
motion without a boost. Each tick removes the configured speed amount; normal
sprint acceleration is skipped and input may only blend the travel direction by
the configured, tightly capped steering influence without increasing speed.
It ends at the exit speed or duration limit, on ground loss, horizontal impact,
water/restricted-state entry, or knockback. A held crawl request ends in crawl;
otherwise existing standing clearance chooses standing or forced crawl.

Death, respawn, dimension change, and server teleport use the existing forced
pose reset, which now also clears slide ticks and lean. Mounts, sleep, flight,
ladders, water, and damage terminate the state through the ordinary lifecycle.
The low geometry continues through `PlayerGeometryResolver`, including optional
MorePlayerModels scaling; sliding does not add a parallel bounding box.

Lean is a normalized `-1..1` gameplay value. The client predicts key changes
for responsiveness, but the server validates compatible state and broadcasts
the accepted value. It is enabled while standing or crouching and disabled for
crawl, swim, slide, sleep, riding, flight, ladders, and death. Both common-side
`InteractionRay` and the first-person camera use the same lateral offset. A
center-to-desired-position block trace reserves a small wall margin and clamps
the offset, so the server's authoritative dig/use ray cannot originate beyond
the blocking wall. Lean never moves or resizes the player's collision box.

Client presentation interpolates lateral camera travel and modest roll. Slide
uses the existing physical low eye height plus a restrained entry settling
offset. Remote pose packets drive an asymmetric, non-stroking slide animation;
lean applies a small torso roll with head counter-roll. These visual blends do
not own collision or momentum.

Dedicated-server, latency, modded-block collision, and animation appearance
still require in-game validation; this implementation was validated by source
inspection and call-path tracing only.
