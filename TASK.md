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

- [ ] Traditional/simplified, aliases, feature credits and equivalent language
  version spellings match; unrelated artists and incompatible versions do not.
- [ ] Mixed original/English lyrics retain quality; originals can beat romanization
  without widening bilingual thresholds or merging sources.
- [ ] Ordinary LRCLIB exact match stays one request. Optional search is bounded
  and its failure cannot discard a healthy exact result.
- [ ] Romanized model input remains original-only. Supplied translations still
  display below the preference coverage threshold.
- [ ] Record regression/performance results with synthetic public fixtures;
  private playlist evidence and full lyrics stay outside the repository.
- [ ] Final validation, documentation, scope review and Draft PR complete.

Reset: no new settings or persisted user state. Existing cache files survive;
selection-policy increment forces old winners to resolve again. No UI changes
or new Previews. Deferred: Russian machine translation, source picker, general
placeholder cleanup, duration changes and threshold tuning.
