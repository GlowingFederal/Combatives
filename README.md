# Combatives

Combatives is a Battlefield-inspired movement and camera overhaul foundation for Minecraft Forge 1.7.10.

## Aqua Acrobatics Legacy port foundation

Combatives now ports and refactors the working modern swimming and crawling behavior from Aqua Acrobatics Legacy into the active `com.glowingfederal.combatives` package tree. Aqua Acrobatics Legacy is included in this repository as public-domain (Unlicense) reference material and port foundation, but it is not required at runtime and no old Aqua mod ID or package is used by compiled Combatives source.

The port is intentionally scoped to player pose, collision bounds, eye-height state, water movement, crawl input/keybind handling, client-to-server crawl state networking, standing-space checks, and server-safe DataWatcher synchronization needed for swimming/crawling. Aqua build logic, metadata, branding, resources, optional compatibility systems, and unrelated aquatics features remain unported.

## Current foundation

- Mod name: Combatives
- Mod ID: `combatives`
- Root package: `com.glowingfederal.combatives`
- Forge target: `1.7.10-10.13.4.1614`
- UniMixins-compatible mixin config: `mixins.combatives.json`
- Common/client proxy structure
- Always-on modern swimming and crawling behavior, refactored from the Aqua Acrobatics Legacy reference source
- Dedicated crawl keybind (`C` by default) with Combatives networking for authoritative server-side crawl state
- Initial config options:
  - `enableAngelicaCompat = true`
  - `debugMovement = false`
  - `debugCamera = false`

## Future work

Future work will include Battlefield-inspired movement inertia, procedural camera motion, recoil, landing effects, and optional Angelica compatibility.

Angelica compatibility must remain optional. Angelica is not a hard runtime dependency.

## Development notes

See `docs/aqua-port-map.md` for the inspected Aqua source map and port/skipped-class rationale. Do not compile anything inside the reference folder. Do not compile or add generated binary files to commits or pull requests. Do not port unrelated Aqua Acrobatics systems unless they are required for Combatives swimming/crawling behavior to function.
