# Multilingual lyrics matching and Karaoke selection

Personal baseline: `659c37f`; branch `fix/multilingual-lyrics-matching`; target
`WanHanxi/AALyrics:main`; existing Draft PR #14. The owner authorized the original
matching implementation and performance audit, then the November 12 follow-up:
all enabled sources participate with either Karaoke state, word-timing priority
follows the existing switches, and the translation-coverage threshold is removed.

## Completed checkpoints

1. Froze the original CPU/request baseline and implemented catalog equivalence,
   identity corroboration, reliable-original preference, mixed-script handling,
   bounded LRCLIB alternatives, translation input guards and diagnostics.
2. Verified the original slice (948 unit tests, three script tests, Debug APK and
   architecture checks), documented comparisons, and opened personal Draft PR #14.
3. Removed the 80% translation gate and SyncLRC request gate; word-timing confidence
   and selection priority now require an explicit WORD preference. The 67 focused
   selector/SyncLRC/cache tests passed; this checkpoint was committed and pushed.
4. Connected the existing persisted experimental/live Karaoke switches to selection.
   Translation preference updates preserve Karaoke and vice versa. Every enabled
   source remains eligible. Added integration coverage for all-source participation,
   preference-keyed disk-cache reuse, restoration, switch reset and demand suspension.
5. Completed 957 unit tests across 20 modules, three script tests, architecture and
   whitespace checks, and Debug assembly. Reused the successful APK after test-only
   benchmark changes. No emulator or physical-device claims.
6. Compared frozen `68486d7` and updated production with identical synthetic inputs,
   including mixed LINE/WORD candidates and both Karaoke preferences. Ordinary mixed
   selection p50 was 0.522 -> 0.524 ms (off) and 0.562 -> 0.506 ms (on); p95 was
   0.997 -> 1.045 ms and 0.819 -> 0.900 ms respectively. See MATCHING_PERFORMANCE.md.
   Private catalog replay and targeted live checks remain outside the repository.

## Final behavior and scope

- Reliable original writing and recording identity still precede preferences.
- Duration boundaries/weight and metadata/quality tolerances (.03/.05) are unchanged.
- Any useful paired target-language translation can earn bilingual preference;
  no new coverage percentage, language detector or minimum-line threshold replaces 80%.
- Karaoke OFF: synchronized WORD and LINE lyrics compete without word-timing bonuses.
- Karaoke ON (both existing switches): comparable usable WORD lyrics take priority;
  supplied translations are preferred within that group, with LINE fallback.
- All enabled sources, including SyncLRC, are queried for either preference.
- Switching reselects the current track using its own preference-keyed cache;
  a first miss performs normal lookup. No new cache of private candidate collections.
- Cache schema remains 2; selection policy is 4. Existing files are retained.
- Reset continues to turn both Karaoke switches OFF and reset the existing source
  preferences separately. No new durable setting, model download or permission.
- Existing layouts/Preview states remain applicable; Karaoke info text and contracts
  now explain that its live switch may change the selected lyrics source.

Deferred: Russian machine translation, source picker, broad language identification,
general instrumental/credit cleanup, candidate completeness/timestamp heuristics,
duration changes and further metadata-tolerance tuning. These are not silently
included in this follow-up.

## Workflow boundary

Final local validation and branch-scope inspection are complete. Delivery remains
on the existing personal Draft PR #14, including the app-wiring and documentation
checkpoint. Stop before review requests, ready-for-review, merge or release without
user authorization.
