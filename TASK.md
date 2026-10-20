# Manual lyrics synchronization

## Scope

- Branch: `feature/lyrics-sync-offset`; baseline: `e85c21c`.
- Global calibration following upstream Auto Lyrics: positive advances lyrics; negative delays them.
- Phone Sync: ±100 ms, ±500 ms, signed millisecond value, Reset, and live original/translated preview.
- Persist across tracks and process restarts, bounded to ±30,000 ms.
- Apply once to Phone lyrics/Karaoke, Sync preview, verbose Details, and Android Auto.
- Reset AALyrics restores calibration to zero without clearing unrelated/external state.
- Per-track/car-specific offsets, tap/audio sync, Performance mode and new providers are deferred.
- Preserve user-owned `gradle/gradle-daemon-jvm.properties` and local SDK settings.

## Checkpoints

- [x] Persist calibration and connect timing consumers, with focused regression coverage.
- [ ] Add responsive Sync UI, live preview, empty/plain states, and Compose previews.
- [ ] Align reset copy and docs; run architecture checks, unit tests and debug APK build.
- [ ] Review changes, push checkpoints, open a Draft PR in the user's fork.

## Acceptance

- Immediate adjustment including paused playback; no player seek, timestamp mutation or provider requests.
- Correct sign in Phone and car, unchanged actual playback progress and Translation identity.
- No-media/loading/plain messages, retained-offset reset, consistent navigation/rotation/seek behavior.
- Installable debug APK with phone testing instructions; distinguish automated checks from device validation.

## Validation

Timing checkpoint: architecture guardrails and focused persistence, Phone lyrics/Details, and Automotive mapper unit tests passed. Full validation follows UI integration.
