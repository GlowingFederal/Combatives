	(PR: Restore packet-owned held interactions)

	- Confirmed that holding a mouse button can produce later vanilla controller
	  calls and a second C07 or C08 packet after the first action changes the
	  current client target; one ray trace still owns only one target.
	- Removed all server redirects that replaced C07/C08 coordinates, faces, and
	  hit values with a later independent server ray, keeping complete digging
	  sequences and placement packets bound to their client-selected targets.
	- Kept vanilla survival mining continuation, right-click delay, reach,
	  protection, build-height, game-mode, harvest, air-use, vehicle targeting, and
	  bounded post-dismount behavior unchanged.
	- Added verbose phase/tick diagnostics at controller, packet send, and packet
	  receive boundaries; the server ray is now read-only diagnostic data and is
	  only calculated when verbose diagnostics are enabled.

	2026-09-13 18:47 — Preserve mount dismount ownership and resynchronize movement state

	- Prevented expected rider/mount overlap from becoming Combatives' forced-crouch `isSneaking()`
	  result. While riding, only the real movement input is exposed, allowing MC Heli to suppress it
	  during its native hold-to-dismount window while vanilla mounts retain normal Sneak dismounting.
	- Made the existing generic riding transition clear crawl, swim, slide, lean, movement snapshot,
	  and pose-animation state on both sides. Dismount now rebases interpolation and camera history to
	  the player coordinates already selected by the mount without calling a positioning method,
	  changing the AABB, checking a mod class, or implementing a dismount timer.
	- Updated mounted-ownership documentation and the manual regression matrix. Java 8 `compileJava`
	  and Mixin annotation processing passed; repeated MC Heli/vanilla mount behavior and
	  integrated/dedicated in-game synchronization still require runtime validation.

	2026-09-13 23:38 — Follow mount-owned position writes through dismount completion

	- Confirmed MC Heli may apply its configured custom exit with `setPosition` after detaching and
	  repeat that authoritative placement for five vehicle ticks. The earlier one-shot dismount reset
	  could therefore leave Combatives history behind a later MC Heli-owned write, exposed by jumping
	  or entering flight.
	- Added a generic post-dismount observer for player `setPosition` calls. While position writes keep
	  arriving, Combatives rebases entity interpolation/walk history, clears its derived movement
	  snapshot, and revision-resets the client camera motion sampler to the position already accepted
	  by the entity; two quiet player ticks close the observer. No mount class, exit coordinates, key
	  timing, position, AABB, velocity, or ground state is overridden.
	- Audited current HMG jump handling: it only rejects at jump entry and observes landing cooldowns,
	  and explicitly bypasses riding and flight without position, velocity, or ground-state writes.
	  Java 8 `compileJava` and Mixin annotation processing passed. In-game MC Heli/HMG/vanilla-mount
	  validation remains to be performed.
	2026-09-14 04:17 — Give the early loader exclusive interaction-hook ownership

	- Removed the duplicate static common/client mixin declarations from the legacy JSON configs. The
	  GTNHMixins `IEarlyMixinLoader` list is now the only registration source for vanilla client and
	  server hooks, preventing a second bootstrap path from applying the same player-controller,
	  targeting, movement, and packet handlers to one physical input.
	- Preserved the exact dynamically selected mixin set, side rules, vanilla hold behavior, targeting
	  corrections, crawl, lean, and optional-mod compatibility. Validation was limited to JSON parsing,
	  registration-source searches, and call-site tracing; single-click and hold behavior still require
	  the requested integrated/dedicated in-game matrix.

	2026-09-20 — Fix MC Heli dismount collision, targeting, and falling handoff regressions

	- Added a cached, optional MC Heli collision boundary which distinguishes composite damage/ray
	  boxes from physical movement collision. Aircraft composite boxes no longer reject pose
	  clearance; real blocks, unrelated entity collision, and ship deck collision remain solid.
	- Exposed a transition-based common mount handoff state. Mounted and exit-position phases retain
	  vehicle camera/packet ownership, while Combatives targeting resumes only after the exit writes
	  become quiet. Both position observation and clearance fallback are bounded to six player ticks.
	- Applied the ownership decision to both EntityLivingBase ray redirects and server C07/C08 target
	  selection. Start-dig and block-use retain their packet target during the unstable exit phase;
	  stop/abort digging and the C08 air-use sentinel remain unchanged.
	- Kept MC Heli's final exit placement authoritative and retained the 1.7.10 packet/AABB coordinate
	  contract. Once the bounded handoff closes, ordinary falling no longer rebases position history
	  in response to later corrections or participates in mount handoff logic.
	- Reconciled pre-existing changelog conflict markers while moving the project history to the
	  requested `CHANGELOG.md` filename.

	2026-09-20 09:05 — Correct the 1.7.10 digging-packet diagnostic hook

	- Corrected the C07 construction diagnostic to target Minecraft 1.7.10's five-integer constructor
	  instead of the newer nested `Action` enum API, restoring compilation without changing packet
	  coordinates, interaction ownership, MPM camera behavior, or HMG point-of-aim behavior.
	- Audited the interaction changes merged in `f881816`: controller and packet hooks only observe
	  state when verbose diagnostics are enabled, and server handling now preserves the complete
	  client-selected C07/C08 target rather than replacing it with a later server ray. MPM camera and
	  targeting transforms and HMG-selected aim therefore remain outside these hooks.
	- Source inspection and compilation validate the descriptor correction; MPM/HMG behavior still
	  requires integrated and dedicated-server in-game regression testing.

	2026-09-21 23:29 — Match combat hit volumes to rendered player poses

	- Traced the vanilla biped, crouch, prone/swim root, pelvis lean, legacy floor anchor, pose packets,
	  vanilla entity/projectile rays, and Flan's snapshot/fallback paths. Kept the centered movement AABB
	  for block physics, tunnels, mounts, and MC Heli, but stopped using it as player combat geometry.
	- Added shared model-local head, torso, and lower-core boxes with rigid transforms. World rays are
	  transformed into each local box, so crouch follows its model pivots, prone/swim extends along the
	  visible horizontal body, and lean rotates/translates the head and torso about the rendered pelvis.
	  Client selection uses target render interpolation; server projectiles use current-tick state.
	- Routed vanilla mouse-over, arrows, throwables, fireballs, fishing hooks, Flan snapshots, and Flan's
	  missing-snapshot bullet fallback through the shared volumes. Flan's animated arm boxes are omitted
	  because their client model matrices are not authoritative; item/shield records remain Flan-owned.
	- Split requested lean from the authoritative wall-limited amount, synchronized both to owner and
	  trackers, and made renderer, interaction rays, and combat volumes consume the resolved value. A
	  leaning body and its combat volumes share interpolated view yaw; neutral body yaw remains vanilla.
	- Preserved movement/camera ownership, pose geometry, mounts, and MC Heli collision handoff. Java 8
	  `compileJava --offline` and Mixin annotation processing passed. No game was launched; runtime
	  appearance, optional-mod injection, latency, and hit behavior still require in-game validation.
