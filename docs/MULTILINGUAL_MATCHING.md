# Multilingual matching policy

## Recording identity

Comparison adds NFKC, compact ICU-derived Traditional/Simplified variants, explicit
feature-credit removal, and equivalent language-version spellings. Original
metadata and lyric text are never rewritten. Live/remix/instrumental and explicit
performance-language conflicts remain rejection gates, including album language
context. The compact tables add about 29 KB of uncompressed resources and no new
runtime dependency; provenance/regeneration and Unicode licensing are documented
in `THIRD_PARTY_LICENSES.md` and `scripts/generate-match-folds.py`.

Artist aliases belong to their catalog entity. Bilingual display names can match
both names of that same entity. Structured participants are compared one-to-one;
an ampersand spelling is expanded only if all participants match the provider's
separate artists. Cyrillic transliteration is supporting evidence only: it needs
an exact participant anchor, complete participant coverage, near-exact title and
strong existing duration evidence. An exact primary artist plus near-exact album,
title and duration may tolerate differing secondary catalog/label credits.
Legacy flat contributor lists retain their limited subset compatibility; band
names containing ampersands are not split for that fallback.

Cross-script mismatch can still be corroborated by an artist-constrained query
or album evidence. Duration alone no longer excuses an unrelated artist. This is
heuristic matching, not proof of recording identity or a universal alias database.

Duration scoring is unchanged: <=2s 1.00, <=4s .92, <=7s .78, <=10s .60,
<=15s .35, beyond 15s rejected; base weight .12. Language/version and identity
checks run separately from duration.

## Original writing and translations

`LyricsWriting` inspects actual lyric rows. Latin script alone is not evidence of
romanization. Repeated lexical evidence supports Japanese/Korean/Russian signals;
Japanese additionally uses syllable patterns and particles across multiple rows.
Repeated rows share analysis within one document. Unknown writing stays unknown.
This is deliberately a limited heuristic, not a language model or transliterator.

Within an eligible timing tier, a native original may displace a suspected
romanization when native metadata is at least .90 and within .03 of the romanized
candidate, with quality within .05. Without a suitable original the romanized
lyrics remain available. Genuine mixed-language singing is not penalized simply
for alternating scripts. Only repeated near-coincident native/romanized row pairs
receive the mixing penalty. Source confidence recognizes Japanese from lyric text
even when every metadata field is Latin; explicit English versions retain the
English-source preference. Native-script evidence never overrides version checks.

Bilingual preference remains >=80% useful-row coverage, within .03 metadata and
.05 quality of the normal synchronized winner. A partial supplied translation
still displays when its original wins normally. Sources are never merged or
retimed to manufacture a bilingual document. Some otherwise valid translations
can still miss these unchanged preference gates.

LRCLIB keeps its ordinary one-request exact fast path. A strong exact result
suspected of romanization may trigger one extra title/artist search without album,
with a one-second coroutine deadline. Optional failure/timeout retains the exact
result and parent cancellation propagates. Discovery returns at most three
writing variants within .03 of its best local score. NetEase's existing search
and three-lyric-fetch budgets remain unchanged.

Translation planning preserves Latin-only rows classified ja/ko/zh instead of
feeding incompatible writing to native-script models. English classified as
English remains eligible. Supplied translations bypass model planning as before;
Russian machine translation and general misidentification remain outside scope.

## Diagnostics and cache

The shared metadata scorer can explain title, artist, duration or version
rejection. Global selection returns bounded request-local decisions, metadata and
quality scores, and writing signals. The coordinator retains at most 64 records,
without lyric text, artist names or persistent logs. Provider-local discarded
search results are not exported by this diagnostics list.

Selection-policy version 3 prevents old selected winners from masking the fix.
Cache schema stays 2, old files are not bulk-deleted, and reset semantics are
unchanged. The first lookup of an old-policy entry needs normal network retrieval;
subsequent valid hits still avoid provider work. No new UI, setting or model asset.

See `MATCHING_PERFORMANCE.md` for identical-input before/after measurements.
