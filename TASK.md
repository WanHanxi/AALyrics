# Android Auto automatic activation

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
provider changes, or upstream work. No merge or release publication is authorized.

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

Phone/car verification is a user handoff: Xiaomi 15 Pro / xiaomi.eu HyperOS 3 / Android 16,
including overnight non-use, system process reclamation, and reboot/unlock. Automated
tests cannot establish OEM wake reliability or which media card Android Auto selects.
Record these checks as pending, never as passed local validation.

## Status

- Baseline fetched and confirmed against personal `origin/main`; tracked tree was clean.
- Existing untracked `.codex-attachments/` is unrelated and must remain uncommitted.
- Existing system notification-listener recovery and projection observation are reused.
- Verification will run sequentially inside a verified 3 GiB / zero-swap systemd scope,
  with one Gradle worker, no parallel execution, no persistent daemon, and a 1 GiB heap.
- No UI layout change is planned, so no new Compose Preview fixture is required.
- Activation state is process-local. Existing diagnostic files retain their bounded
  retention/reset policy; no new persisted user state or reset target is introduced.
