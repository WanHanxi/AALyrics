# NetEase lyrics: implementation references and rights

Checked 2026-11-07. This records evidence and unresolved permission; it is not a legal opinion or a content license. Deployment jurisdiction and distribution model matter.

## What AALyrics uses

`:provider:netease` is an independently written Kotlin adapter for two anonymous web endpoints. It reuses AALyrics' existing OkHttp, Gson, coroutines, shared metadata matching and LRC parsing. It does not embed a Node service, public API mirror, Lyricify binary, or ivLyrics source. There is no new third-party runtime library and no change to AALyrics' PolyForm Noncommercial 1.0.0 license.

The prior Python probes consulted public endpoint/request shapes in:

- [Lyricify Lyrics Helper](https://github.com/WXRIW/Lyricify-Lyrics-Helper), [Apache-2.0 LICENSE](https://github.com/WXRIW/Lyricify-Lyrics-Helper/blob/master/LICENSE).
- [ivLyrics NetEase plugin](https://github.com/1-Dot/ivLyrics-Netease-Cloud-Music), [MIT LICENSE](https://github.com/1-Dot/ivLyrics-Netease-Cloud-Music/blob/main/LICENSE).

These are references, not incorporated dependencies. If their implementation code is copied or adapted later, review and preserve the applicable license, attribution and notice obligations; Apache-2.0 additionally requires modified-file notices where applicable. AALyrics' own noncommercial license does not expand third-party rights.

## What similar projects publicly show

| Project | Publicly documented behavior | Software license evidence |
| --- | --- | --- |
| Lyricify | Its own lyrics helper exposes NetEase and other sources. The application README identifies the helper as an open-source component. | Helper: Apache-2.0. Do not infer that the entire application has the helper's license. |
| ivLyrics NetEase plugin | Matches Spotify tracks, fetches `lrc` plus `tlyric`/`ytlrc`, and can skip AI translation. Requires a user-supplied CORS proxy; no public mirror by default. | Plugin: MIT. This is an independent example, not a verified consumer of Lyricify's library. |
| 163MusicLyrics | Fetches NetEase/QQ originals and translations, offers composition, saving and local caching. | Apache-2.0. Its README establishes features and code license, not a platform/content agreement for AALyrics. |
| Binaryify/NeteaseCloudMusicApi | Original GitHub repository is archived and states it stopped maintenance to protect copyright. | Current default branch retains only the shutdown README; AALyrics does not use it. Historical/fork licenses must be checked at the exact revision before reuse. |

References: [Lyricify application](https://github.com/WXRIW/Lyricify-App#lyricify-开源), [ivLyrics behavior](https://github.com/1-Dot/ivLyrics-Netease-Cloud-Music/blob/main/README.zh-CN.md), [163MusicLyrics](https://github.com/jitwxs/163MusicLyrics), [archived API repository](https://github.com/Binaryify/NeteaseCloudMusicApi).

The reviewed public pages do not establish a transferable license for NetEase lyrics or translations. This does not prove those maintainers lack private agreements. A disclaimer, open-source license, self-hosted proxy, opt-in setting, client-side request or noncommercial use is not by itself content authorization.

## Platform and content rights are separate

1. **Platform terms/access.** [NetEase's official terms entry](https://st.music.163.com/official-terms/service) points to [the current Chinese terms](https://y.music.163.com/g/yida/36a81250504747a19283b29e4e9ff38c). The linked text was retrieved directly from the official host on the review date. Section 10 restricts unauthorized copying/reproduction and related uses for both commercial and noncommercial purposes, with third-party content rights separately recognized. Section 11's user-content license to NetEase does not automatically authorize an unrelated third-party client. Successful anonymous HTTP requests are not a third-party API agreement or service-level guarantee.
2. **Original lyrics and translations.** The [PRC Copyright Law (2020 revision), published by the Supreme People's Court IP Court](https://ipc.court.gov.cn/zh-cn/news/view-6244.html), protects relevant reproduction and information-network rights (Article 10) and recognizes rights in translations without extinguishing original-work rights (Article 13). Personal study/research exceptions in Article 24 are conditional and cannot automatically be extended to distributing a general-purpose app or operating a public lyrics service. Other jurisdictions require their own analysis.
3. **Code license.** Apache-2.0 and MIT authorize the covered software under their terms. They do not license the provider's data, lyrics or translations. PolyForm Noncommercial also covers software, not third-party song content.

No platform or rightsholder permission has been obtained by this implementation. Before broader distribution/commercial use, obtain permission covering the intended source access, original/translated lyric display, attribution, territories and caching, or obtain qualified legal assessment of the specific use. Do not market the integration as officially licensed or endorsed.

## Implemented handling and its limits

NetEase is off by default. First enablement shows a factual third-party notice and Cancel / Enable actions; ordinary upgrades preserve the saved choice and acknowledgement. Reset restores OFF and clears acknowledgement. Disabled NetEase is excluded from requests, selection and cached-result reuse. This is an explicit user choice, not a transfer of responsibility or a grant of content rights. Existing providers are unchanged in this slice.

- Queries are bounded and cancellable, use no account cookies, and fetch no audio.
- Originals, supplied translations, source IDs and available credits remain paired. No blanket claim of human authorship is made.
- Existing app-private bounded cache stores the paired content; no lyrics database, proxy, bulk export or raw commercial-song fixtures are shipped in Git.
- Machine translation remains an optional whole-document fallback and never fills gaps within a supplied translation.
- Privacy and Terms describe provider contact, supplied translation storage and the distinction between software and content rights.

These choices document behavior and reduce unnecessary collection/mixing. They do not cure missing authorization, make copying automatically lawful, or guarantee continued endpoint access.
