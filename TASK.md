# Precise manual lyrics offset

## Scope

- Continue `feature/lyrics-sync-offset` from `4a13ef8` in the personal fork. No PR or merge; preserve user-owned Gradle/SDK settings.
- Change fixed adjustment controls to ±50/500 ms.
- Make the displayed offset tappable to enter any signed integer millisecond value within the existing ±30,000 ms range.
- Prefill/select the saved value, open the keyboard, and provide a sign toggle for numeric keyboards without a minus key.
- Save (or keyboard Done) replaces the shared persisted offset; typing and dismissing never change it. Validate malformed and out-of-range input visibly.
- Opening the editor cancels unfinished three-tap calibration. Keep the current three-tap behavior, Reset, and shared Phone/Android Auto timing.
- No Undo, changes to tap sampling, per-song offsets, or unrelated features. The user will test through Android Studio; no exported APK.

## Checkpoints

- [x] Change fine adjustment to 50 ms with timing regression coverage and aligned docs.
- [x] Add exact-value editor, app callback, validation tests, and Preview coverage.
- [x] Verify tests/build, architecture, keyboard/dialog layout, reset semantics, and final docs; commit/push checkpoints.

## Acceptance and validation

- Four +50 ms actions move the paused preview across the existing 200 ms boundary; 500 ms and Reset retain their behavior.
- Exact values such as +125 and −275 ms persist and apply to the same shared clock. Zero and both range endpoints are valid.
- Empty/sign-only, decimal, nonnumeric, overflowing, and out-of-range values cannot be saved. Cancel/back/outside dismissal keeps the previous offset.
- Exact entry remains available without timed lyrics because it edits the global preference. The live preview and fixed-step controls retain their timed-lyrics requirements.
- No new durable state: Reset AALyrics still cancels calibration and restores this same offset to zero. Editor drafts are presentation state only.
- Previous three-tap implementation passed 765 tests plus build/architecture checks; the user subsequently reported successful phone and Android Auto testing.
- Fine-step checkpoint: `:app:testDebugUnitTest --tests '*PhoneSyncMapperTest'` passed (7 tests), including four +50 ms steps across the paused lyric boundary and retained coarse/reset behavior. `git diff --check` passed.
- Editor checkpoint: exact-value validation and persistence tests passed. Full `JAVA_HOME=/opt/android-studio/jbr ./gradlew test :app:assembleDebug --max-workers=2 --no-parallel --console=plain` passed (771 tests in 94 suites; zero failures/errors/skips). Architecture checks and `git diff --check` passed.
- Production UI verification on an isolated emulator: tapping the displayed number opened the dialog, selected the saved value, and displayed the numeric keyboard. Save persisted +125 ms; the sign toggle and keyboard Done persisted −275 ms. An out-of-range 30001 draft disabled Save and was rejected by keyboard Done. Cancel, Back, and outside dismissal retained −275 ms; reopening restored the saved value. Normal portrait screenshots were inspected.
- Narrow-screen runtime verification was attempted but the emulator failed after changing display size/density and font scale (QEMU hanging-thread/invalid-color-buffer errors, then emulator process exit). This is not recorded as a passing narrow-screen check. Narrow/large-font and landscape Compose previews are included; further real-device layout testing can be done through Android Studio.
- Final diff/docs/reset review passed. The temporary emulator exited and no emulator process remained. Only emulator-5580 was operated; the user's connected phone was not operated.
- Handoff: changes stay on the personal feature branch and are pushed without a PR. In Android Studio, Run the app, open Sync, and tap the value under LYRICS OFFSET. Use an exact signed millisecond value and Save/Done; the same persisted offset applies on Phone and Android Auto. No exported APK is required.
