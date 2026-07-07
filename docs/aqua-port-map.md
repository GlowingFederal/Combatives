# Aqua Acrobatics Legacy swimming/crawling port map

Aqua Acrobatics Legacy is included in this repository as public-domain/Unlicense reference material. Combatives ports only the swimming/crawling systems needed for player movement and rewrites them under `com.glowingfederal.combatives`; reference sources are not compiled.

## Ported or rewritten for Combatives

| Aqua reference class | Combatives implementation | Notes |
| --- | --- | --- |
| `entity/EntitySize` | `entity/EntitySize` | Ported as a small value type for pose-dependent dimensions. |
| `entity/Pose` | `entity/Pose` | Ported pose enum values needed by swimming, crawling, sleeping, and death states. |
| `entity/player/IPlayerResizeable` | `entity/player/ICombativesPlayerPose` | Rewritten interface for Combatives pose, swim, crawl-key, eye-height, and collision APIs. |
| `mixins/early/minecraft/EntityPlayerMixin` | `mixin/EntityPlayerMixin` | Ported/refactored core pose DataWatcher sync, crawl/swim pose selection, collision resizing, standing-space checks, eye-height hook, swim animation, sprint swimming, and water movement. Aqua integration hooks were removed. |
| `network/NetworkHandler` | `network/NetworkHandler` | Rewritten SimpleNetworkWrapper registration using the Combatives mod id/channel. |
| `network/message/PacketSendKey` | `network/message/PacketCrawlKeyState` | Rewritten from toggle semantics to pressed/released state sync. Server validates player state before accepting crawl requests. |
| `util/Keybindings` | `client/CombativesKeyBindings` | Rewritten client crawl keybind registration with Combatives localization keys. |
| `proxy/ClientProxy` key event registration | `proxy/ClientProxy` and `client/ClientMovementInputHandler` | Rewritten as client-tick input tracking so press and release states are both sent to the server. |
| `util/math/MathHelperNew` | Inline interpolation in `EntityPlayerMixin` | Rewritten because only linear interpolation was needed for swim animation. |

## Inspected and intentionally not ported

| Aqua reference class or system | Reason skipped |
| --- | --- |
| `client/handler/AirMeterHandler`, `client/handler/FogHandler`, water resource pack classes, biome water fog colors | Water visuals and HUD changes are unrelated to making crawl/swim movement function and would broaden this PR beyond gameplay movement. |
| Bubble-column blocks, particles, boat/item/throwable mixins, underwater grass handling | These are Aqua aquatics feature systems, not required for player swimming/crawling pose/input/collision behavior. |
| Optional integration classes for EFR, Morph, AE2, Hats, Optifine, and mixin plugin compatibility gates | Combatives must not add unrelated optional compatibility systems or hard dependencies in this PR. Equivalent safety checks were kept local to vanilla player states. |
| `EntityRendererMixin`, `ItemRendererMixin`, `RenderPlayerMixin`, `ModelBipedMixin`, `PlayerControllerMPMixin`, `EntityClientPlayerMPMixin`, `EntityOtherPlayerMPMixin` | Rendering/camera overhauls are non-goals for this PR. Only the eye-height hook required for crawl/swim correctness was kept in the common player mixin. |
| Aqua config classes and branding/localization messages | Combatives swimming/crawling are always-on core behavior and must not expose Aqua runtime branding or disable toggles. |
| Aqua access transformer and build metadata | Build/metadata files are not needed and reference sources must not be compiled directly. |

## Behavior rewritten instead of copied

- Crawl input uses hold-to-crawl pressed/released state packets instead of Aqua's toggle packet so releasing the key exits when standing space is clear.
- Server-side crawl acceptance is authoritative: invalid player states reject crawl requests, and low ceilings keep the player in a low pose until the standing collision box is clear.
- Debug diagnostics are routed through `MovementDiagnostics` and gated by Combatives `debugMovement`.
