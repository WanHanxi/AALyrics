# Application Composition

## Status

- Merged in PR #25.
- Main baseline after merge: `3ea97ce`.
- Classification: **REWRITE / REFACTOR** at the application boundary. Reuse the completed core/provider contracts; do not recreate the old `MediaTracker` monolith.
- The production graph is now established and is the input boundary for later live-media/runtime and presentation work.

## Purpose

The provider and core layers are composed into the first real application object graph. This slice connected those existing parts without starting presentation work or live Android media-session tracking.

The production object graph is:

```text
:app composition root
        |
        +--> LrcLibProvider
        +--> PetitLyricsProvider (only when configured)
        +--> MusixmatchProvider
        +--> SyncLrcProvider
        |
        +--> CrossProviderCandidateSelector
        |
        v
LyricsCoordinator
        |
        v
PlaybackLyricsController
        |
        v
LyricsState
```

At the time of PR #25, no UI consumed that state. Later runtime/presentation slices now consume application-owned state through presentation mapping; PR #50 hosts the production Phone shell from READY without changing the original composition ownership boundaries.

## Composition behavior

- `:app` is the composition root and depends on all four concrete provider modules plus `:provider:selection`.
- Composition is explicit/manual; no DI framework was added.
- One process-lifetime application graph and application-owned coroutine scope are used for the coordinator.
- LRCLIB, Musixmatch, and SyncLRC are instantiated behind the existing `LyricsProvider` contract. PetitLyrics is added only when its optional build configuration is complete.
- `CrossProviderCandidateSelector` is injected behind the existing `CandidateSelector` port.
- `LyricsCoordinator` is constructed from providers, selector, application scope and the `LyricsCache` port.
- Production composition supplies one `FileLyricsCache` from `:cache:files`, rooted at `noBackupFilesDir/lyrics_cache`. Tests may supply an isolated file cache or `LyricsCache.None`; the production graph factory requires an explicit cache. No storage policy or file operations move into the composition root.
- `PlaybackLyricsController` depends on `LyricsLookupLifecycle`, not concrete providers.
- The shared coordinator `LyricsState` / `StateFlow` is exposed for later runtime and presentation work.

## PetitLyrics configuration

The existing `BuildConfig` injection remains authoritative:

- `PETITLYRICS_USER_ID`
- `PETITLYRICS_APP_NAME`
- `PETITLYRICS_PKG_NAME`
- `PETITLYRICS_CLIENT_APP_ID`

`PetitLyricsConfig` is built from those values. All four blank disables the source; all four nonblank enables it; partial configuration fails the build with field names only. The graph omits an unconfigured PetitLyrics provider entirely, so it does not participate in requests, diagnostics, or the active-provider cache key. Secrets remain outside source/logs/tests. This introduces no durable preference or reset behavior change. See `docs/providers/PETITLYRICS.md` for configuration sources and precedence.

## Playback preference wiring

All enabled sources, including SyncLRC, participate regardless of `LyricsRequest.preferredSyncType`. Application composition carries the preference through playback-to-lookup ownership so the selector can give word timing priority only when Karaoke is enabled.

Current behavior:

- same track identity + same preferences -> do not restart lookup;
- same track identity + changed preferences -> start a fresh lookup;
- track identity change -> start a fresh lookup;
- position/status/rate/duration-only churn -> do not restart lookup;
- no active track -> clear lookup ownership.

Production starts without a word-timing preference and restores it only when both persisted Karaoke switches are ON. The Advanced experimental gate exposes the Quick-controls live switch; merely exposing it does not prefer WORD. Disabling either switch removes that priority. Updates preserve the independently selected translation language. Same-track changes check the corresponding preference-keyed cache before provider work.

## Explicitly not owned by composition

Application composition does **not** own:

- phone UI or `MainActivity` presentation;
- Android Auto presentation;
- live `NotificationListenerService` / `MediaSessionManager` runtime;
- process-wide lyrics-demand gating;
- cache format, I/O, eviction or invalidation policy;
- translation;
- artwork/colors;
- timing offset/calibration or karaoke rendering;
- provider scoring/selection policy;
- provider transport/search behavior.

The next independent slice after PR #25 was the live-media runtime documented in `docs/MEDIA_SESSION_RUNTIME.md`. Subsequent Phone presentation/runtime composition is documented in `docs/PHONE_RUNTIME_HOST.md`; this document remains the historical contract for the provider/core application graph itself.

## Validation record

PR #25 passed:

```text
./gradlew test check :app:assembleDebug
bash scripts/verify-architecture.sh
git diff --check
```

The first bounded review found the missing `android.permission.INTERNET` declaration for the newly composed network providers; that was fixed before merge. The second bounded review found no major issue.
