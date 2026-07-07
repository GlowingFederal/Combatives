# Combatives

Combatives is a Battlefield-inspired movement and camera overhaul foundation for Minecraft Forge 1.7.10.

## Clean reference policy

Aqua Acrobatics Legacy is included in this repository only as public-domain (Unlicense) reference material. It is not compiled, packaged, renamed into Combatives packages, or directly depended on by this mod.

Combatives is a clean, original implementation. Reference source may be used only as inspiration while future systems are implemented independently.

## Current foundation

- Mod name: Combatives
- Mod ID: `combatives`
- Root package: `com.glowingfederal.combatives`
- Forge target: `1.7.10-10.13.4.1614`
- UniMixins-compatible mixin config: `mixins.combatives.json`
- Common/client proxy structure
- Initial config options:
  - `enableAngelicaCompat = true`
  - `debugMovement = false`
  - `debugCamera = false`

## Future work

Future work will include modern swimming, crawling, Battlefield-inspired movement inertia, procedural camera motion, recoil, landing effects, and optional Angelica compatibility.

Angelica compatibility must remain optional. Angelica is not a hard runtime dependency.

## Development notes

Do not compile anything inside the reference folder. Do not copy Aqua Acrobatics code directly, and do not port unrelated camera systems as part of this foundation work.
