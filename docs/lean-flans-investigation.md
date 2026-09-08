# Lean direction and Flan's investigation

## Findings recorded before implementation

Source inspected: this workspace and `referenceSRC/FlansmodSRC/src/main/java`.
These are source-level findings, not a claim of an in-game reproduction.
`version.properties` was already modified when investigation began.

### Input through gameplay geometry

`ClientMovementInputHandler.handleLeanInput` maps left to -1, right to +1,
both/neither to 0, predicts locally and sends `PacketLeanState` on change.
The handler validates locomotion and applies that scalar to
`EntityPlayerMixin.combativesLean`. `PoseSync` and `PacketPlayerPoseS2C`
broadcast it to the owner and observers; vanilla movement packets carry yaw.
There is no cardinal-direction conversion in the lean packet.

`InteractionRay.authoritative` uses current entity `rotationYaw/rotationPitch`,
the accepted AABB floor and effective eye height. Its interpolated counterpart
wraps yaw deltas across 180 degrees. `LeanGeometry.legalOffset` uses
`PlayerLocalBasis`, traces from the unleaned eye to the desired eye, and clamps
the displacement at a wall with a 0.05-block margin. The player movement AABB
is not translated by leaning. Moving that box would change movement, feet,
and the ray anchor, and is not a direction fix.

For Minecraft yaw y, forward=(-sin(y), cos(y)) and right=(-cos(y), -sin(y))
in (X,Z). The existing helper is correct for every yaw. Right is unit length,
orthogonal to forward, and left=-right. No cardinal branches are needed.

| Facing | Yaw | Left (X,Z) | Right (X,Z) |
|---|---:|---|---|
| North | 180 | (-1,0) | (1,0) |
| South | 0 | (1,0) | (-1,0) |
| East | -90 | (0,-1) | (0,1) |
| West | 90 | (0,1) | (0,-1) |
| NE | -135 | (-q,-q) | (q,q) |
| SE | -45 | (q,-q) | (-q,q) |
| SW | 45 | (q,q) | (-q,-q) |
| NW | 135 | (-q,q) | (q,-q) |

q=1/sqrt(2). Multiply each vector by accepted lean distance.

### Confirmed camera faults

`EntityRendererMixin` invokes `CameraController.applyTransforms` at
`orientCamera` TAIL. OpenGL post-multiplies: vanilla yaw/pitch are already in
the matrix. Adding the scalar tactical offset to `glTranslatef`'s X argument
there translates world X, not camera-local right. It also translates the
world rather than applying the inverse world displacement of the camera.
Likewise the tactical Z rotation there is world-Z rotation, not view roll.
This explains heading-dependent camera/server disagreement without blaming
the correct common-side trigonometry. Integrated and dedicated servers use
the same ray code; latency can expose the mismatch but is not its sign cause.

The camera additionally smooths accepted displacement independently of the
gameplay ray, which takes effect immediately. Turning or approaching a wall
can therefore leave the camera at a different origin. Disabling the cosmetic
camera also disables that displacement although gameplay lean remains active.

### Model and held-item faults

`ModelBipedMixin` adds `LeanVisualPose` at `setRotationAngles` TAIL and restores
it at `ModelBiped.render` RETURN. Held items render later and call arm
`postRender`, so they consume already-restored arms. This affects vanilla
items and Flan's main and off-hand guns. Repeated `setRotationAngles` calls
without `render` also retain the captured base and can accumulate lean.
Biped roll is relative to `renderYawOffset` while gameplay lean is relative
to entity yaw; idle head/body separation consequently changes visual direction.

### Flan's paths

* `ModelCustomArmour.render` overrides `ModelBiped.render`, calls inherited
  `setRotationAngles`, copies pivots/angles into Turbo arrays, then independently
  renders them. Skirts consume leg pivots/angles too. The existing adapter's
  RETURN cleanup is appropriate, but is registered as
  `mixin.compat.flans.FlansCustomArmourMixin` although its actual package is
  `compat.flans`. Moreover, the active early loader never offers it. Optional
  mod targets belong in the late loader after mod discovery.
* Main-hand `RenderGun.renderItem(EQUIPPED)` inherits vanilla right-arm
  `postRender`; `FlansModClient.renderOffHandGun(Specials.Post)` explicitly
  calls the left arm's `postRender`. `setupGunRender/renderEquippedMovement`
  adds content-pack weapon transforms inside that matrix. Neither needs a
  second Flan's lean angle; preserving the parent pose fixes both.
* First-person `renderItem(EQUIPPED_FIRST_PERSON)` enters through Forge's
  item renderer. `renderEquippedFirstPersonMovement`, `renderGunModel`,
  attachments, recoil/reload/ADS and `RenderArms` use gun-local transforms.
  They do not reconstruct world yaw. Apply view roll once at the shared hand
  boundary; applying third-person biped lean to these arms would double it.
* `ItemGun.shoot -> ItemBullet.getEntity -> EntityBullet`'s handheld
  constructor creates an origin from `posY + getEyeHeight()` without lateral
  lean. Its yaw/pitch math has the correct Minecraft signs. The explicit
  origin constructors also serve mounted guns, vehicles and bombs and must
  not be broadly replaced.
* `PacketGunFire.handleServerSide` temporarily replaces player yaw/pitch
  with packet fields. This is independent aim ownership and can bypass the
  server's accepted movement orientation.
* `PlayerSnapshot` independently builds lag-compensated body/head/arm/shield
  hitboxes from `renderYawOffset/rotationYawHead`, with a local-client-only
  1.6-block anchor subtraction. This is separate from the vanilla movement
  AABB and must be considered when checking bullet hits on a leaning target.
* `ItemGun` also reconstructs deployable placement rays, melee paths and
  lock-on directions; these are distinct from handheld bullet creation.
  Vehicle camera/render paths own their own matrices; leaning is disallowed
  while mounted. Content-pack gun transforms are animation, not player yaw.

The supplied HMG context describes a consumer-side integration; no HMG
adapter or public point-of-aim method is present in this workspace. The
available common authority is `InteractionRay`.

## Intended narrow corrections

Reuse `PlayerLocalBasis` and `InteractionRay`; remove tactical lean from the
world-X cosmetic transform, apply inverse accepted world offset at camera
TAIL and view roll before vanilla orientation. Keep cosmetic bob/shake intact.
Preserve visual lean through held-item rendering and restore before the next
angle calculation. Use entity yaw consistently for the leaning render basis.
Load optional Flan's hooks late, with no Flan's compile dependency; adapt
handheld shot origin and accepted orientation without replacing spread or
vehicle trajectories. Keep findings about independently rebuilt hitboxes and
other rays explicit in the final validation notes.

## Implementation and additional trace results

The remote-player mixin only adjusts the vertical anchor; it does not rewrite
yaw. Server `processPlayer` hooks are diagnostic and preserve vanilla movement
yaw acceptance. `ServerInteractionTarget` uses the common ray for digging and
block use, and `EntityLivingBaseMixin.rayTrace` uses its interpolated equivalent.

Further tracing found two lifecycle details: Flan's first-person arm helpers
instantiate a biped and call `setRotationAngles` directly; both must skip the
third-person lean while the shared hand roll is active. Also, SimpleImpl packet
handlers were applying lean/pose on networking threads. Pending lean requests
now coalesce per player and are accepted on server tick START; replicated poses
are applied on the client thread. This adds no cached yaw or new wire format.

The final changes are:

| Class | Changed execution path |
|---|---|
| `movement.PlayerLocalBasis` | Shared wrapped entity-yaw interpolation, retaining the correct lateral basis. |
| `interaction.InteractionRay` | Both aim direction and lateral displacement now derive from the same yaw basis. |
| `movement.LeanGeometry` | Exposes wall-limited semantic lean for visual/snapshot consumers. |
| `client.camera.CameraController` | Removes tactical lean from world-X translation, world-Z rotation and cosmetic smoothing; other effects retain their existing behavior. |
| `client.camera.TacticalLeanCamera` (new) | Applies view roll before orientation, inverse legal world displacement afterwards, and tracks the first-person hand scope. |
| `mixin.EntityRendererMixin` | Calls those camera boundaries, uses wrapped yaw for first-person lean, and applies shared roll once to the hand path. |
| `mixin.ModelBipedMixin` | Restores lean before recomputing angles, retains it for held items, includes headwear, uses wall-limited lean, and suppresses third-person pose in the hand scope. |
| `mixin.RendererLivingEntityMixin` | Reads entity yaw for both body and head while leaning without mutating entity fields; restores models after equipment rendering. |
| `network.message.PacketLeanState` | Queues/clamps semantic requests for tick-thread validation and replication. |
| `network.PoseSyncEvents` | Drains lean requests on server tick START. |
| `network.message.PacketPlayerPoseS2C` | Applies replicated state on the client thread. |
| `loading.CombativesLateMixins` | Selects common Flan's hooks only when `flansmod` is loaded; selects armour only on clients. |
| `compat.flans.FlansCustomArmourMixin` | Keeps RETURN cleanup, adds the production SRG render-method name. |
| `compat.flans.FlansBulletMixin` (new) | Redirects only the handheld constructor's eye-origin factory to the authoritative ray. Existing yaw/pitch, spread, speed and explicit vehicle origins remain intact. |
| `compat.flans.FlansGunFireMixin` (new) | Prevents the gun-button packet's temporary yaw/pitch writes, so shooting consumes accepted player orientation. |
| `compat.flans.FlansSnapshotMixin` (new) | Aligns leaning snapshot head/body yaw to the same entity yaw and invokes shared-pose adaptation. |
| `compat.FlansSnapshotPose` (new) | Reflectively applies existing pose roll to cloned snapshot axes and leg brace translation; fixes the leaning snapshot's floor anchor. |
| `compat.flans.FlansItemGunMixin` (new) | Lean-aware deployable ray, lock-on origin/direction and lateral melee anchor. These Ultimate-specific secondary hooks are optional (`require=0`); deployable cardinal placement rules and pack melee animations remain intact. |
| `movement.PlayerLocalBasisTest` (new test) | Checks all requested headings and a continuous yaw sweep, handedness, orthogonality, distance projection, inverse camera matrix and seams. |

`mixins.combatives.compat.late.json` is new and has the actual optional mixin
package. The invalid armour entry was removed from
`mixins.combatives.client.json`. `build.gradle` adds `verifyLeanBasis` to
`check`, so future successful builds run the mathematical regression checks.
All Java class names above are relative to `com.glowingfederal.combatives`.

The common snapshot adapter uses the existing `LeanVisualPose` data class,
which has no client dependencies despite its package name. It resides outside
the protected mixin package. No Flan's imports/dependency were added; reflective
layout failure logs once and disables snapshot adaptation. Optional overridden
Minecraft methods explicitly list development and SRG names because `@Pseudo`
does not automatically map those target method names. Minecraft references
inside the injections are included in the generated refmap.

## Validation performed

* PASS: standalone test against production `PlayerLocalBasis`: eight headings,
  both sides, 5,761 yaw samples (-720 through 720 in 0.25-degree steps), five
  signed distances per sample, inverse camera translation and wrap seams.
* PASS: all main Java sources compile with Java 8 against locally cached
  dependencies, using `javac -proc:none` and output under
  `build/lean-compile-check`. This is a source/API check, not a production jar.
* PASS: separate annotation-processing check with the project's Mixin 0.7.11
  processor and MCP-to-SRG mappings. The generated refmap contains the new
  Minecraft field/invocation targets for the optional hooks.
* Full Gradle build: attempted with Java 8 and `build --offline --no-daemon`.
  It failed in `compileJava` with `GC overhead limit exceeded`. The request to
  retry with a 2 GB Gradle heap was declined. No successful production build,
  reobfuscation or packaged runtime validation is claimed. The failed build
  restored the pre-existing build number (2); the user's pre-existing
  `version.properties` change remains.
* No singleplayer or dedicated-server game was launched during this task.

To finish packaged validation, the pending command is:

```powershell
./gradlew.bat build --offline --no-daemon '-Dorg.gradle.jvmargs=-Xmx2G'
```

Use Java 8 and the existing Gradle cache. A successful build will run
`verifyLeanBasis`, reobfuscate, and create the project's normal artifacts.

## Manual test matrix (not yet executed)

Run **every heading and both sides from the direction table above** in all
four configurations below. Add a stationary second client to observe pose and
gun alignment on the dedicated server; use third-person and an integrated
LAN observer for singleplayer replication checks.

| Environment | Mods | Startup check |
|---|---|---|
| Singleplayer/integrated | Combatives without Flan's | No optional targets attempted; no missing-class errors. |
| Dedicated + two clients | Combatives without Flan's | Server loads no client camera/model mixins. |
| Singleplayer/integrated | Combatives + supplied Flan's Ultimate version/content packs | Late-loader log lists common hooks and client armour hook. |
| Dedicated + two clients | Same Combatives/Flan's versions | Common hooks on server; armour only on clients; no injection/refmap errors. |

| Scenario at each heading | Actions | Required observation |
|---|---|---|
| Stationary open space | Left, release, right, release; then both keys | Eye offset follows the direction table; both keys cancel; no residual model roll. |
| Continuous turning | Hold each side and turn 360 degrees slowly and rapidly; cross +/-180 | No sign flip, fixed world-X motion or wrap-seam orbit. Camera, ray and observer agree on side. |
| Head/body separation | Stand still, turn view without moving feet, then lean | Lean follows view/entity yaw, including on the observer; no body-yaw-dependent reversal. |
| Cover on either side | Lean into a full block wall, corner and partial cover; turn while holding lean | Eye displacement stops at the same ray-traced obstruction; no camera-only interpolation through the wall. The foot movement AABB stays fixed. |
| Aim/use | Mark a target, dig, place and use blocks while leaning | Client highlighted block and server interaction log agree once orientation/state packets arrive. |
| Config | Disable cosmetic camera; disable camera rotations; try max distance 0 and 0.5 | Gameplay displacement follows server settings; no tactical displacement when maximum is 0; rotation toggle removes roll only. |
| State transitions | Sneak, crawl, swim, mount, sleep, die/respawn, reconnect while holding/releasing keys | Server rejects disallowed lean and replication clears it; no stale lean/model pose on the next player render. |
| Flan's custom armour | Render every equipped slot and skirts; enchanted passes; alternate leaning and neutral players | Turbo arrays follow parent pose with no accumulation or leaked state. |
| Third-person guns | Main hand, off hand, dual wield, reload, ADS, attachments | Both guns remain attached to the leaned arms through vanilla/Flan's Specials rendering. No second hardcoded gun roll. |
| First-person guns/arms | Hip fire, ADS, scope overlay, reload/pump/charge, both hands | Gun and independently created arms receive the shared hand roll once; no extra biped lean or clipping caused by double roll. |
| Flan's firing | Fire at near and distant targets and around both corners while turning | Handheld projectiles start from the legal common origin and use accepted yaw/pitch; spread remains normal. Test packet latency explicitly. |
| Flan's target hitboxes | Enable Flan's snapshot debug view; shoot head/body/arms/shields of a leaning observer | Snapshot yaw/roll follows the rendered side and its anchor has no local-client-only height jump. |
| Flan's secondary paths | Deploy a gun, lock on and melee while leaning at cover | Placement/lock-on consume lean-aware rays, melee shifts laterally; pack animation and deployable cardinal placement remain as before. |
| Regression | Release lean; walk/strafe, jump, crawl, swim and use mounted guns | Existing locomotion, no-lean rendering and explicit mounted bullet trajectories behave as before. |

### Practical limits to assess in-game

Network latency still separates prediction, server acceptance and observer
interpolation; these changes share conventions, not instantaneous delivery.
Flan's snapshot format has one combined leg box, so it cannot express the two
opposite per-leg brace angles. The adapter uses the shared leg roll and pivot
offset without inventing a second leg-hitbox architecture. Its existing
coarse hitbox dimensions and pack/model scaling remain Flan's responsibility.
The model roll is an anatomical visual pose; the gameplay lean is an
eye-origin displacement, not a rigid translation of the whole player AABB.
The existing collision check is a point ray, not a swept head-volume test.
Other Flan's forks or mods replacing Forge's hand-render entry point need
runtime testing; the source trace here covers the supplied reference version.
