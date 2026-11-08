# NetEase paired original lyrics and Chinese translations

## Authorized scope

On 2026-11-07 the owner requested NetEase integration (QQ deferred), preferring a source's paired original and translated lyrics. Machine translation remains enabled as fallback by default, with a user switch to disable it. Personal fork only; finish with a Draft PR, without merge or release.

## Plan and acceptance

1. Add a bounded anonymous NetEase provider using the verified search and lyric endpoints, existing OkHttp/Gson/LRC utilities, shared metadata matching, cancellation and error isolation. No third-party lyrics SDK, proxy, account or audio access.
2. Model source-supplied translations together with original lyrics and attribution. Pair only unique equal timestamps from the same response; preserve missing lines, credits and source identity. Use synthetic test lyrics only.
3. Retain existing scoring and version gates. When translation is enabled, prefer a sufficiently complete same-source target-language pair only among comparably strong matches. Preserve normal scoring otherwise. Include this preference in lookup ownership and cache identity.
4. Publish supplied translations through the existing atomic Translation state without invoking ML Kit. Preserve whole-artifact provenance and optional whole-song machine fallback; never machine-fill gaps inside supplied translations.
5. Wire settings changes, Phone/Auto presentation, attribution, cache round trips and reset semantics. Reset restores machine fallback ON; downloaded models and lyric caches continue to survive reset, with explicit storage cleanup unchanged.
6. Update architecture, provider profile and legal/reference documentation. Verify licenses separately from platform/content authorization; no claim that anonymous access, noncommercial licensing or disclaimers grant content rights.
7. Run focused tests and full required build/checks sequentially within workstation memory limits; review branch-wide diff, push coherent checkpoints, then open a personal Draft PR.

## Validation/status

- Prior anonymous probe succeeded for five of six manually matched popular-song samples; this is not a coverage or quality benchmark.
- Implementation in progress. No build or on-device validation claimed yet.
- Deferred: QQ, YRC word timing, cross-source translation alignment, manual source picker, commercial/content licensing agreements.
