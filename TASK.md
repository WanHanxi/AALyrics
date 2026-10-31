# Persistent offline lyrics cache

## Baseline and scope

- Branch `feature/persistent-lyrics-cache`, based on freshly fetched `upstream/main` at `e85c21c`.
- Restore previously resolved lyrics after A → B → A and process recreation without provider requests.
- Reference inspected: `GitUpGitUp/auto-lyrics` main `854c5fa`, `lyrics/LyricsCache.kt` and `media/MediaTracker.kt` cache/read/refresh/prefetch call sites.
- Keep the change suitable for a later upstream contribution; no unrelated personal Sync or connection-diagnostics changes.

## Decisions

- Cache selected canonical lyrics behind a core port and a separate JVM file-storage adapter. Preserve PLAIN/LINE/WORD data, optional end times, language and attribution.
- Cache-first: a valid hit completes a fresh lookup without networking. Save only positive usable results, including results obtained despite another provider failing. Never persist failures or NotFound.
- Identity includes normalized request metadata (title, ordered artists, album, album artist, exact optional duration), sorted catalog references, selection preferences and the active provider set. Preserve opaque reference values. Conservative metadata changes miss rather than risk the wrong recording. Version the format/selection policy.
- App-private no-backup storage, atomic replacement, off-main-thread I/O, 256 entries / 32 MiB with least-recently-used eviction and a 1 MiB entry limit. No age expiry, automatic refresh or prefetch, so age alone cannot remove offline availability.
- Reset AALyrics preserves the cache. Eviction, clearing app storage or uninstall removes it. Translation output and calibrated/UI state are never persisted here.

## Checkpoints

- [x] Core cache contract and lookup integration, with lifecycle/failure/preference tests.
- [ ] Versioned bounded file adapter and application wiring, with restart/offline/corruption/eviction tests.
- [ ] Documentation/privacy/reset alignment, architecture checks, full tests and Debug build; branch-wide regression review.
- [ ] Commit and push completed checkpoints; prepare upstream-ready handoff.

## Validation constraints

- Run Gradle sequentially in a verified 3 GiB systemd scope, zero swap, one worker, no parallel tasks/daemon reuse, 1 GiB Gradle heap and in-process Kotlin.
- No emulator is needed for this storage/lifecycle change. Preserve the user's generated Gradle JVM settings.
- Required regressions: cache hit avoids every provider; selection/provider/recording identity changes miss; fresh lookup IDs; stale/cancelled reads cannot publish or start providers; failed cache I/O does not break lookup; persisted canonical round-trip and graph recreation; demand gating and partial-provider failures remain correct.

## Deferred

- Translation caching, prefetch, network refresh/TTL policy, cache-management UI, provider ranking changes and upstream merge.

## Validation record

- Checkpoint 1: `:core:lyrics:test` passed in 24 seconds in verified 3 GiB / zero-swap scope; architecture and whitespace checks passed.
