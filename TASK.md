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
- [ ] Align documentation and reset retention, run focused tests / architecture / build checks within the workstation budget, inspect the complete diff.
- [ ] Commit and push checkpoints; deliver a locally signed Debug APK and open a Draft PR targeting `WanHanxi/AALyrics:main`, then stop.

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
- No task emulator is running. Prefer compiled Compose previews and a phone handoff over optional resource-intensive emulator checks.

## Deferred

- Connection recovery / timeout UI changes, full system log collection, production log export, and any upstream contribution or merge.
