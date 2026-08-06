# Entity camera behavior architecture

## Data flow

The new layer follows this ownership path:

`mounted entity -> EntityMotionSampler -> EntityCameraBehavior -> CameraEffectSink -> CameraEffectManager -> CameraController -> render mixins`

The sampler owns observation, providers own interpretation, the sink owns provider-facing collection, the effect manager owns validation/accumulation/saturation, the controller owns final state, and mixins own rendering. The framework neither manipulates OpenGL nor changes rider rotation.

## Components

- **Registry and matchers:** A runtime, multi-registration registry selects entities by exact class, assignable class, registry identifier, or any custom matcher. It returns all matches rather than selecting a single winner.
- **Behavior factory:** Creates an independent provider instance for each matching registration on each mounted-entity lifecycle.
- **Lifecycle manager:** Detects attach, mount changes, detach, and once-per-tick versus render updates. It also reconciles registrations added or removed while mounted.
- **Mount context:** An immutable generic view of rider, mount, previous mount, transition, timing, partial ticks, and motion. It intentionally contains no entity-mod-specific concepts.
- **Motion sampler/sample:** Maintains the short derivative history needed for velocity and acceleration, records orientation/angular velocity and timestamps, identifies discontinuities, and creates render-interpolated observations.
- **Effect sink:** Accepts provider intent and delegates it to the existing manager. Frame contributions are cleared after each manager update; impulses and continuous effects retain the existing API lifecycle.

## Compatibility and future use

No behaviors are registered by this change, so landing/explosion feedback, movement lean, procedural bob, continuous effects, custom impulses, the Camera API, and mount pose compatibility retain their prior paths and output. Future horse, boat, aircraft, tank, seat, or unknown-mod integrations should be isolated provider registrations rather than controller edits. Optional integrations can match registry names or custom predicates without linking their entity classes.
