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

A supplied translation in the requested language receives preference when at
least one useful original row has a paired translation. There is no coverage
percentage gate: untranslated English, vocalizations and other gaps remain
original-only. This is an availability preference, not a translation-accuracy
score. Metadata must remain within .03 and lyric quality within .05 of the normal
synchronized winner. Sources are never merged or retimed.

All enabled sources, including SyncLRC, participate regardless of Karaoke. With
Karaoke off, WORD documents compete as ordinary synchronized lyrics, with no
word-timing confidence bonus. The existing experimental gate and Quick-controls
Karaoke switch must both be on to prefer usable WORD candidates within the same
identity/quality bounds. Supplied translations then receive preference within
that WORD group; if no suitable WORD exists, bilingual/ordinary LINE fallback
remains available. Rendering still needs real word timestamps. A switch can
change the current lyrics source, including from supplied bilingual LINE lyrics
to WORD lyrics without a supplied translation.

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

Selection-policy version 4 prevents old selected winners from masking the fix.
Cache schema stays 2, old files are not bulk-deleted, and reset semantics are
unchanged. The first lookup of an old-policy entry needs normal network retrieval;
subsequent valid hits still avoid provider work. Karaoke and translation
preferences are separate cache-key inputs; changing the effective Karaoke state
reselects the current track, with a normal provider lookup on a cache miss. No new
setting or model asset is introduced. Reset still turns both Karaoke switches off.

See `MATCHING_PERFORMANCE.md` for identical-input before/after measurements.
