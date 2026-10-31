# Integrate persistent lyrics cache into personal main

## Scope and authorization

- The owner authorized merging the completed cache feature into `WanHanxi/AALyrics:main` on 2026-10-31, independently of upstream review.
- Personal baseline: `origin/main` at `fd99084`, including manual global Sync controls, persisted timing offset and three-tap calibration.
- Cache source: `feature/persistent-lyrics-cache` at `6a99a87`, submitted as upstream PR #92 against `whoxamxl/AALyrics:main`.
- Prepare and validate the squash integration on `chore/integrate-lyrics-cache`, then fast-forward personal `main` and push to `origin/main` without another PR. No commit is authored directly on `main`.
- Preserve the fork's existing Sync behavior and workflow guidance. Keep the upstream cache/Sync contribution branches and diagnostics branch unchanged.

## Checkpoints

- [x] Fetch personal main and confirm the intended source revisions and clean starting worktree.
- [x] Combine the cache with existing Sync application wiring, reset copy and architecture/migration records.
- [ ] Complete architecture, scope, regression tests and Debug assembly for the combined tree within the workstation memory budget.
- [ ] Commit the validated integration on its topic branch, fast-forward personal main and push it without force.

## Integrated behavior

- Cache selected canonical original lyrics through `LyricsCache` and the separate JVM `:cache:files` adapter. Restore valid saved lyrics without provider requests across track changes and process recreation.
- Store versioned JSON in private no-backup storage with atomic writes, off-main-thread I/O and LRU bounds of 256 entries / 32 MiB total / 1 MiB per entry. No TTL, negative-result caching, prefetch or Translation caching.
- Retain stable catalog identity with conservative metadata fallback, selection/provider separation, fresh lookup IDs and cancellation protection.
- Keep Sync timing/calibration downstream of canonical lyrics; cached timestamps remain uncalibrated. `Reset AALyrics` restores the offset to zero and cancels calibration while preserving cached lyrics and downloaded Translation models.

## Validation

- Prior cache validation: `test :app:assembleDebug` passed at `12b9d23`, with 762 tests across 93 suites and no failures/errors/skips. The owner reported successful phone testing with no issues on 2026-10-31. Android Auto physical testing was not separately reported.
- Historical cache implementation/validation details remain in `6a99a87:TASK.md`; prior Sync validation remains in `3d13584:TASK.md`.
- Integration conflicts were limited to application imports/initialization, reset text, architecture/migration documentation and the task status. Resolution retains both the production file-cache injection and the existing tap-calibration lifecycle.
- Run combined-tree checks sequentially in a verified 3 GiB systemd scope with zero swap, one worker, no parallel execution/daemon reuse, 1 GiB Gradle heap and in-process Kotlin. No emulator is needed.
- Preserve the user-owned Gradle JVM file: SHA-256 `6c13aa5ba36690ccfbbb25a8807d01d98c15833e446d7ae82ddc726472cb8ef7`.

## Deferred and isolation

- Upstream review/merge, Translation caching, prefetch/refresh policy and cache-management UI remain separate follow-ups.
- Do not modify upstream PR #92 or #91, their source branches, or the unrelated diagnostics branch.
- This owner-authorized integration does not change the default topic-branch/PR workflow for future work.
