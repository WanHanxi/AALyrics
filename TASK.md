# Personal fork 1.1 Beta 1 release preparation

## Scope and authorization

- Prepare `v1.1.0-beta.1` on `release/1.1.0-beta.1`, based on personal `main` `175e821`.
- The owner requested TeslaMate-inspired release notes, personal update routing, an independent application ID/signing identity, and release-pipeline verification.
- Follow-up: the owner explicitly requested making PetitLyrics optional instead of depending on credentials from another project. All four values absent/blank disables the source; a complete configuration enables it; partial configuration fails with field names only.
- Update the existing personal Draft PR [#4](https://github.com/WanHanxi/AALyrics/pull/4). No merge, ready conversion, review request, tag, or public release is authorized.

## Checkpoints

- [x] Prepare personal update routing, install/signing identity, curated notes and release verification; open Draft PR #4.
- [x] Make PetitLyrics optional in Gradle configuration, production composition and the Release secret gate; retain mandatory release signing.
- [x] Align Changelog, Privacy, README, provider/composition and release documentation with the default three-source build.
- [x] Verify absent/complete/partial configuration, provider/graph tests, architecture and the signed candidate under the workstation resource budget.
- [x] Review the branch diff, push coherent checkpoints, verify hosted CI and update Draft PR #4; stop before merge/tag.

## Decisions and boundaries

- Version: `1.1.0-beta.1`; accumulated features justify a minor bump without claiming stable readiness.
- Display names remain `AALyrics` / `AALyrics Debug`. Release package: `io.github.wanhanxi.aalyrics`; Debug adds `.debug`. Kotlin namespaces remain `io.github.whoxamxl.aalyrics`.
- Default sources: LRCLIB, Musixmatch and SyncLRC. No local PetitLyrics configuration or repository Secrets exist. Do not retrieve/copy third-party credentials or change supplied values.
- Optionality belongs to build configuration and application wiring. Preserve provider transport/search/parsing, provider order when enabled, selector and timing semantics.
- Reset contract re-evaluated: no new persisted state or user preference. Reset cannot change build configuration. Active provider IDs already partition cache keys; cache retention and reset behavior stay unchanged.
- Keep upstream license, notices and donation attribution. Use the existing personal signing key; do not rotate it or commit private material.
- No emulator is required. Run local Gradle work sequentially with one worker, no parallelism, no persistent daemon, 1 GiB heap/512 MiB metaspace, and verified systemd `MemoryMax=3G` / `MemorySwapMax=0`. Do not change IDE/JVM settings.

## Completed release-preparation evidence (before optional-source follow-up)

- Personal main/base: `175e8212da71aa988dda6ce8271f2125bba94579`. Draft PR #4 originally opened at `2956650`; no upstream-facing branch was touched.
- Notes reference: [TeslaMate v4.3.0](https://github.com/teslamate-org/teslamate/releases/tag/v4.3.0), with outcomes first, installation notes, features/fixes, limitations and full change history last.
- Full local `test :app:assembleRelease` passed: 863 tests across 109 suites, no failures/errors/skips. Architecture checks passed. Local runs verified the 3 GiB / zero-swap limits; no emulator was started.
- Prior signed candidate: `app/build/outputs/apk/release/aalyrics-personal-1.1.0-beta.1-validation.apk`, version `1.1.0-beta.1`, code `1001`, personal package/certificate, label `AALyrics`; six bundled documents matched then-current source. Its former SHA256 was `f6a4932ba07103ee3dc7e704b11a86df3db49b3370bf36a3b03ebda125c72dd5`. The source and bundled-document changes in this follow-up require rebuilding it.
- APK negative checks rejected the upstream package, wrong versionCode, wrong certificate pin and stale Changelog. Workflow dry runs verified tag/version/title/code/notes and rejected malformed tags and version mismatches. No tag or Release was created.
- The owner completed GitHub CLI authentication as `WanHanxi`; the token is in the system keyring. Repository Actions are enabled with the existing `allowed_actions: all` policy.
- All four `AALYRICS_RELEASE_*` signing Secrets were uploaded from the existing personal key and verified by names only. No PetitLyrics Secrets are configured or required for the default build.
- [Hosted Build 41613715977](https://github.com/WanHanxi/AALyrics/actions/runs/41613715977) passed on `6c2cabc` in 7m 52s: Debug APK, unit tests, release-signed lower-version fixture and uploads. Manual dispatch was used after earlier pushes showed no runs; no cause is claimed for those absent runs.
- Downloaded hosted fixture: `1.1.0-alpha.1`, code `1`, package `io.github.wanhanxi.aalyrics`, label `AALyrics`. Its certificate matches the local key and committed pin: `65ab98bb7c208e914c49c229bf040f29ab2570d9af697c9fdf789b3f268e473d`. APK SHA256: `608237e2e2d98167ee4065f20a86282324622806bf0c25e24385c173224b80f5`.
- No connected Android device was available. The task-started adb server was stopped; no emulator was used. Generated Gradle JVM settings retained SHA256 `6c13aa5ba36690ccfbbb25a8807d01d98c15833e446d7ae82ddc726472cb8ef7`.

## Optional-source follow-up validation

- Configuration matrix passed using only synthetic values: partial configuration (including a whitespace-only missing field) fails with missing field names and no value leakage; all-whitespace values succeed; complete values pass application-graph tests and are preserved exactly. The final build removes all synthetic environment values and uses no local provider file.
- Full `test :app:assembleRelease` passed in 1m 14s: 865 tests across 109 suites, zero failures/errors/skips. This includes absent/configured production graph coverage and the existing PetitLyrics no-network-on-incomplete-config test. All local Gradle invocations verified the 3 GiB / zero-swap scope and used the mandated one-worker flags.
- Exercised the actual Release workflow signing gate with synthetic inputs: it accepts missing PetitLyrics configuration and rejects each missing required signing field. The signed build still receives optional provider variables for authoritative Gradle validation.
- Current signed candidate: `app/build/outputs/apk/release/aalyrics-personal-1.1.0-beta.1-validation.apk`, 84409579 bytes; SHA256 `54b2f67dafd52b37ac89f49a1593ae2bdf4183a2196058be122330c435cf416b`. Package `io.github.wanhanxi.aalyrics`, label `AALyrics`, version `1.1.0-beta.1`, code `1001`, and the pinned personal certificate all pass inspection. The six bundled documents match current source, all four generated Release provider fields are empty, and synthetic configuration is absent from APK code.
- Architecture and whitespace checks pass. Rechecked the cumulative branch scope and personal remote main, still `175e821`. No provider implementation, reset code, UI flow, signing key or upstream-facing branch was changed by this follow-up. Generated JVM settings retain their recorded hash.
- Optional-source checkpoint `15ce4ea` is pushed. [Hosted Build 41626517577](https://github.com/WanHanxi/AALyrics/actions/runs/41626517577) passed on that exact source in 5m 12s: architecture, Debug APK, unit tests and both Debug artifact uploads. As before, no automatic run was visible after the push; an explicit manual Build dispatch supplied this verification. No publication workflow was triggered.
- Draft PR #4 now describes the optional provider and the current local/hosted results, with no missing-PetitLyrics publication blocker. The remaining record-only commit changes TASK.md, so it reuses the successful APKs and CI. All task Gradle scopes are inactive/not loaded; no emulator was started. Stop without ready conversion, review request, merge or tag.

## Publication handoff

- On 2026-11-03, the owner reported that installing the older signed fixture and then the current signed candidate worked, and functional testing found no problems. Record this as owner-reported real-device installation/upgrade and functional validation; no additional APK/source change or build is needed.
- PetitLyrics credentials are no longer a publication prerequisite. Only enable the source later with a complete set of authorized credentials and matching release notes.
- Independent backup of the existing keystore and passwords remains unconfirmed; GitHub Secrets are not a replacement for that backup.
- The manual old-to-new installation check does not exercise published GitHub discovery/download. Verify the published APK/checksum assets, Pre-release status and in-app discovery after authorized publication.
- Merge/tag/publication still require explicit owner authorization. Keep PR #4 as Draft after this follow-up.
