# Android Auto-specific lyrics offset

## Scope

- Continue the personal `feature/lyrics-sync-offset` branch from `652b569`, preserving the established no-PR/no-merge handoff and user-owned Gradle/SDK files.
- Preserve the global offset for Phone lyrics/Karaoke, Details, and three-tap calibration. Android Auto consumes global + an independently persisted AA offset, with the same positive-advance/negative-delay convention.
- Keep the existing global preference key and default the new AA key to zero. Bound each setting to ±30 seconds; the car total is their unclamped sum (up to ±60 seconds).
- Add a compact AA offset card in Phone Sync with ±50/500 ms, exact entry, independent Reset, and the effective car total. Offer explicitly labelled Phone/Android Auto lyric previews using the shared timing engine.
- Each card Reset clears only its own setting. Reset AALyrics cancels calibration and resets both keys explicitly. Preserve canonical timestamps, real playback progress, provider/translation lifecycle, and downloaded models.
- Keep three-tap calibration global. Separate per-car/device profiles, AA-side controls, automatic calibration, and per-track/provider correction remain follow-up work.

## Checkpoints

- [x] Persist both offsets as one application-owned snapshot; wire the combined car offset and reset semantics; cover compatibility, bounds, independent changes/reset, and tap preservation.
- [ ] Add scoped controls/editor/preview mapping and normal/narrow/large-font/limits/reset previews; test Phone/car separation and preview timing.
- [ ] Align README, timing/Phone/Automotive/reset docs and stale Sync status; complete focused regression/build/architecture/diff validation, commit and push the handoff.

## Validation plan

- Run Gradle sequentially with `--max-workers=1 --no-parallel --no-daemon`, a 1 GiB heap / 512 MiB metaspace, and in-process Kotlin. Prefer a verified 3 GiB systemd scope with no swap; check host memory first.
- Reuse completed checks where source is unchanged. Do not start another emulator after the earlier bounded boot failure; compile Compose previews and provide a phone/AA testing handoff.
- Focused tests cover settings persistence/reset, tap calibration, exact input, Phone Sync/Details/lyrics, shared timing, and Automotive projection. Run architecture checks and branch-wide diff review.

## Status

- Checkpoint 1: 29 focused settings/tap/Sync mapper tests passed. Architecture checks and `git diff --check` passed. The Gradle scope verified `memory.max=3221225472` and `memory.swap.max=0`; the single-use daemon exited after the successful run.
- Initial worktree contained only the pre-existing untracked `gradle/gradle-daemon-jvm.properties`; it remains untouched.
