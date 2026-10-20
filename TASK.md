# Three-tap lyrics synchronization

## Scope

- Continue `feature/lyrics-sync-offset` from `7161124`, preserving the existing ±100/500 ms controls, Reset, persistence, and shared Phone/Android Auto offset.
- Reference Auto Lyrics `854c5fa`, `MainActivity.onTapSyncPressed`: prompt three consecutive lyric starts and average three taps.
- Show three fixed upcoming nonblank timed lines, highlight the line to listen for, then save the average after the third tap. Allow cancel/restart.
- Compute each absolute offset as `lyric start - raw projected playback position` at the click's monotonic time; positive advances lyrics. Do not copy the upstream inverse-sign arithmetic.
- Stop incomplete calibration on track/lyrics changes, pause/rate changes, observed timeline jumps, navigation, manual adjustment, and reset. Never persist partial samples.
- Keep the existing ±30-second range; reject out-of-range calibration rather than silently applying it.
- No microphone permission, automatic audio matching, per-track offsets, providers, or Performance mode.
- Personal fork only: no PR or merge. Preserve user-owned Gradle/SDK settings.

## Checkpoints

- [x] Implement application-owned tap session, exact-time sampling, and regression tests.
- [x] Wire the three-line panel into Sync with preview coverage; retain existing manual controls.
- [x] Update user/architecture docs and reset semantics, run tests/build/architecture checks, inspect emulator layout.
- [x] Commit and push the tested implementation checkpoints to the personal fork; prepare the final documentation handoff and Android Studio phone testing instructions. The user has connected a phone and does not need an exported APK.

## Acceptance and validation

- Three taps average and replace the saved offset; existing offset is not added twice.
- Late/early examples, playback rate/source timestamp handling, skipped blanks/duplicate timestamps, and insufficient remaining lines are covered.
- Partial/cancelled/stale/seek-interrupted sessions cannot change the saved value. Manual controls and Reset still work.
- Unit/build checks and emulator evidence will be recorded separately from real-phone listening validation.

Core checkpoint: `:app:testDebugUnitTest` for `TapSyncCalibrationTest` and `LyricsTimingSettingsTest` passed (18 tests). Covers absolute sign/averaging, click-time clock projection, source updates, cancellation/stale input, range limits, and retained manual adjustment/reset.

UI checkpoint: presentation mapper tests and `JAVA_HOME=/opt/android-studio/jbr ./gradlew test :app:assembleDebug --max-workers=2 --no-parallel --console=plain` passed (765 tests, 93 suites, zero failures/errors/skips). Architecture guardrails and branch-wide `git diff --check` passed. The tap target uses fixed text sizing and a fixed marker column so the button stays in place as the highlighted line advances.

Visual verification: installed the build in a temporary emulator and rendered the active three-tap Compose fixture at ordinary width and at 320dp / 1.3× font scale. All three lines, current-line highlight, completed-line marker, tap/cancel actions, and retained manual controls render correctly. The PreviewActivity action bar overlays the fixture's top edge; it is not the production Phone shell. No runtime exception was logged. The temporary emulator was closed afterward; the user's connected phone was not operated.

Handoff: current local checkout remains on `feature/lyrics-sync-offset`; use Android Studio Run to update AALyrics Debug on the connected phone, play timed lyrics, then open Sync and start three-tap calibration. Real listening alignment and Android Auto host testing remain user/device validation. No exported APK, PR, merge, or unrelated feature work is included.
