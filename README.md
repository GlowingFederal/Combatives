# Combatives

Combatives is a source-available movement and first-person camera overhaul for
Minecraft Forge 1.7.10. It brings modern swimming and prone/crawling movement
to the older game while adding physical, movement-responsive camera feedback.

The project is designed to preserve ordinary Minecraft gameplay and multiplayer
pose authority rather than replace them: movement and poses are synchronized,
and the camera layer presents motion without intentionally changing mouse
sensitivity, aiming input, player rotation, reach, or other gameplay mechanics.

## Features

- **Modern swimming** with water movement, a swimming pose, adjusted collision
  bounds, eye height, and third-person animation.
- **Crawling / prone movement**, toggled with `C` by default, including
  standing-clearance checks and matching collision and visual poses.
- **Multiplayer pose synchronization** between the owning player, server, and
  tracking clients across normal lifecycle changes.
- **A physical first-person camera** with procedural movement bob, lean, and
  movement-responsive FOV.
- **Action feedback** for landing, freefall, momentum changes, collisions, and
  nearby explosions.
- **Entity-aware camera behavior**, including speed- and terrain-responsive
  horse riding. The extensible provider system also lets other mods describe
  camera behavior for their own entities.
- **Compatibility-oriented player geometry** that keeps camera, collision, and
  interaction origins aligned and avoids taking ownership of vanilla mount and
  dismount placement.

Camera effects are presentation only and are bounded by the camera system's
saturation and hard clamps. The crawl-specific camera cycle is **experimental**
and disabled by default; crawling itself is not dependent on that effect. An
optional, default-off raw mouse-delta safety clamp exists for diagnosing
pathological input spikes, but is separate from normal camera effects.

## Requirements and compatibility

| Component | Requirement |
| --- | --- |
| Minecraft | **1.7.10** only |
| Minecraft Forge | Built against **10.13.4.1614** |
| UniMixins | **Required**; the build expects `unimixins-all-1.7.10-0.3.1` |
| Java | Java 8 is the project's source and target level |

- Combatives uses UniMixins/GTNHMixins to side-gate its common and client
  mixins. Do not remove UniMixins from either a player installation or the
  development environment.
- **MorePlayerModels / MorePlayerModels+** is an optional integration, not a
  dependency. When FML detects its `moreplayermodels` mod ID, Combatives can
  align supported whole-model/entity-disguise geometry and restore the
  first-person item pass for entity disguises. If the optional targeting hook
  does not match a changed MPM build, MPM retains its own targeting behavior.
- Mounted pose handling uses Minecraft's generic riding relationship rather
  than hard-coded mount classes. Rideable mods remain responsible for choosing
  valid dismount positions.
- Compatibility is deliberately defensive, but universal mod compatibility is
  not claimed. See the [player geometry and MPM compatibility notes](docs/player-geometry-and-mpm-compatibility.md)
  and [riding/crawling camera notes](docs/riding-and-crawling-camera.md), plus the
  [sleep/crawl render lifecycle audit](docs/sleep-pose-and-camera-lifecycle.md)
  for the currently documented boundaries, including the An Extra Touch camera
  composition analysis.

## Configuration

Forge creates the standard configuration at `config/Combatives.cfg`. Its main
categories cover:

- camera features and strength multipliers (bob, lean, FOV, landing, freefall,
  inertia, collision, explosion, horse riding, and experimental crawl motion);
- camera safety limits and the optional raw mouse-delta clamp;
- MorePlayerModels+ physical hitbox scaling; and
- general or verbose movement/camera diagnostics.

The separately distributed Fairplay build uses `Combatives-Fairplay.cfg`; it
locks gameplay and camera values to project defaults and exposes only general
and verbose diagnostics.

## Developers and contributors

This is an old-style ForgeGradle 1.2 project. Keep its versions intact: do not
upgrade Gradle, ForgeGradle, mappings, Java, Mixins, or Forge as incidental
cleanup.

### Set up and build

1. Install a **Java 8 JDK** and clone the repository.
2. Place `unimixins-all-1.7.10-0.3.1.jar` in `libs/`; `build.gradle` resolves
   that local flat-directory dependency by name.
3. Use the included Gradle 4.4.1 wrapper. Run `./gradlew setupDecompWorkspace`
   when a ForgeGradle workspace must be prepared, then `./gradlew idea` or
   `./gradlew eclipse` if the corresponding IDE metadata is wanted.
4. Build the production artifacts with `./gradlew build`. Production builds
   increment the build number and create the normal and Fairplay jars under
   `build/libs/`.

Useful entry points:

- `src/main/java/com/glowingfederal/combatives/` contains the mod, movement,
  swimming, crawling, networking, configuration, compatibility, camera
  implementation, and mixins.
- `src/main/java/com/combatives/api/camera/` contains the **public Camera API**.
  Code under `com.glowingfederal.combatives.client.camera.internal` is an
  implementation detail, not an integration surface.
- `src/main/resources/` contains the FML metadata and side-specific mixin
  configurations.
- `docs/` contains focused engineering notes, including the
  [Aqua port map](docs/aqua-port-map.md),
  [DataWatcher 28 compatibility investigation](docs/datawatcher-28-compatibility-investigation.md),
  [interaction-geometry contract](docs/authoritative-interaction-geometry.md),
  and [camera documentation](docs/camera-api.md).

Compatibility work should preserve vanilla/client-server ownership, use
optional and side-safe boundaries, and fail back to the other mod's native
behavior when a hook cannot be applied. Consult the relevant audit in `docs/`
before changing player geometry, targeting, riding, or MPM behavior. Do not
compile or commit material from `referenceSRC/`; it is reference and attribution
material rather than active Combatives source.

## Combatives Camera API

Other mods can contribute camera intent without modifying Combatives' renderer
internals. API version 1 is rooted at
`com.combatives.api.camera.CombativesCameraAPI` and supports:

- tuned preset effects such as explosions, landings, impacts, weapon fire,
  vehicle collisions, environmental rumble, and suppression;
- namespaced custom impulses with pitch/yaw/roll, translation, FOV, timing,
  decay, priority, stacking, and optional positional falloff;
- continuous effects with handles that can update strength or position, be
  enabled/disabled, and be stopped; and
- entity camera providers registered by matcher/factory, with motion samples
  and sinks for per-frame, impulse, and continuous contributions.

Check `CombativesCameraAPI.getApiVersion()` and `getCapabilities()` at runtime
rather than inferring support from the mod version. Start with
[`docs/camera-api.md`](docs/camera-api.md) for effect examples and
[`docs/entity-camera-behaviors.md`](docs/entity-camera-behaviors.md) for custom
mount/entity providers. Public integrations should depend on
`com.combatives.api.camera` and its `entity` subpackage, never the internal
camera manager or render mixins.

## Credits and acknowledgements

- **GlowingFederal** — Combatives author.
- **Aqua Acrobatics Legacy** — public-domain reference foundation for the
  swimming and crawling port. It is not a runtime dependency.
- Minecraft Forge, FML, UniMixins, Sponge Mixin, and other included reference
  material retain their own notices and upstream licenses. See
  [`CREDITS-fml.txt`](CREDITS-fml.txt),
  [`MinecraftForge-Credits.txt`](MinecraftForge-Credits.txt), and the license
  files accompanying the relevant material.

## License

- **Combatives core** is **source available** under the
  [Combatives Source Available License](LICENSE.txt). It is not described as
  open source; redistribution and derivative works are restricted by that
  license.
- **Combatives Camera API** — the files explicitly comprising the public API
  under `src/main/java/com/combatives/api/camera/` — is separately licensed
  under the **Apache License 2.0**, allowing other mods to use and integrate
  with that API under its terms.
- Third-party and reference code remains governed by its respective upstream
  licenses and notices.
