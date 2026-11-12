# Multilingual lyrics matching

Personal baseline: `659c37f`, branch `fix/multilingual-lyrics-matching`, target
`WanHanxi/AALyrics:main`. Implementation and performance comparison authorized.
Duration scores/weight and bilingual coverage/tolerances stay unchanged.

## Checkpoints

1. Freeze reproducible synthetic CPU and request-latency baselines.
2. Normalize equivalent metadata, retain artist aliases, and require evidence for
   contributor/cross-script identity. Preserve recording versions.
3. Prefer reliable originals over suspected romanization; preserve mixed singing;
   bound LRCLIB alternatives; guard translation input; expose selection reasons;
   increment selection-policy cache version.
4. Run regressions, required checks and Debug build within workstation limits;
   compare identical benchmark inputs; update docs and review branch scope.
5. Push coherent checkpoints; open personal Draft PR only after completion. Stop
   before review, ready-for-review, merge or release.

## Acceptance

- [x] Traditional/simplified, aliases, feature credits and equivalent language
  version spellings match; unrelated artists and incompatible versions do not.
- [x] Mixed original/English lyrics retain quality; originals can beat romanization
  without widening bilingual thresholds or merging sources.
- [x] Ordinary LRCLIB exact match stays one request. Optional search is bounded
  and its failure cannot discard a healthy exact result.
- [x] Romanized model input remains original-only. Supplied translations still
  display below the preference coverage threshold.
- [x] Record regression/performance results with synthetic public fixtures;
  private playlist evidence and full lyrics stay outside the repository.
- [x] Final validation, documentation and branch-wide scope review complete.

Implementation is complete. The final workflow action is opening a personal
Draft PR, then stopping before review/ready/merge/release.

Reset: no new settings or persisted user state. Existing cache files survive;
selection-policy increment forces old winners to resolve again. No UI changes
or new Previews. Deferred: Russian machine translation, source picker, general
placeholder cleanup, duration changes and threshold tuning.

Checkpoint: metadata and all provider/selection regressions passed. Architecture
and whitespace checks passed. Baseline benchmark frozen before production changes.

Checkpoint: original-writing selection, bounded optional discovery, translation
script compatibility, cache invalidation and request-local diagnostics regressions
passed. Japanese lyric text informs source confidence when metadata is romanized;
English performance labels preserve the existing English-source preference.

## Final verification

- 948 unit tests across 20 modules passed, with no failures or skips; all three
  release-setup script tests passed. Architecture and whitespace checks passed.
- Debug APK assembled; verified both comparison tables and the Unicode license
  notice in the packaged APK. No physical-device or emulator validation claimed.
- The first combined compile/package run hit D8's 1 GiB heap ceiling. A fresh
  packaging-only run reused compilation and set `ActiveProcessorCount=2`; it
  passed without increasing the required heap or verified 3 GiB / zero-swap scope.
  Full unit tests then passed sequentially under the same limits. No IDE settings
  changed and no emulator was started.
- Identical synthetic fixtures, three alternating fresh-JVM runs: ordinary CPU
  median 0.245 -> 0.409 ms; all warm p95 values below 0.9 ms. First selection
  31.5 -> 57.9 ms. Controlled ordinary HTTP lookup 87.2 -> 88.3 ms, one request;
  suspected romanization 86.3 -> 176.6 ms, one -> two requests. See
  `docs/MATCHING_PERFORMANCE.md` for methods, other cases and limitations.
- Source configuration and existing duration/bilingual thresholds are preserved.
  No UI changes, new settings, machine-translation models, or reset target.
- Personal main remains `659c37f`; branch-wide diff contains only the intended
  matching/selection/translation guard, diagnostics, cache policy, tests,
  generated Unicode data and documentation. Private audit material is excluded.
