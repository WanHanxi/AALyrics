# Android Auto incident diagnostics Debug APK

## Scope

Start from personal main `659c37f` (released as 1.2.2), on
`fix/android-auto-diagnostics`, targeting `WanHanxi/AALyrics:main`.
The owner requested a Debug APK that records enough context to investigate a
future unexpected switch from AALyrics to Spotify using the incident time and
an exported log. The owner will install the APK and perform the real-car test.

Reuse automatic, bounded Debug connection diagnostics and the existing
Settings > Advanced > Debug > Export connection diagnostics action. Verbose
details remains a presentation preference, independent of recording. Do not
add a new setting, enable production logging, or change AA selection behavior.

## Implementation checkpoints

1. Record received system session lists with their original order, notification
   versus query origin, token fingerprints, and playback/routing state. Include
   selected-controller identity, playback transitions, and destruction events.
   Instrument existing reads/callbacks only; never refresh sessions or change
   playback to obtain a diagnostic record.
2. Correlate AALyrics activation requests, published state, initial activation,
   connection refresh, and service destruction with the connection request and
   own session fingerprint. Keep lyric ticks and playback-position churn out of
   the log. Preserve metadata privacy and existing storage bounds.
3. Add focused regression tests, run required tests/build within workstation
   limits, align documentation, and deliver a signed Debug APK. Push coherent
   checkpoints, review the branch diff, and open a personal Draft PR only after
   final validation. Stop before review/merge/release.

## Acceptance and validation

- [x] Logs distinguish session order changes, identical list notifications, and
  replacement sessions from the same package, with diagnostic read failures
  isolated from playback.
- [x] Logs identify AALyrics' published playback and activation request/reason,
  without introducing new activation, session queries, or transport commands.
- [x] Debug-only recording and export retain existing bounded local storage;
  no song titles, lyrics, media IDs, raw tokens, or automatic uploads.
- [x] Focused regression tests and required architecture/build checks pass.
- [x] APK package, version, debug signature, and installation compatibility with
  the previous local Debug artifact are checked where evidence is available.
- [x] Document the incident-time/export procedure and observability limits.
- [x] Commit/push implementation checkpoints and prepare the validated final
  documentation checkpoint for a personal Draft PR.

## Reset and presentation review

No new durable preference or storage category. Diagnostic records continue to
survive Reset AALyrics for troubleshooting and remain bounded by the existing
rotation policy. No user-visible layout or control changes; existing Compose
Previews remain applicable. The supplied APK is for the owner's subsequent
installation/test; do not claim physical-device validation before it happens.

## Observability limits

AALyrics can observe system session lists and its own behavior. These records
cannot directly read Android Auto's chosen display source or prove another app
called setActive. Token fingerprints are diagnostic correlation values, scoped
by the process records; they are not serialized session tokens. Correlate the
owner's incident time and observed sidebar/cluster with the surrounding events.

## Completed verification and handoff

- Runtime/test checkpoints: `822db4b` and `23a52dc`. The first test compilation
  exposed nullable-list assertion typing errors; corrected them before the
  successful validation run.
- 531 Debug unit tests passed: 68 in `:platform:media`, 27 in `:ui:automotive`,
  and 436 in `:app`, including eight new diagnostic regressions. Debug APK build,
  architecture guardrail, whitespace, and branch scope checks passed.
- Gradle ran sequentially with one worker, no parallel execution, no persistent
  daemon, a 1 GiB JVM heap, and in-process Kotlin compilation inside a verified
  3 GiB/no-swap systemd scope. Task scopes exited; no emulator or phone installation
  was used for this diagnostic follow-up.
- APK metadata verified: `io.github.wanhanxi.aalyrics.debug`, version
  `1.2.2-dev+23a52dc.dirty`, code 1, debuggable, with a valid local Android Debug
  signature. The runtime matches `23a52dc`; later edits are documentation only.
  The previous local APK is no longer available for direct certificate comparison,
  so installation compatibility is a user-device handoff rather than a completed
  device check. Package/signing configuration is unchanged.
- The deliverable APK and SHA-256 are retained outside tracked source in local
  artifacts. Use the existing Debug export action; Verbose details is not required.
- Public documentation describes correlation limits and the incident-time/export
  procedure. New-device/real-car validation is intentionally left to the owner,
  who requested the APK for installation and later incident capture.
