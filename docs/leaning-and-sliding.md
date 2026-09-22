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

The fixed movement footprint is not used as the player combat narrow phase.
Standing, crouching, crawl, and swim keep their existing centered movement
AABBs for tunnels, wall collision, mounts, and MC Heli handoff. Combat rays use
three oriented volumes derived from the rendered vanilla biped instead: head,
torso, and a combined pelvis/lower-body core. Each ray is transformed from
world space into a volume's local orthonormal basis and intersected with its
local AABB. This represents lean roll and the horizontal prone silhouette
without introducing oriented boxes into world physics.

## Pose-aware combat-volume transform

The volume source dimensions are the rendered model cubes after
`RenderPlayer`'s `0.9375` scale: head `8 x 8 x 8` model pixels, torso
`8 x 12 x 4`, and a combined lower core `8 x 12 x 4`. The combined core spans
the two legs' shared envelope; independently animated arms and legs are not
added because their render-time gait matrices are not authoritative server
state.

Transform composition follows the renderer's OpenGL order. In application
order (rightmost operation first), it is:

```text
world floor/interpolated position
  * interpolated body yaw (view yaw while leaning)
  * land-crawl world-down grounding
  * blended prone/swim root pitch
  * full prone local translation
  * crouch render drop
  * pelvis-centered lean translation and roll
  * part pivot, head yaw/pitch or crouch torso pitch
  * local box center
```

The model conversion uses one model pixel = `0.9375 / 16` blocks and the
vanilla `24 / 16 + 0.0078125` baseline before scale. Renderer-local `+Z` is
player-local backward after the vanilla `180 - yaw` rotation, so the injected
`-90` degree render pitch becomes `+90` degrees in the combat volume's
right/up/forward coordinates. This is why prone head/core centers extend along
the player's forward/back axis instead of the centered `0.6 x 0.6` movement
box.

Lean reuses `LeanPoseMath`: roll is `-0.16 * acceptedLean` radians and pelvis
shift is `0.45 * acceptedLean` model pixels. Positive lean therefore moves the
head/torso along `PlayerLocalBasis.right`, while negative lean is its mirror.
The basis is evaluated from the same interpolated view yaw used to render a
leaning body, so cardinal and diagonal headings do not introduce a second sign
conversion. Client selection uses render-timeline target position/yaw; server
projectiles and Flan snapshots use current-tick state.

Vanilla client entity selection, arrows, throwables, fireballs, and fishing
hooks retain their existing broad-phase searches but replace the player-only
narrow intersection with these volumes. Flan snapshots replace HEAD, BODY, and
LEGS boxes with the same volume centers, axes, and local bounds; unsupported
animated arm boxes are removed, while Flan-owned item/shield boxes remain.

The first-person transforms intentionally do not reuse identical numeric signs.
The render collision sample is based on vanilla's `lastTickPos` interpolation,
but the resulting tactical displacement remains a player-relative vector. The
view matrix translates the world by the negative of that vector after vanilla
yaw and pitch; it never recovers the vector by subtracting absolute world-space
camera coordinates. The roll is installed before vanilla orientation and likewise rotates the
world: its OpenGL angle uses the semantic lean sign so that the resulting visible
camera orientation is the inverse, matching the requested side. Model-part roll
is a model-space pose instead and retains its existing opposite scalar sign.

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

The local client predicts the wall-limited value each tick for responsiveness;
the server resolves it from the current-tick eye and broadcasts the resolved
amount whenever it changes. Remote render and combat geometry consume that
resolved amount instead of repeating a potentially different local wall trace.
The shared `LeanVisualPose` rolls the
torso and its animated head/shoulder/hip attachments around the pelvis, with a
small pelvis shift toward the supporting leg. The opposite leg counter-braces
while the near leg follows slightly; a small hip overlap closes the rigid-cube
seam. Left/right poses mirror each other. Existing walk and ADS pitch/yaw
animation is untouched. Cosmetic pivot and leg-roll adjustments do not change
the established values consumed by Flan's gameplay hitbox snapshots.
Every changed angle and X/Y pivot is captured after vanilla
animation and restored after the model render, including armor `ModelBiped`
instances, so transforms do not accumulate.

The pelvis and part transforms remain in `ModelBiped` local space. While lean is
accepted, `RenderPlayer` uses interpolated view yaw as the one model-to-world
orientation; combat volumes use the same yaw. Neutral players retain vanilla
interpolated body yaw. No yaw is baked into model-local limb pivots.

Dedicated-server, latency, modded-block collision, and animation appearance
still require in-game validation; this implementation was validated by source
inspection and call-path tracing only.

Authoritative pose packets carry pose, swim/crawl state, locomotion state,
requested lean, and the server-resolved wall-limited lean to the owner and
tracking clients; vanilla entity updates continue to own yaw. The separate
geometry packet carries server-resolved MPM scale/revision.
Both packet paths apply on the client thread. Once the server has accepted a
pose or scale, client reconstruction bypasses local expansion clearance so a
tracker cannot render the new pose while retaining an older AABB. Ordinary
local prediction and server pose selection still use the existing obstruction
checks. Lifecycle resets preserve each concrete 1.7.10 player subclass' own
`yOffset` convention while rebuilding the box from its stable floor.

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

## Optional jump restrictions

`JumpRestrictions.register(String, Predicate<EntityPlayer>)` accepts common-side
policies during mod initialization. Providers must own their server policy and
use synchronized data for client prediction; predicates must not mutate movement
or pose. Combatives' existing `EntityLivingBase.jump()` HEAD hook evaluates these
policies and still performs crawl-key/standing-clearance handling before returning.
An allowed jump keeps vanilla vertical velocity. This API has no optional-mod
class references and does not change horizontal shaping or pose authority.

Updated HMG registers its weapon-weight policy reflectively. Combatives then owns
its jump cancellation; HMG's standalone coremod branch becomes a no-op. With no
compatible API, HMG retains its own pre-jump fallback. HMG supplies server mobility
snapshots, heavy rejection and medium landing delays, and bypasses active flight.
It no longer clamps velocity or teleports players to undo jumps. Light weapons
and Combatives-only operation retain their existing jump path. HMG policy sync
requires matching updated HMG builds on both sides.

For reproduction, enable `verboseMovementDebug` and HMG's `-Dhmg.jumpTrace=true`
on both JVMs. HMG records jump-hook ownership/rejection and weight/timer values;
Combatives vertical traces include logical side, tick, flight and airborne flags.
In-game transformer ordering, creative flight/landing, crawl plus heavy weapons,
standing clearance, switching, and dedicated/integrated prediction remain to be
validated. The hook does not add validation of vanilla C03 position reports.
