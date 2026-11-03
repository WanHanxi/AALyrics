# Personal fork 1.1 Beta 1 release preparation

## Scope and authorization

- Prepare `v1.1.0-beta.1` on `release/1.1.0-beta.1`, based on personal `main` `175e821`.
- The owner requested curated release notes inspired by TeslaMate, personal update routing, an independent application ID and signing identity, and release-pipeline verification.
- The owner confirmed that PetitLyrics credentials are unavailable/unknown and asked to finish the remaining preparation first. Do not change provider identity values or silently disable the provider; record the missing release prerequisite.
- No merge or public release/tag push is authorized. Follow the repository workflow through final validation and a Draft PR against `WanHanxi/AALyrics:main`.

## Checkpoints

- [x] Route update discovery and distribution links to the personal repository; separate the install identity while preserving Kotlin namespaces and upstream attribution.
- [x] Curate `1.1.0-beta.1` Changelog with user-facing highlights, installation guidance, fixes and actual limitations; align release documentation.
- [ ] Create and verify a durable personal release key outside Git, prepare repeatable release checks, and inspect available GitHub Actions configuration.
- [ ] Run architecture checks and relevant tests/builds sequentially under the verified 3 GiB / zero-swap budget; inspect the signed APK identity, certificate and bundled changelog.
- [ ] Review the complete branch, record validation and external prerequisites, push coherent checkpoints and open the personal Draft PR.

## Decisions and boundaries

- Version: `1.1.0-beta.1`; the new minor version reflects the accumulated features without claiming stable readiness.
- Release application ID: `io.github.wanhanxi.aalyrics`; Debug adds `.debug`. Source namespaces remain `io.github.whoxamxl.aalyrics`.
- Keep existing provider/timing/reset behavior. A new package has separate Android-owned permissions and app storage; there is no cross-package settings/cache migration.
- Keep the original project's license, notices and donation attribution. Do not reuse the author's private signing key or rotate any existing personal release key.
- Signing material must stay outside the repository, with restrictive permissions; only public certificate fingerprints may appear in tracked documentation.
- GitHub connector supports repository code/PR operations, but not Actions secret administration. No authenticated browser is available in this session. Local provider configuration is absent. These are external setup limits, not successful cloud validation.
- No emulator is required for the source/configuration change. Reuse successful builds when unchanged; native install/update and permission flows must be recorded separately from APK inspection and JVM tests.

## Evidence

- Confirmed remote personal `main` is `175e8212da71aa988dda6ce8271f2125bba94579`.
- Personal GitHub Actions runs: none visible. Personal Releases: none in the initial assessment.
- Reference: TeslaMate release notes at <https://github.com/teslamate-org/teslamate/releases/tag/v4.3.0>: outcome-focused introduction, relevant upgrade notice, features/fixes, full change history last.

- Identity checkpoint: architecture checks passed; `:app:testDebugUnitTest` passed with 402 tests and no failures/errors. The process verified `memory.max=3221225472` and `memory.swap.max=0`.
