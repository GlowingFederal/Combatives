# Sleep, pose, camera, and crawl-render lifecycle

## Vanilla and Combatives ownership

In vanilla 1.7.10, `sleepInBedAt` first changes the player to the 0.2 by 0.2
bed size and then anchors the sleeping entity at the bed. `wakeUpPlayer` restores
0.6 by 1.8 geometry and the normal height offset before it asks the bed for a
safe exit and calls `setPosition` there. The restored height is therefore an
input to wake placement, not cleanup which can safely wait for a later tick.

Combatives owns gameplay pose and effective geometry. Its sleep `setSize`
redirect previously selected `SLEEPING` without applying that geometry. More
importantly, vanilla's wake `setSize` changed the live entity while the
Combatives caches still described the sleeping body. On the next pose update,
`isResizingAllowed` treated the vanilla standing dimensions as an unexplained
third-party mutation and refused to update the cached geometry. That left the
sleep eye height and bounding-box-relative legacy eye conversion alive after
waking. It also allowed wake placement to observe geometry from two different
owners, producing the low physical exit and permanent low camera symptoms.

The sleep redirect now commits sleeping geometry immediately. At the head of
`wakeUpPlayer`, Combatives clears crawl/swim requests and force-commits standing
physical and effective geometry before vanilla calculates its exit. Ordinary
collision-gated expansion is intentionally not used at this point: the entity
is still at the bed, and choosing a clear destination is precisely the work
vanilla performs next. At method return the gameplay eye conversion is sampled
again from vanilla's final `yOffset`, position, and bounding box.

The camera mixin does not retain a bed-derived world height. Every
`orientCamera` call derives its base from interpolated `posY`, the live AABB
floor relationship, and the applied effective eye height. The apparent camera
cache leak was downstream of the stale `combativesAppliedGeometry` and
`combativesLegacyEyeHeight`; restoring and resampling their owner lifecycle
returns standing, crouch, crawl, and interaction-ray origins to the same
effective geometry.

## Crawl model state

Vanilla computes the current frame's biped animation before Combatives adds the
land-crawl contribution. The crawl animator changes leg X and Z rotations, but
the shared `ModelBiped` instance survives the render and vanilla does not
guarantee that every axis is authored on every later animation path. The prior
frame's crawl Z could consequently become the next frame's apparent base.

The model hook now captures all three leg axes after vanilla animation and
before applying crawl. The crawl contribution is visible for that render only;
`ModelBiped.render` return restores the captured current-frame vanilla values.
It does not zero legs, so walking, sneaking, riding, jumping, and other vanilla
animation remain the base, including during an interpolated crawl fade.

## An Extra Touch compatibility audit

An Extra Touch targets `EntityRenderer.orientCamera` at `HEAD` and `RETURN`.
At `HEAD` it may temporarily substitute decoupled yaw/pitch. At `RETURN` it
applies third-person clipping/follow translations, an optional vertical
compatibility offset, additive camera-overhaul pitch/yaw/roll and shake, then
restores entity rotation. It does not target `RenderPlayer` or `ModelBiped`.

Combatives derives the physical camera origin inside vanilla `orientCamera`
and adds procedural transforms at `TAIL`. Same-point mixin ordering is not a
public cross-mod contract, but both return hooks multiply incremental GL
transforms rather than assigning shared yaw/pitch/roll fields. Thus either
ordering composes a large-scale Combatives physical origin/motion transform
with An Extra Touch's secondary transform. Its temporary rotation is restored
after its return work and does not alter authoritative pose geometry.

**NO COMPATIBILITY CHANGE REQUIRED.** A hard dependency, feature suppression,
or mod-specific offset would make the composition less robust. Decoupled-camera
interaction remains owned by An Extra Touch when that optional mode is active;
ordinary first-person targeting remains owned by Combatives' effective
geometry and authoritative view-ray path.

## Transition invariants

- Standing/crouching/crawl/swim must first reach standing geometry before bed
  exit calculation; wake finishes at a 0.6 by 1.8 unscaled base (then optional
  gameplay scale), with the AABB floor and legacy `posY` invariant aligned.
- Waking and immediately entering crawl or crouch starts from resampled
  standing state, then performs the normal collision-gated pose transition.
- Crawl transitions to stand, walk, sprint, jump, crouch, or swim cannot reuse
  a crawl-authored leg axis because the render restores its vanilla base.
- Combatives and An Extra Touch camera transforms remain additive and neither
  changes server geometry or permanently owns the other's transient state.
