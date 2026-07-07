# Changelog

## Complete Combatives swimming and crawling port

- Added the missing crawl keybind, localization, client tick input handling, and client-to-server crawl state packet/channel registration.
- Extended the player pose mixin with hold-to-crawl state, server-side crawl validation, standing-space blocking, low-ceiling crawl retention, and an eye-height hook used by crawl/swim poses.
- Expanded debug movement diagnostics for crawl input, crawl request acceptance/rejection, pose collision blocking, and client/server pose sync.
- Added `docs/aqua-port-map.md` documenting Aqua reference classes inspected, ported, rewritten, and intentionally skipped.

## Port Aqua swimming and crawling behavior into Combatives

- Ported/refactored Aqua Acrobatics Legacy swimming and crawling pose behavior into Combatives packages.
- Added player pose synchronization, bounding-box resizing, crawl/swim eye-height state, water movement, and scoped movement diagnostics.
- Registered the required player mixin in `mixins.combatives.json` without introducing an Angelica hard dependency.
- Documented that Aqua Acrobatics Legacy is public-domain/Unlicense reference material and that the runtime implementation is refactored under Combatives.

## Initial Combatives foundation

- Renamed the example mod foundation to Combatives with mod ID `combatives` and root package `com.glowingfederal.combatives`.
- Added UniMixins-compatible manifest metadata and `mixins.combatives.json`.
- Added common/client proxy classes, an initial config with startup logging, and placeholder future-system packages.
- Updated README documentation for Combatives scope, reference-only Aqua Acrobatics Legacy policy, non-compiled reference source, and planned future movement/camera work.

## Production-grade Gradle versioning

- Added `version.properties` with manually managed `mod_version` and automatically managed `build_number` values.
- Added Gradle version loading so the project exposes `modVersion`, `buildNumber`, and `fullVersion`, with JARs using `mod_version.build_number`.
- Added production-build build number incrementing with failed-build rollback while preserving `version.properties` comments and formatting.
