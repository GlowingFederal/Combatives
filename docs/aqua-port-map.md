# Aqua Acrobatics Legacy swimming/crawling port map

Aqua Acrobatics Legacy is included in this repository as public-domain/Unlicense reference material. Combatives ports only the connected swimming/crawling systems needed for player movement and rewrites them under `com.glowingfederal.combatives`; reference sources are not compiled.

## Source-level audit table

| Aqua class/file | Purpose | Used by swimming/crawling? | Ported to Combatives? | Combatives replacement class | If skipped, exact reason |
| --- | --- | --- | --- | --- | --- |
| `AquaAcrobatics.java` | Mod lifecycle entry point and proxy delegation. | yes | yes | `Combatives`, `proxy/CommonProxy`, `proxy/ClientProxy` | Rewritten for Combatives mod id and lifecycle. |
| `proxy/CommonProxy.java` | Common lifecycle, network registration, server handlers. | yes | yes | `proxy/CommonProxy`, `network/NetworkHandler` | Unrelated Aqua config/event code omitted. |
| `proxy/ClientProxy.java` | Client lifecycle, key/event registration, crawl key packet trigger. | yes | yes | `proxy/ClientProxy`, `client/ClientMovementInputHandler` | Rewritten as hold-to-crawl tick input plus startup diagnostics. |
| `util/Keybindings.java` | Registers Aqua crawl key/category. | yes | yes | `client/CombativesKeyBindings` | Rewritten with Combatives localization keys and logs. |
| `network/NetworkHandler.java` | SimpleNetworkWrapper channel and packet registration. | yes | yes | `network/NetworkHandler` | Rewritten with Combatives channel. |
| `network/message/PacketSendKey.java` | Sends crawl toggle key to server and updates server crawl state. | yes | yes | `network/message/PacketCrawlKeyState` | Rewritten from toggle to pressed/released state for hold-to-crawl. |
| `entity/Pose.java` | Pose enum used by size, DataWatcher, rendering, movement. | yes | yes | `entity/Pose` | Directly ported values needed by swim/crawl. |
| `entity/EntitySize.java` | Width/height metadata and fixed/flexible sizing. | yes | yes | `entity/EntitySize` | Directly ported minimal value type. |
| `entity/player/IPlayerResizeable.java` | Shared pose/swim/crawl/size/eye-height contract. | yes | yes | `entity/player/ICombativesPlayerPose` | Rewritten and extended for Combatives crawl-key state. |
| `client/entity/IPlayerSPSwimming.java` | Local-player swim sprint, forced-down, and actual sneak helpers. | yes | yes | `client/ICombativesClientPlayerSwimming` | Ported for local prediction and packet sneak correction. |
| `util/MovementInputStorage.java` | Saves pre-vanilla movement/sprint input state for Aqua sprint logic. | yes | yes | `client/MovementInputStorage` | Ported minimal fields used by swim/crawl. |
| `mixins/early/minecraft/EntityPlayerMixin.java` | Core DataWatcher pose sync, updateSwimming, updatePose, collision resize, eye height, swim movement. | yes | yes | `mixin/EntityPlayerMixin` | Rewritten with Combatives diagnostics and no optional integrations. |
| `mixins/early/minecraft/client/EntityPlayerSPMixin.java` | Local-player prediction: actual sneak, forced crouch/crawl, water sprint persistence, swim start/stop, water sneaking. | yes | yes | `mixin/EntityPlayerSPMixin` | Ported core behavior; block-push exact-collision stream omitted because Combatives does not expose Aqua collision config. |
| `mixins/early/minecraft/client/EntityClientPlayerMPMixin.java` | Sends actual sneak state instead of forced visual crouch state. | yes | yes | `mixin/EntityClientPlayerMPMixin` | Ported redirect for client/server state consistency. |
| `mixins/early/minecraft/client/EntityOtherPlayerMPMixin.java` | Corrects remote player yOffset after pose sync. | yes | yes | `mixin/EntityOtherPlayerMPMixin` | Ported yOffset correction for multiplayer visual sync. |
| `mixins/early/minecraft/EntityPlayerMPMixin.java` | Server-side eye-height helpers for swimming pose. | yes | partial | `mixin/EntityPlayerMixin` | Common eye-height hook covers Combatives server/client player path; no separate MP-only class needed. |
| `mixins/early/minecraft/client/ModelBipedMixin.java` | Player model swim/crawl animation. | yes | no | none | Inspected; visual animation only, not required to persist DataWatcher pose, collision box, or server state. |
| `client/model/IModelBipedSwimming.java` | Interface for swim animation on models. | yes | no | none | Only supports skipped `ModelBipedMixin` rendering animation. |
| `mixins/early/minecraft/client/RenderPlayerMixin.java` | Applies swim/crawl rotations in rendering. | yes | no | none | Inspected; rendering-only and not part of authoritative pose/size/sync state. |
| `mixins/early/minecraft/client/EntityRendererMixin.java` | Camera/fog/view adjustments. | yes | no | `mixin/EntityPlayerMixin` eye-height hook | Camera correctness is handled through pose eye height; fog/visual-only code skipped. |
| `mixins/early/minecraft/client/ItemRendererMixin.java` | First-person hand/item swim rendering. | no | no | none | Rendering-only; no pose persistence or movement state. |
| `mixins/early/minecraft/client/PlayerControllerMPMixin.java` | Client interaction tweaks around swimming/crawling. | no | no | none | Inspected; not required for pose state machine or crawl key. |
| `util/math/MathHelperNew.java` | Lerp/rotation math for animations. | yes | partial | inline interpolation in `EntityPlayerMixin` | Only linear swim-animation interpolation needed for state; model rotation helpers skipped with model renderer. |
| `util/math/AxisAlignedBBSpliterator.java` | Exact block collision stream for client push-out config. | no | no | none | Only used by skipped Aqua configurable exact block collision branch, not core pose box validation. |
| `util/math/CubeCoordinateIterator.java` | Helper for exact collision stream. | no | no | none | Dependency of skipped exact collision branch only. |
| `util/BlockPos.java` | Lightweight coordinate helper for exact collision/client push-out code. | no | no | none | Dependency of skipped exact collision branch only. |
| `mixins/early/minecraft/accessor/FluidAccessor.java` | Fluid internals for Aqua water systems. | no | no | none | Not used by player swim/crawl state machine. |
| `mixins/early/minecraft/accessor/IEventBusAccessor.java` | Event bus internals for Aqua compatibility. | no | no | none | Not used by player swim/crawl state machine. |
| `handler/CommonHandler.java` | Misc common gameplay handlers. | no | no | none | Inspected; unrelated to Combatives player swim/crawl state. |
| `client/handler/AirMeterHandler.java` | Air meter rendering. | no | no | none | HUD-only. |
| `client/handler/FogHandler.java` | Water fog rendering. | no | no | none | Visual-only. |
| `client/resource/WaterResourcePack*.java` | Water texture/resource replacement. | no | no | none | Visual/resource system unrelated to pose. |
| `biome/BiomeWaterFogColors.java` | Water fog color data. | no | no | none | Visual-only. |
| `block/BlockBubbleColumn.java`, `entity/IBubbleColumnInteractable.java` | Bubble column block/entity interactions. | no | no | none | Aquatics block feature unrelated to crawl/swim pose persistence. |
| `mixins/early/minecraft/BlockLiquidMixin.java`, `BlockSoulSandMixin.java`, `BlockGrassMixin.java`, `BlockMyceliumMixin.java` | Aqua water/block feature behavior. | no | no | none | Not in player pose/crawl/sprint call chain. |
| `mixins/early/minecraft/EntityMixin.java`, `EntityLivingBaseMixin.java`, `EntityItemMixin.java`, `EntityThrowableMixin.java`, `EntityBoatMixin.java` | General Aqua entity/water/boat behavior. | no | no | none | Inspected; not required for player pose DataWatcher or crawl key sync. |
| `mixins/early/minecraft/client/RenderBoatMixin.java` | Boat rendering. | no | no | none | Rendering-only and unrelated. |
| `client/particle/*` | Bubble/current particles. | no | no | none | Visual-only. |
| `integration/*`, `optifine/*`, `mixinplugin/*`, `config/ConfigHandler.java` | Optional mod integrations, mixin gates, config toggles. | no | no | none | Combatives must not add Aqua branding, optional dependencies, mixin plugin logic, or swim/crawl disable configs. |

## Behavior notes

- Crawl input remains hold-to-crawl instead of Aqua's toggle, but the server remains authoritative and releasing the key exits only when the standing pose collision box is clear.
- Local-player prediction now follows Aqua's connected sprint/crouch/swim input flow: actual sneak state is separated from forced visual crouch, water sprint can persist, and client movement is slowed while forced down.
- Pose state is synchronized through the player DataWatcher, and debug movement logs identify pose writes, DataWatcher corrections, bounding-box recalculation, eye-height recalculation, swim/crawl selection, and explicit swim cancellation reasons.
