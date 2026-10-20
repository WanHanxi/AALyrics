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
- [ ] Wire the three-line panel into Sync with preview coverage; retain existing manual controls.
- [ ] Update user/architecture docs and reset semantics, run tests/build/architecture checks, inspect emulator layout.
- [ ] Commit coherent checkpoints, push to the personal fork if authentication is available, and provide Android Studio phone testing instructions. The user has connected a phone and does not need an exported APK.

## Acceptance and validation

- Three taps average and replace the saved offset; existing offset is not added twice.
- Late/early examples, playback rate/source timestamp handling, skipped blanks/duplicate timestamps, and insufficient remaining lines are covered.
- Partial/cancelled/stale/seek-interrupted sessions cannot change the saved value. Manual controls and Reset still work.
- Unit/build checks and emulator evidence will be recorded separately from real-phone listening validation.

Core checkpoint: `:app:testDebugUnitTest` for `TapSyncCalibrationTest` and `LyricsTimingSettingsTest` passed (18 tests). Covers absolute sign/averaging, click-time clock projection, source updates, cancellation/stale input, range limits, and retained manual adjustment/reset.
