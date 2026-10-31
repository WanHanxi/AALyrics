# Merge media connection diagnostics into personal main

## Scope and authorization

- On 2026-10-31, the owner requested merging `feature/media-connection-diagnostics` into personal `main` and deleting the already-integrated `chore/personal-fork-workflow` branch.
- Personal baseline: `d78894f`, including Sync calibration and persistent offline lyrics caching. Diagnostics source: `17ab7d8`, based on the earlier personal main `fd99084`.
- Preserve the diagnostics branch ancestry with an ordinary merge, following the owner's stated preference for a visible feature-to-main merge relationship. Do not squash or rewrite published history.
- Prepare the merge on local temporary branch `chore/merge-media-diagnostics`, validate it, fast-forward personal main to the completed merge and push to `WanHanxi/AALyrics:main`. Remove the temporary branch after delivery. No additional PR is needed for this authorized integration.
- Keep the upstream cache and Sync contribution branches unchanged.

## Checkpoints

- [x] Fetch main/source/cleanup branches and confirm source revisions and a clean starting worktree.
- [x] Verify the workflow branch is fully reachable from main; delete its local and remote references.
- [x] Combine diagnostics with the existing cache and Sync behavior; retain both storage policies and reset disclosures.
- [ ] Validate the combined tree with architecture/scope checks, all tests and a Debug build within the workstation memory budget.
- [ ] Verify the merge parents and prepare the validated result for personal-main delivery and temporary-branch cleanup.

## Integrated behavior

- Debug-only process monitoring records bounded media connection events and captures prolonged Connecting episodes while lyrics demand is active. The Phone export action saves a report through Android's document picker.
- Recording observes existing selection, rebind, discovery and playback state. This feature gathers evidence; it does not claim to fix the Xiaomi/Spotify connection problem.
- Diagnostic records exclude track titles, lyrics, media identifiers, notification contents, credentials and exception messages. Recording/export is not initialized or exposed in Release builds.
- Reset restores the existing settings and Sync offset while retaining reusable cached lyrics, Translation models and bounded diagnostics. Each storage feature keeps its own private no-backup directory.

## Validation and provenance

- Source diagnostics validation at `a269af3`: 780 tests across 98 suites and Debug assembly passed. Source implementation, privacy/reset decisions and phone handoff are recorded in `17ab7d8:TASK.md`.
- Cache plus Sync integration at `e763163`: 795 tests across 97 suites and Debug assembly passed. The owner previously reported successful phone testing of the cache feature; that is not a claim of testing diagnostics export or reproducing the connection fault.
- Only task status and adjacent reset comments/event recording conflicted. Keep both feature behaviors; no functional changes beyond the source diagnostics branch are planned.
- Run Gradle sequentially in a verified 3 GiB / zero-swap systemd scope with one worker, no parallel execution/daemon reuse, 1 GiB Gradle heap and in-process Kotlin. No emulator is needed for this integration.
- Preserve the user-owned Gradle JVM file checksum: `6c13aa5ba36690ccfbbb25a8807d01d98c15833e446d7ae82ddc726472cb8ef7`.

## Delivery and deferred work

- Use a fast-forward to the validated merge on personal main and an explicit non-force push to `origin main:main`; the merge commit retains personal main as first parent and diagnostics `17ab7d8` as second parent.
- Keep `feature/media-connection-diagnostics` available; only the owner-named workflow branch and task-owned temporary integration branch are cleaned up.
- On-device diagnostics export and Xiaomi/Spotify/Android Auto incident capture remain phone handoff checks. Connection recovery changes, production diagnostics and upstream work remain separate tasks.
