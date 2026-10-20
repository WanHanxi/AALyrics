# Manual lyrics synchronization

## Scope

- Branch: `feature/lyrics-sync-offset`; baseline: `e85c21c`.
- Global calibration following upstream Auto Lyrics: positive advances lyrics; negative delays them.
- Phone Sync: ±100 ms, ±500 ms, signed millisecond value, Reset, and live original/translated preview.
- Persist across tracks and process restarts, bounded to ±30,000 ms.
- Apply once to Phone lyrics/Karaoke, Sync preview, verbose Details, and Android Auto.
- Reset AALyrics adds calibration zeroing to its existing explicit app-owned reset contract.
- Per-track/car-specific offsets, tap/audio sync, Performance mode and new providers are deferred.
- Preserve user-owned `gradle/gradle-daemon-jvm.properties` and local SDK settings.

## Checkpoints

- [x] Persist calibration and connect timing consumers, with focused regression coverage.
- [x] Add responsive Sync UI, live preview, empty/plain states, and Compose previews.
- [x] Align reset copy and docs; run architecture checks, unit tests and debug APK build.
- [x] Review branch-wide changes and push the completed implementation to the personal fork.

## Acceptance

- Immediate adjustment including paused playback; no player seek, timestamp mutation or provider requests.
- Correct sign in Phone and car, unchanged actual playback progress and Translation identity.
- No-media/loading/plain messages, retained-offset reset, consistent navigation/rotation/seek behavior.
- Installable debug APK with phone testing instructions; distinguish automated checks from device validation.

## Validation

Timing checkpoint: architecture guardrails and focused persistence, Phone lyrics/Details, and Automotive mapper unit tests passed.

UI checkpoint: `./gradlew test :app:assembleDebug --max-workers=2 --no-parallel` passed with 748 tests, zero failures/errors/skips. Architecture checks and `git diff --check` passed.

Emulator checks: Debug APK installation, onboarding-to-Sync navigation, timed/translated Compose fixture rendering, saved +300 ms restoration after process restart, and the actual Reset button persisting/displaying zero all passed. A 320dp / 1.3× font check exposed button-label wrapping; reduced horizontal padding and explicit body text size fixed it. Rebuilt and reinstalled the APK, verified readable controls, and scrolled to reach Reset and the preview. Real phone/audio and Android Auto verification remain manual follow-up.

Delivery: implementation, documentation alignment, branch-wide scope review, and automated/emulator validation are complete. The completed branch is pushed to the personal fork and a verified debug APK is available. Following the user's personal-use direction, handoff does not require a pull request; nothing is submitted to the original repository. No merge or additional feature work is included.
