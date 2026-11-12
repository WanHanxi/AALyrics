# NetEase provider profile

## Scope

Anonymous, bounded retrieval of line-timed original lyrics and existing Chinese translations. No official third-party API agreement or content license has been established. This is an independent Kotlin adapter, not a dependency on a Node/.NET lyrics library.

## Transport and discovery

Application opt-in is required: **Settings > Lyrics > NetEase Cloud Music** defaults OFF and requires the first-enable source notice. Choice/acknowledgement survive ordinary upgrades; explicit reset clears both. The application supplies the effective provider set to lookup ownership and the coordinator, which excludes disabled providers from requests, candidate selection and cache keys. Disabling cancels active work and clears retained results, including during suspended demand. Cached files survive but are inaccessible while disabled. The adapter and other providers do not own this policy.

- Search `https://music.163.com/api/cloudsearch/pc` with title and artist, at most 10 results. A title-only fallback is allowed only when the first query produces no plausible candidates.
- Shared `MetadataMatchScore` validates title, artist, album, duration and recording version before fetching at most three candidate lyric responses. Artist `alias`/`alia` names are retained per catalog entity through both local filtering and global selection. Comparison supports Traditional/Simplified, explicit feature credits and equivalent language-version labels; display metadata stays unchanged. Explicit Chinese instrumental labels such as `伴奏` are recognized in version contexts; service `nolyric`/`uncollected` flags reject placeholder-only results.
- `https://music.163.com/api/song/lyric/v1` supplies `lrc` and `tlyric` together. This slice advertises LINE only; YRC and romanization remain out of scope.
- HTTP/API failures remain failures, not empty search results. Cancellation cancels the OkHttp call. No credentials, public proxy, access-control bypass or audio download.

## Pairing and selection

Only unique equal timestamps in original and translated LRC are paired. No positional zip, nearest timestamp, fuzzy text match or cross-source merge. Missing or ambiguous lines remain original-only. Translation attribution, contributor names and payload credits are retained with the document.

The global selector retains metadata/quality/source scoring and the existing duration policy. Reliable original writing can displace suspected romanization before bilingual/karaoke preference; genuine mixed-language singing has no script-alternation penalty. See `docs/MULTILINGUAL_MATCHING.md`. An enabled target-language preference can favor a supplied pair with at least one translated useful original row, within 0.03 metadata and 0.05 quality of the normal synchronized winner. There is no translation-coverage percentage threshold. This measures supplied-translation availability, not accuracy. Recording-version rejection still applies before preference. With Karaoke off, a suitable bilingual LINE candidate can beat WORD lyrics. With both Karaoke switches on, comparable usable WORD candidates take priority; supplied translations are preferred within that group, with bilingual LINE fallback when no suitable WORD candidate exists.

Some catalog records contain literal Unicode replacement characters (`U+FFFD`) in otherwise valid UTF-8 translation responses; the official app can show the same missing text. These characters are preserved, and their presence alone never discards a translation or forces machine fallback. Among eligible bilingual candidates with equal final, metadata, original-quality and source-confidence scores, the selector prefers a translation without replacement characters before using the stable source-ID tie-break. This only detects known missing characters, not translation accuracy or general readability. It cannot rescue a different recording or override stronger scores. Existing usable cache entries remain available; the tie-break applies to new retrievals.

## References and rights

Endpoint shapes were checked against public Lyricify Lyrics Helper (Apache-2.0) and ivLyrics NetEase plugin (MIT) implementations, then independently probed. No source from either project is incorporated. Existing AALyrics OkHttp, Gson and LRC facilities are reused.

- https://github.com/WXRIW/Lyricify-Lyrics-Helper
- https://github.com/1-Dot/ivLyrics-Netease-Cloud-Music

Software licenses do not license lyrics, translations, platform access or redistribution. See `docs/LYRICS_SOURCE_RIGHTS.md` for the evidence and unresolved authorization boundary.
