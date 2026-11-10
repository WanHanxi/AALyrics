# Android Auto automatic activation and 1.2.1 release preparation

## Authorized scope

On 2026-11-07 the owner approved implementing the discussed automatic activation and
providing a phone/car testing handoff. Work starts from personal `main` at
`54f56871c70958a444aec3c5e490aa4f46880b59` on `feature/android-auto-activation` and targets
`WanHanxi/AALyrics:main`.

When real Android Auto projection is connected and an eligible source first publishes
a valid playing track, bind the existing `LyricsBrowserService` to prepare its media
session without opening the Phone Activity. Support either connection/playback order
and process recreation. Retain the application's binding through pauses/track changes;
release it on projection disconnect. Existing notification-listener recovery supplies
playback observation. Android Auto's choice of media application is outside this slice.

No media-priority manipulation, foreground-service keepalive, new durable preference,
provider changes, or upstream work.

On 2026-11-09 the owner requested including v1.2.1 release preparation in this same PR,
avoiding a separate release PR. The extension adds the newest Changelog entry, updates
README/release instructions, and validates the existing tag-derived version and bundled
release notes. The current step keeps PR #10 as Draft; merge and tag publication follow
the repository's explicit approval gates.

## Checkpoints and acceptance

1. Record scope and baseline; commit/push the plan.
2. Implement application-owned activation policy and Android service binding, wire raw
   projection/playback inputs, and add focused lifecycle/regression tests. Bound retries
   and clean up failed/pending bindings; service-created demand must not sustain its own
   activation after disconnect. Commit/push the implementation checkpoint after tests.
3. Align lifecycle documentation and provide installation, Xiaomi settings, car scenarios,
   and diagnostic interpretation. Re-evaluate reset/privacy/Preview implications.
4. Run final unit tests, debug APK build, architecture checks, and branch-wide scope review.
   Commit/push final documentation, then open a personal Draft PR and stop.
5. Owner-requested extension: include 1.2.1 release notes and preparation in the same PR,
   validate tag-derived metadata and bundled documents, and keep publication for the
   explicitly approved merge/tag step.

Phone/car verification is a user handoff: Xiaomi 15 Pro / xiaomi.eu HyperOS 3 / Android 16,
including overnight non-use, system process reclamation, and reboot/unlock. Automated
tests cannot establish OEM wake reliability or which media card Android Auto selects.
Record these checks as pending, never as passed local validation.

## Status

- Baseline fetched and confirmed against personal `origin/main`; tracked tree was clean.
- Existing untracked `.codex-attachments/` is unrelated and must remain uncommitted.
- Existing system notification-listener recovery and projection observation are reused.
- Activation policy and explicit Android self-binding are implemented. Real projection and
  eligible playback are the only activation inputs; pauses retain the binding, disconnect
  releases it, and failed/dead/null bindings get at most three attempts per car connection.
- Focused validation passed: 43 tests across activation, listener recovery, demand gating,
  and Phone lifecycle. Architecture and whitespace checks passed. The systemd scope
  verified `MemoryMax=3221225472` and `MemorySwapMax=0` before Gradle; the build exited.
- Verification ran sequentially inside a verified 3 GiB / zero-swap systemd scope,
  with one Gradle worker, no parallel execution, no persistent daemon, and a 1 GiB heap.
- No UI layout change is planned, so no new Compose Preview fixture is required.
- Activation state is process-local. Existing diagnostic files retain their bounded
  retention/reset policy; no new persisted user state or reset target is introduced.

## Final validation and handoff

- `./gradlew test :app:assembleDebug` passed with the required workstation flags:
  116 suites / 919 tests, zero failures/errors/skips. Architecture guardrails, three
  release-script tests, and whitespace checks also passed.
- Final verification enforced the 3 GiB / zero-swap scope and recorded peak usage of
  2,234,990,592 bytes (about 2.08 GiB). The scope exited; no Java build process or emulator
  remains. No emulator or agent-run physical phone/car verification was attempted.
- Debug APK signature verification passed. Artifact:
  `app/build/outputs/apk/debug/app-debug.apk`, package `io.github.wanhanxi.aalyrics.debug`,
  version `1.2.0-dev+9a99551.dirty`, SHA-256
  `a831e06303d98aa33315e20f2e2401aaaa6ec9a1ee73ed4c3f4382a0f54391b8`.
  Runtime source is checkpoint `9a99551`; remaining changes are documentation only,
  so this successful build is reused instead of rebuilding for a new commit label.
- Branch-wide review confirmed application lifecycle wiring, two new activation/binding
  classes, focused tests, and aligned documentation only. No provider, manifest, permission,
  presentation layout, audio focus, or durable-setting changes.
- [Device testing guide](docs/ANDROID_AUTO_ACTIVATION.md) includes independent Debug setup,
  source/connection order, pause/track changes, disconnect, Bluetooth-only, overnight,
  reboot/unlock, optional controlled process death, and export interpretation.
- The owner reported on 2026-11-09 that testing appeared to work without problems and
  activation triggered every time Android Auto connected on the previously identified
  Xiaomi 15 Pro / xiaomi.eu HyperOS 3 / Android 16. This records the owner's observed
  connection-trigger result; overnight, reboot, and controlled process-death scenarios
  were not separately confirmed, and no diagnostic report was supplied with the feedback.
- Personal [Draft PR #10](https://github.com/WanHanxi/AALyrics/pull/10) is open.
  [Build run 42458511957](https://github.com/WanHanxi/AALyrics/actions/runs/42458511957)
  passed for implementation/documentation head `4933c15`. The later device-feedback
  checkpoint `2aa4802` also passed [Build run 42770833051](https://github.com/WanHanxi/AALyrics/actions/runs/42770833051).
  Both documentation follow-ups reuse the successful local runtime tests/APK.
- Review requests, ready-for-review transition, merge, publication, and media-priority
  work still require separate owner authorization. No guaranteed OEM wake behavior or
  automatic media-card selection is claimed.

## Combined v1.2.1 release preparation

- Keep `feature/android-auto-activation` and personal PR #10 for both the feature and
  release documentation. No second PR or release branch is needed.
- Published v1.2.0 has its signed APK and checksum; Release run 42449660669 succeeded
  with run number 3. The next normal Release run would produce versionCode 1004; the
  workflow remains authoritative and must be rechecked at publication.
- Personal `main` is still `54f5687`; the prior PR head `2aa4802` passed Build run
  42770833051. Remote v1.2.1 did not exist at preparation start.
- Added Changelog 1.2.1 dated 2026-11-09 with activation behavior, retained 1.2.0 state/cache,
  direct 1.1.x upgrade limitations, and the boundary between activation and media priority.
- Executed the existing Release workflow's version resolution, Changelog version check,
  and curated-note extraction locally with `v1.2.1` and sample run number 4. All passed:
  versionName `1.2.1`, title `AALyrics 1.2.1`, sample versionCode `1004`, and extracted notes
  exactly matching the newest Changelog body. No signing or publication step was run.
- `:app:syncBundledDocumentsAssets` passed; all six generated document assets exactly
  match their sources. The verified 3 GiB / zero-swap scope peaked at 1,024,585,728 bytes
  (about 0.95 GiB) and exited, with no Java or emulator processes remaining.
- Architecture and whitespace checks passed. This extension changes five documentation
  files only; successful runtime tests and the test APK are reused. The existing local
  test APK predates the new release notes; the tagged Release build will include them.
- PR #10 tracks the combined scope and final CI evidence. After approved merge, recheck
  the Changelog date and create v1.2.1 on merged `main`; pushing the tag runs the existing
  signed Release workflow. No tag is used as a dry run.
