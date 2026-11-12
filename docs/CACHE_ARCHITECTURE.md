# Lyrics Cache Architecture

## Purpose

Define the cache boundary and the persistent offline lyrics implementation selected after inspecting the legacy cache and current retrieval/selection inputs.

The reference `GitUpGitUp/auto-lyrics` main at `854c5fa` was inspected for this slice: `lyrics/LyricsCache.kt` and the read/refresh/prefetch call sites in `media/MediaTracker.kt`. Implementation progress and validation are recorded in `TASK.md`.

## Stable ownership rules

Cache is a data-access capability. It is not presentation state, provider ranking policy, MediaSession state, or translation/timing policy.

Stable direction:

```text
lyrics/application policy
        ↓
cache abstraction
        ↓
storage adapter
        ↓
platform/storage technology
```

Rules:

- core/application logic may depend on a cache abstraction, never an Android database/preferences implementation;
- Phone and Android Auto UI must not read or write cache storage directly;
- concrete providers must not own global cache policy;
- provider-specific transport DTOs must not become the durable cross-feature cache contract;
- cache hits must still respect current request identity, selection preferences, stale-result protection, and cancellation ownership;
- cache use must not allow obsolete track results to replace current state;
- persistence failures must degrade to normal retrieval rather than corrupt lyrics lifecycle state.

## Canonical data and derivation

A cache must preserve the distinction between canonical fetched lyrics and later derived capabilities.

```text
provider result / normalized lyrics facts
        ↓
canonical cacheable material
        ↓
selection / translation / calibration / karaoke projections
```

Generated machine Translation, user timing calibration, active karaoke position, and surface-specific UI state must not be silently folded into one opaque lyrics cache entry.

Where useful, those capabilities may later define their own associated cached artifacts keyed to canonical lyrics identity, but that is a later implementation decision.

## Translation cache policy

Persistent generated Translation Cache is explicitly outside the current implementation path. The November 2026 NetEase slice explicitly persists source-supplied translations and their attribution alongside the paired original document; those are fetched source facts, not a cache of machine-generated artifacts.

- Through stable `v1.0.0`, persistent Translation Cache must not be introduced.
- After `v1.0.0`, it remains disabled unless explicitly authorized.
- This policy does not prohibit transient in-memory state required to coordinate one active Translation request or one process-level ML Kit model download.
- If Translation Cache is authorized later, it remains a derived-artifact cache keyed to canonical lyrics identity plus the Translation configuration/engine/version factors required for correctness; it must not be folded invisibly into canonical lyrics storage.

The purpose is observability during development: an old translated artifact must not make a changed Translation algorithm appear fixed or broken.

## Legacy behavior classification

| Reference behavior | Decision | Current adaptation |
| --- | --- | --- |
| Read disk before requesting providers; reuse lyrics after restart | PRESERVE | A valid hit completes without contacting any lyrics provider. |
| Save selected lyrics, word timing and source | PRESERVE / REFACTOR | Save the complete canonical `LyricsDocument`, including optional line/word end times, language and structured attribution. |
| Non-fatal storage failures and corrupt-entry removal | PRESERVE | Read failure falls through to retrieval; write failure never hides resolved lyrics. Cancellation is not a cache failure. |
| Context-owned file storage | REFACTOR | The application supplies a no-backup directory to a separate JVM `:cache:files` adapter; pure core sees only `LyricsCache`. |
| Lowercased title/artist joined with a separator and 32-bit hash | REFACTOR | Versioned structured request identity, SHA-256 filenames, exact key verification and richer recording/selection inputs. |
| Direct writes and unbounded retention | REFACTOR | Atomic replacement, bounded entry size and least-recently-used capacity limits. |
| Age-based background refresh and queue prefetch in `MediaTracker` | DROP from this slice | No expiry or background refresh/prefetch. An old valid entry stays useful offline until capacity eviction. These policies require separate work. |
| Persisting legacy status alongside lyrics | DROP | Persist usable documents only; no negative results, provider failures, lookup IDs or UI status. |

## Placement and lifecycle

The primary cache unit is the **selected normalized lyrics document**, after cross-provider candidate selection. This restores the old successful-result reuse without persisting provider transport DTOs or duplicating candidate-selection policy.

`LyricsCoordinator` invokes the replaceable `LyricsCache` port before provider fan-out and saves the selected document before publishing Ready/Degraded. It does not own file paths, serialization, eviction or expiry. Persistence finishes before publication so immediately switching away from visible lyrics does not cancel their pending save. I/O failure still allows publication. Cache hits publish Ready with a fresh lookup identity and diagnostics identifying a cache hit with zero provider attempts; historical provider failures are not replayed.

The same coroutine and atomic state reducer govern cache and network work. Cancellation is propagated, and cancellation is rechecked after cache operations so an obsolete/non-cooperative read cannot trigger provider work or publish stale lyrics. Demand gating remains upstream of lookup. Phone and Android Auto share the process-owned cache through the existing application graph.

## Request identity and invalidation

The encoded key includes:

- when present, all stable catalog references sorted by namespace/value, with opaque values preserved;
- otherwise normalized title, ordered artist names, album and album artist (trimmed and lowercased with `Locale.ROOT`), plus exact optional duration in milliseconds;
- the preferred synchronization type and optional translation target language, including distinct unset preferences;
- the sorted active lyrics-provider IDs;
- a cache format/selection-policy version.

Stable catalog references identify the recording, consistent with `PlaybackSnapshot.trackIdentity` and the existing corrected-metadata playback tests. With identical references, a later title correction or known duration reuses the document. Different references, selection preferences or provider sets miss; reference/provider set order does not matter. Without references the metadata fallback is conservative: album/version/duration changes miss rather than fuzzy-match another recording, and unknown/known duration remain distinct. Adding or removing catalog identity also misses; there is no weaker-key alias fallback. Playback position, transient player session identity, calibration and the machine-fallback switch are not inputs. The enabled target-language preference is an input because it can change the winning original. Provider or selector changes that invalidate previous winners must bump the cache policy version.

Active provider IDs mean the enabled subset, not every registered adapter. NetEase is excluded by default. Disabling it cancels the current lookup and clears retained state; the subsequent lookup uses a key without NetEase and cannot read a NetEase-enabled winner. Reset also disables it and clears acknowledgement. Existing cache files survive both actions and may be reused after explicit re-enablement; no schema change or bulk deletion is needed for this gate.

Only documents containing nonblank lyric text are stored. Both full and partially successful provider runs may supply usable cached lyrics. NotFound, failures and blank/empty documents are never cached. Schema/key mismatches, malformed data and invalid model invariants invalidate an entry and fall through to normal retrieval. There is no time-based expiry or automatic revalidation in this slice; selection preference changes use a distinct entry instead of overwriting another variant.

## File adapter and retention

- `:cache:files` is a JVM storage adapter depending on the core cache port, not the reverse. It uses explicit versioned JSON encoding/decoding and reconstructs validated domain models.
- Production storage is `noBackupFilesDir/lyrics_cache`, not Android's disposable `cacheDir`. The adapter constructor performs no cache-directory creation or file reads; operations use the I/O dispatcher.
- A single process-owned adapter serializes its operations. Writes use a same-directory temporary file, flush/sync and atomic replacement; unsuccessful replacement preserves the previous entry. Interrupted temporary files are cleaned on the next adapter access.
- Limits: 256 documents, 32 MiB total payload and 1 MiB per entry. Read access updates recency; writes evict the least recently used entries when necessary. Oversized documents remain displayable but are not cached.
- Valid entries survive process/activity recreation, ordinary app updates and Reset AALyrics. Reset restores settings and intentionally preserves reusable downloaded lyrics, consistent with the downloaded-assets policy.
- Capacity eviction, Android Clear storage or uninstall removes cached lyrics. The cache is excluded from device backup by its no-backup location. It is not a chronological listening-history feature, but retained lyrics and metadata can identify previously requested songs; the Privacy Policy describes that storage.

## Acceptance and deferred work

Validate exact canonical PLAIN/LINE/WORD round trips; A → B → A while offline and application-graph recreation; provider-call avoidance on hit; recording/preference/provider-set separation; corruption/version mismatch; capacity/oversize behavior; non-fatal I/O failures; cancellation/stale reads and writes; and unchanged provider failure isolation and demand gating.

Persistent machine-generated Translation caching remains prohibited by the policy above. Prefetching, expiry/background refresh, cache management UI, cross-version migration, and provider ranking changes are not part of this slice. Follow-up work must preserve the identity, lifecycle, storage and reset boundaries here.

## NetEase schema transition

The NetEase transition introduced schema and selection policy 2. The multilingual update below advances selection policy only. The paired translation, nullable per-row texts, contributors and notices round-trip with the original. Different translation preferences cannot share a winner. Prior version keys are misses; no bulk cache deletion is performed and capacity eviction still owns old-file retention. Reset preserves lyric caches, including supplied translations, just as it preserves original-only entries.

Source translations containing literal replacement characters remain usable cached facts. The bilingual tie-break favoring equally scored translations without these characters does not invalidate existing winners or change cache versions: new retrievals use it, while existing entries remain available offline, including their upstream text defects. It does not silently refresh or rewrite previously cached lyrics.

## Multilingual selection policy update

Selection policy 4 invalidates hits for older selected winners, including romanized
lyrics selected before policy 3 and results selected under the former translation
coverage/always-WORD policy. Schema remains 2. Stored files are not bulk-deleted or
rewritten; ordinary cache eviction still applies. Karaoke OFF and ON use separate
preference keys. Reset removes the existing Karaoke preference but preserves cache
files; no new reset target or durable setting is introduced.
