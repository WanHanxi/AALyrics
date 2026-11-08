# NetEase provider profile

## Scope

Anonymous, bounded retrieval of line-timed original lyrics and existing Chinese translations. No official third-party API agreement or content license has been established. This is an independent Kotlin adapter, not a dependency on a Node/.NET lyrics library.

## Transport and discovery

- Search `https://music.163.com/api/cloudsearch/pc` with title and artist, at most 10 results. A title-only fallback is allowed only when the first query produces no plausible candidates.
- Shared `MetadataMatchScore` validates title, artist, album, duration and recording version before fetching at most three candidate lyric responses.
- `https://music.163.com/api/song/lyric/v1` supplies `lrc` and `tlyric` together. This slice advertises LINE only; YRC and romanization remain out of scope.
- HTTP/API failures remain failures, not empty search results. Cancellation cancels the OkHttp call. No credentials, public proxy, access-control bypass or audio download.

## Pairing and selection

Only unique equal timestamps in original and translated LRC are paired. No positional zip, nearest timestamp, fuzzy text match or cross-source merge. Missing or ambiguous lines remain original-only. Translation attribution, contributor names and payload credits are retained with the document.

The global selector retains metadata/quality/source scoring. An enabled target-language preference can favor a supplied pair with at least 80% useful-line coverage, within 0.03 metadata and 0.05 quality of the normal synchronized winner. This measures structural completeness, not translation quality. Recording-version rejection still applies before preference. Same-source translations can display even below the ranking threshold if that original document wins normally.

## References and rights

Endpoint shapes were checked against public Lyricify Lyrics Helper (Apache-2.0) and ivLyrics NetEase plugin (MIT) implementations, then independently probed. No source from either project is incorporated. Existing AALyrics OkHttp, Gson and LRC facilities are reused.

- https://github.com/WXRIW/Lyricify-Lyrics-Helper
- https://github.com/1-Dot/ivLyrics-Netease-Cloud-Music

Software licenses do not license lyrics, translations, platform access or redistribution. See `docs/LYRICS_SOURCE_RIGHTS.md` for the evidence and unresolved authorization boundary.
