# Debug media connection diagnostics

## Scope

- Personal branch `feature/media-connection-diagnostics`, based on personal `main` `fd99084`.
- Diagnose intermittent long-lived Connecting with Spotify on Xiaomi 15 Pro / Android 16, on both Phone and Android Auto, without a computer at reproduction time.
- Debug builds automatically persist bounded connection events and freeze recent context when Connecting lasts 30 seconds during active lyrics demand.
- Export on the phone through Settings > Advanced > Debug and Android's document picker.
- Preserve current session selection, discovery, rebind, provider and release behavior. This is an evidence-gathering build, not a speculative connection fix.

## Checkpoints

- [x] Add platform diagnostic events, bounded persistent storage and focused regression tests.
- [x] Wire Debug-only process monitoring, automatic stall captures, phone export and Compose previews.
- [x] Align documentation and reset retention, run focused tests / architecture / build checks within the workstation budget, inspect the complete diff.
- [x] Commit and push implementation checkpoints; prepare the locally signed Debug APK.
- Publication gate: implementation, validation and APK handoff are complete. After committing this delivery record, the final action is to open a Draft PR targeting `WanHanxi/AALyrics:main` and stop.

## Acceptance and retention

- Events distinguish process startup, notification access, listener create/connect/disconnect/destroy, session observation/selection, rebind requests/results and exceptions.
- Observe Phone and Android Auto through the shared application/platform runtime; no dependency on an open Phone screen for capture.
- File writes run off the main thread; event backlog and disk use are bounded. Logging failures must not break playback.
- Record only diagnostic metadata (timestamps, build/device information, package names, states and exception types); no notification body, track title, lyrics, media identifiers, credentials or raw exception messages.
- Export includes previous process events and retained stall captures; cancellation/failure has a clear outcome.
- Debug diagnostics intentionally survive Reset AALyrics to retain evidence across troubleshooting. They stay under no-backup app storage and rotate automatically; clearing app storage/uninstall removes them. User-exported files remain user-owned.
- Release builds do not initialize recording or show the export action.

## Validation

- Checkpoint 1: platform Debug tests and focused app `*Connection*Test` passed (31 seconds) in a verified 3 GiB / zero-swap scope.
- Checkpoint 2: all app, media and Phone Debug unit tests passed (35 seconds); Debug Compose previews compiled. Architecture and whitespace checks passed.
- Run Gradle sequentially in a verified 3 GiB systemd scope with no swap, one worker, no parallel tasks, no daemon reuse, 1 GiB JVM and in-process Kotlin.
- Final `test :app:assembleDebug` passed in 40 seconds at implementation commit `a269af3`; 780 unit tests across 98 suites, no failures/errors/skips. Existing successful tasks were reused.
- Branch-wide architecture checks and `git diff --check` passed. Personal main remains `fd99084`, and the reserved upstream-contribution branch remains `3d13584`.
- APK package/version/signature validated with aapt2 and apksigner. Package `io.github.whoxamxl.aalyrics.debug`, version `1.0.0-beta.1-dev+a269af3`, local Android Debug signer SHA-256 `bbcffafa7296076c0a8c9565798a83c49f4860856e6743d758fa4d5aad1f5703`.
- APK: `app/build/outputs/apk/debug/aalyrics-connection-diagnostics-a269af3.apk` (92,051,127 bytes). SHA-256 `67f49678c2cfc56e756ef282d8dca8213e1ebef71c1ba1c60113ac49cce5fe14`.
- All builds used verified `MemoryMax=3221225472`, `MemorySwapMax=0`, one worker, no parallel tasks, no daemon reuse and in-process Kotlin. The final scope is inactive/dead; no Gradle/Kotlin daemon or emulator remains. User-owned Gradle JVM file checksum remains `6c13aa5ba36690ccfbbb25a8807d01d98c15833e446d7ae82ddc726472cb8ef7`.
- No emulator was launched. New Compose previews compiled, but on-device export UI and the Xiaomi/Spotify/Android Auto reproduction still require the phone handoff below. No claim of reproducing or fixing the connection fault is made.
- Only delivery documentation changes follow the validated implementation; reuse the successful APK rather than rebuilding for those changes.

## Phone handoff

1. Install the new APK over the existing local AALyrics Debug installation and open it once. Recording starts automatically; Verbose details is independent.
2. Use Spotify and AALyrics normally, including Android Auto. No action is required during a Connecting stall.
3. Later open Settings > Advanced > Debug > Export connection diagnostics, choose Downloads (or another destination), and wait for the saved confirmation.
4. Share the saved text file and the approximate incident time. The report retains earlier process records and the last three automatic stall captures within the bounded storage budget. It contains app diagnostics, not a full system bug report.

## Deferred

- Connection recovery / timeout UI changes, full system log collection, production log export, and any upstream contribution or merge.
