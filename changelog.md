# Changelog

## Initial Combatives foundation

- Renamed the example mod foundation to Combatives with mod ID `combatives` and root package `com.glowingfederal.combatives`.
- Added UniMixins-compatible manifest metadata and `mixins.combatives.json`.
- Added common/client proxy classes, an initial config with startup logging, and placeholder future-system packages.
- Updated README documentation for Combatives scope, reference-only Aqua Acrobatics Legacy policy, non-compiled reference source, and planned future movement/camera work.

## Production-grade Gradle versioning

- Added `version.properties` with manually managed `mod_version` and automatically managed `build_number` values.
- Added Gradle version loading so the project exposes `modVersion`, `buildNumber`, and `fullVersion`, with JARs using `mod_version.build_number`.
- Added production-build build number incrementing with failed-build rollback while preserving `version.properties` comments and formatting.
