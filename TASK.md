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
- [x] Add scoped controls/editor/preview mapping and normal/narrow/large-font/limits/reset previews; test Phone/car separation and preview timing.
- [x] Align README, timing/Phone/Automotive/reset docs and stale Sync status; complete focused regression/build/architecture/diff validation and finalize the handoff.

## Validation plan

- Run Gradle sequentially with `--max-workers=1 --no-parallel --no-daemon`, a 1 GiB heap / 512 MiB metaspace, and in-process Kotlin. Prefer a verified 3 GiB systemd scope with no swap; check host memory first.
- Reuse completed checks where source is unchanged. Do not start another emulator after the earlier bounded boot failure; compile Compose previews and provide a phone/AA testing handoff.
- Focused tests cover settings persistence/reset, tap calibration, exact input, Phone Sync/Details/lyrics, shared timing, and Automotive projection. Run architecture checks and branch-wide diff review.

## Status

- Checkpoint 1: 29 focused settings/tap/Sync mapper tests passed. Architecture checks and `git diff --check` passed. The Gradle scope verified `memory.max=3221225472` and `memory.swap.max=0`; the single-use daemon exited after the successful run.
- Checkpoint 2: Sync/AA controls, scoped editor, independent resets, car total, and selectable timing previews compile, including debug Compose fixtures. All 36 settings/tap/Sync mapper/exact-input tests passed in the same verified memory budget. Reset tooltip/dialog copy and previews now cover both offsets.
- Checkpoint 3: README, changelog, migration inventory, and architecture/Phone/Automotive/reset documents now describe the implemented global/AA behavior and deferred work. Final validation passed: 105 freshly executed tests across app, Phone input, and Automotive, plus 21 unchanged core timing tests reused from successful up-to-date results. `:app:assembleDebug`, `scripts/verify-architecture.sh`, and branch-wide `git diff --check main` passed; the branch diff was reviewed for scope.
- Final verification ran sequentially inside `aalyrics-aa-offset-final.scope`, with verified `memory.max=3221225472` and `memory.swap.max=0`; reported peak memory was 2.1 GiB. All task-owned verification scopes exited. No emulator was started for this follow-up, and no successful build was repeated without a source change.
- Baseline ancestry was checked against `main` at `e85c21cbd2fdda62ed8ed9d947ff2a5889b01de4`; the remote baseline was rechecked and remained unchanged. Implementation checkpoints `57a6eda` and `ec6c686` were committed and pushed on the existing topic branch. This handoff does not open a PR or merge the branch.
- Compose debug previews compile, including narrow/large-font/limits/reset states. Actual rendered Phone/AA visual and device verification remains pending; the earlier bounded emulator boot failure is not treated as visual validation.
- Initial worktree contained only the pre-existing untracked `gradle/gradle-daemon-jvm.properties`; it remains untouched.

## Phone / Android Auto device handoff

- Set Global Offset to `+300 ms` and Android Auto Offset to `−750 ms`: Phone/Karaoke/Details use `+300 ms`, and the car/AA timing preview uses `−450 ms`. Changing AA compensation alone must leave Phone timing unchanged.
- Check each card's exact editor, ±50/500 ms controls, preview selection, and independent Reset. Verify scrolling and readability on a narrow display and at large font sizes.
- Restart the process and confirm both values persist. Complete three-tap calibration and confirm it changes only the global value, preserving AA compensation.
- Run Settings → Advanced → Reset AALyrics and confirm both offsets return to zero and any unfinished calibration stops. Downloaded models remain preserved.
