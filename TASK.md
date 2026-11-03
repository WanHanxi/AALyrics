# Personal fork 1.1 Beta 1 release preparation

## Scope and authorization

- Prepare `v1.1.0-beta.1` on `release/1.1.0-beta.1`, based on personal `main` `175e821`.
- The owner requested curated release notes inspired by TeslaMate, personal update routing, an independent application ID and signing identity, and release-pipeline verification.
- The owner confirmed that PetitLyrics credentials are unavailable/unknown and asked to finish the remaining preparation first. Do not change provider identity values or silently disable the provider; record the missing release prerequisite.
- No merge or public release/tag push is authorized. Follow the repository workflow through final validation and a Draft PR against `WanHanxi/AALyrics:main`.

## Checkpoints

- [x] Route update discovery and distribution links to the personal repository; separate the install identity while preserving Kotlin namespaces and upstream attribution.
- [x] Curate `1.1.0-beta.1` Changelog with user-facing highlights, installation guidance, fixes and actual limitations; align release documentation.
- [x] Create and verify a durable personal release key outside Git, prepare repeatable release checks, and inspect available GitHub Actions configuration.
- [x] Run architecture checks and relevant tests/builds sequentially under the verified 3 GiB / zero-swap budget; inspect the signed APK identity, certificate and bundled changelog.
- [x] Review the complete branch, record validation and external prerequisites, and push coherent checkpoints.
- [ ] Open the personal Draft PR after final handoff; stop without merge or tag publication.

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

- Signed verification checkpoint: `test :app:assembleRelease` passed in 3m 7s; 863 tests across 109 suites, zero failures/errors/skips. A subsequent 36-second `:app:assembleRelease` reused compilation and packaged the final Privacy maintainer clarification. Both runs verified the 3 GiB / zero-swap limits. No emulator was started.
- Final APK is `app/build/outputs/apk/release/aalyrics-personal-1.1.0-beta.1-validation.apk`, 84392999 bytes; SHA256 `f6a4932ba07103ee3dc7e704b11a86df3db49b3370bf36a3b03ebda125c72dd5`. Package `io.github.wanhanxi.aalyrics`, version `1.1.0-beta.1`, code `1001`; all six bundled documents match source. This local artifact has no PetitLyrics credentials and is not approved for public distribution.
- `verify-release-apk.py` passed against the real personal signed APK. Negative checks rejected upstream package identity, a wrong Android versionCode, a wrong certificate pin and stale bundled notes. The signing setup helper's dry run validated the private key without uploading; a Debug key was rejected.
- Release workflow scripts passed a local dry run for tag/version/title/code resolution and curated-note extraction; invalid tags and mismatched Changelog versions were rejected. No tag or Release was created.
- Public Actions metadata confirms Build and Release are active. Added `release/**` Build pushes so preparation receives hosted CI before PR creation; provider credentials remain limited to main pushes.
- No connected Android device was available (`adb devices` empty). Native installation/update, Android Auto discovery and permission re-grant remain an explicit device handoff. The connector cannot administer signing Secrets; the owner subsequently completed GitHub CLI login as recorded below. The owner deferred missing PetitLyrics credentials. An off-device private-key backup has not been verified.

- Owner correction: retain the original display names `AALyrics` / `AALyrics Debug`; distinguish the personal app by its package ID. Repackage the verified source after this resource-only change; no new unit tests are needed for label text.

- Display-name correction validated with `:app:assembleRelease` in 51 seconds under the same limits. The final signed APK reports label `AALyrics`; the package ID, personal certificate and bundled Changelog verification pass. The final artifact hash/size above now refer to this corrected build.

## Final branch review and external handoff

- Reviewed the whole branch against personal main `175e821`; the final scope is release preparation, distribution identity/routing and documentation. No provider behavior, playback/timing semantics or reset implementation was changed. All commits use the repository's Conventional Commit form.
- Remote main remains `175e821`; the personal release branch was verified pushed through the requested display-name correction. No upstream-facing branch was touched.
- Repeated authenticated-connector and public API reads after release-branch pushes still returned zero Actions runs, although workflow metadata is `active`. An authenticated check later confirmed repository Actions are enabled. The subsequent explicit Build dispatch is recorded below; no cause is claimed for the earlier absent push runs.
- A checksum-verified official GitHub CLI was prepared outside the repository and a device login was requested from the owner, because the connector cannot manage Actions Secrets. The owner completed that login, and the authenticated setup is recorded below.
- All task Gradle scopes are inactive/not loaded; no emulator was started. The task-started empty adb server was stopped. The generated Gradle JVM settings retain SHA256 `6c13aa5ba36690ccfbbb25a8807d01d98c15833e446d7ae82ddc726472cb8ef7`.
- The corrected local signing artifact is the existing validated APK above. Later task/status documentation does not require rebuilding it. The PR must state PetitLyrics credentials, independent key backup and real-device validation as publication prerequisites; the hosted signing result is recorded below.

## Authenticated hosted setup

- The owner completed GitHub CLI device authentication as `WanHanxi`; credentials were stored in the system keyring. The repository Actions permission API returned `enabled: true` with its existing `allowed_actions: all` policy; no permission-policy change was made.
- Uploaded all four `AALYRICS_RELEASE_*` signing Secrets using the verified personal key, then read back only their names to confirm presence. No private values were printed. The repository still has no PetitLyrics Secrets.
- Started Build run <https://github.com/WanHanxi/AALyrics/actions/runs/41613715977> on source `6c2cabc`, with the existing release-signed test fixture enabled (`1.1.0-alpha.1`, versionCode `1`). It produces workflow artifacts, not a GitHub Release, and is intentionally older than the planned personal release.

- Hosted Build run `41613715977` completed successfully in 7m 52s: Debug APK, unit tests, release-signed fixture build, artifact uploads and keystore cleanup all passed. The only remaining annotations are upstream Actions/runtime deprecation notices, with no failed check.

- Downloaded the hosted signed fixture and independently verified it with Android Build Tools: package `io.github.wanhanxi.aalyrics`, label `AALyrics`, version `1.1.0-alpha.1`, code `1`, and certificate SHA256 `65ab98bb7c208e914c49c229bf040f29ab2570d9af697c9fdf789b3f268e473d`, matching the local key and committed pin. APK SHA256: `608237e2e2d98167ee4065f20a86282324622806bf0c25e24385c173224b80f5`. This confirms the uploaded signing Secrets produce the intended installation identity.
- Final review is complete and the branch is ready for the personal Draft PR. Only task validation records follow the successful cloud source revision; reuse the successful local and hosted APKs. Publication still requires the deferred provider credentials, an independent key backup, native device verification, and explicit owner authorization to merge/tag.
