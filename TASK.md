# NetEase paired original lyrics and Chinese translations

## Authorized scope

On 2026-11-07 the owner requested NetEase integration (QQ deferred), preferring a source's paired original and translated lyrics. Machine translation remains enabled as fallback by default, with a user switch to disable it. Personal fork only; finish with a Draft PR, without merge or release.

## Authorized license-page cleanup

The owner approved removing the NetEase implementation-reference section from `THIRD_PARTY_LICENSES.md`, which is also shown on the app's License page. Lyricify and ivLyrics are research references, not incorporated runtime dependencies. Their references and the platform/content rights discussion remain in `docs/LYRICS_SOURCE_RIGHTS.md` and `docs/providers/NETEASE.md`. Any further consolidation of rights wording requires separate approval; no Terms, source notice, runtime behavior or reset changes are included here.

Validation: `:app:syncBundledDocumentsAssets` passed in a verified 3 GiB / zero-swap scope with the workstation flags; the scope exited. All six generated bundled documents match their repository sources, the removed reference section is absent from the License-page asset, and both repository reference documents retain the research attribution. Whitespace checks passed. The prior successful code build and 906-test validation are reused for this documentation-only change; no APK rebuild or device verification was needed or performed.

## Reported translation corruption follow-up

The owner reported replacement characters in AALyrics while the official NetEase app shows clean text, and requested checking the official result before assuming another song version is interchangeable. The supplied share link resolves to song 3392743810 (translation version 7, no replacement characters); a duplicate metadata match, 3392881804 (translation version 5), returns eight literal U+FFFD characters in valid UTF-8 JSON. Current and legacy lyric endpoints and version parameters 0/-1 reproduce the latter payload. Replaying these two responses through the baseline compiled production provider/selector reproduces selection of 3392881804. The owner subsequently found the same defect in the other official-app record, and clarified that this largely readable translation should remain usable.

Plan: retain supplied text, including source replacement characters. Only break otherwise equal bilingual selection scores in favor of an eligible translation without U+FFFD; preserve recording/metadata gates, original quality, source confidence, disabled/other-target behavior and the existing machine-fallback policy. Keep all existing usable caches, including imperfect translations; no schema/policy bump or automatic refresh. Use synthetic regressions for tied alternatives, stronger matches, wrong recording rejection, valid Unicode and persisted source defects. Do not hardcode song IDs, strip or guess missing text, or mix translations between records. Continue on personal Draft PR #8 without merge/release/review request.

Status: implementation and final validation complete. `./gradlew test :app:assembleDebug` passed in a verified 3 GiB / zero-swap scope using the workstation flags (115 suites / 906 executions, zero failures/errors/skips). Architecture checks, all three release-script tests and whitespace checks passed. Replaying the recorded responses through the compiled production provider/selector now selects 3392743810 at the same 0.984 score while preserving all eight source replacement characters in 3392881804's supplied translation. Synthetic tests cover equal-score ordering, unchanged disabled/other-target selection, retention with no better match, stronger metadata and wrong-version rejection, Unicode and existing cache compatibility. No persisted state or reset behavior changed; all usable lyric caches still survive reset and upgrades. No emulator/device check was run; this is an offline response replay, and previously cached lyrics are not automatically refreshed.

## Authorized opt-in follow-up

The owner approved implementing a default-OFF NetEase switch with the neutral third-party notice and Cancel / Enable actions. Existing LRCLIB, Musixmatch and SyncLRC behavior is explicitly out of scope. Continue on Draft PR #8; no merge, release or review request.

1. Persist the NetEase choice and notice acknowledgement atomically. A new installation or missing historical choice defaults OFF; ordinary upgrades preserve both values. Explicit Reset AALyrics restores OFF and clears acknowledgement, while retaining the existing cache/model retention policy.
2. Gate NetEase before provider requests and include the effective provider set in lookup ownership and cache keys. Disabling cancels current work, removes retained NetEase results and refreshes eligible active playback with the existing sources; no background lookup starts without demand.
3. Add the Phone Settings switch and first-enable confirmation, with preview coverage and updated reset/privacy/provider documentation. Keep machine fallback ON by default and user-controllable.
4. Validate persistence, enabling/disabling, cancellation and cache isolation, then run required build/checks sequentially within the workstation budget. Commit/push coherent checkpoints and update the existing Draft PR around the final behavior.

Status: opt-in implementation and final validation complete. The focused `:core:lyrics:test :app:testDebugUnitTest` checkpoint passed, followed by full `./gradlew test :app:assembleDebug` in a verified 3 GiB / zero-swap scope (114 suites / 899 executions, zero failures/errors/skips). Architecture guardrails, whitespace checks and all three release-script tests passed. All six APK-bundled documents match repository sources; LICENSE and NOTICE remain unchanged.

Coverage includes default OFF, confirmation-gated enablement, retained ON/OFF upgrade choices, reset, source eligibility, cancellation/late results, separate disk-cache identities across restarts and the Chinese model-picker gate. Settings UI and reset/privacy/provider/cache documentation are aligned. Interactive Settings and notice/reset previews compile, including narrow/landscape/enlarged-text variants; no emulator, visual rendering or on-device test is claimed. Both task-owned build scopes exited; no emulator was started. Continue to use personal Draft PR #8 as the completion point, without review request, merge or release.

## Plan and acceptance

1. Add a bounded anonymous NetEase provider using the verified search and lyric endpoints, existing OkHttp/Gson/LRC utilities, shared metadata matching, cancellation and error isolation. No third-party lyrics SDK, proxy, account or audio access.
2. Model source-supplied translations together with original lyrics and attribution. Pair only unique equal timestamps from the same response; preserve missing lines, credits and source identity. Use synthetic test lyrics only.
3. Retain existing scoring and version gates. When translation is enabled, prefer a sufficiently complete same-source target-language pair only among comparably strong matches. Preserve normal scoring otherwise. Include this preference in lookup ownership and cache identity.
4. Publish supplied translations through the existing atomic Translation state without invoking ML Kit. Preserve whole-artifact provenance and optional whole-song machine fallback; never machine-fill gaps inside supplied translations.
5. Wire settings changes, Phone/Auto presentation, attribution, cache round trips and reset semantics. Reset restores machine fallback ON; downloaded models and lyric caches continue to survive reset, with explicit storage cleanup unchanged.
6. Update architecture, provider profile and legal/reference documentation. Verify licenses separately from platform/content authorization; no claim that anonymous access, noncommercial licensing or disclaimers grant content rights.
7. Run focused tests and full required build/checks sequentially within workstation memory limits; review branch-wide diff, push coherent checkpoints, then open a personal Draft PR.

## Initial integration validation (before the opt-in follow-up)

- Prior anonymous probe succeeded for five of six manually matched popular-song samples; this is not a coverage or quality benchmark.
- Implementation complete. NetEase adapter, bilingual selection, cache persistence, supplied-artifact publication, fallback setting/reset, Phone/Auto projection, credits and model-independent Chinese selection are integrated.
- Final `./gradlew test :app:assembleDebug` passed sequentially with the workstation flags in a verified 3 GiB / zero-swap systemd scope. Result files contain 112 suites / 889 successful test executions, zero failures/errors. Architecture guardrails, `git diff --check`, and three release-script Python tests passed.
- Compiled production provider was exercised anonymously against Lemon and Yellow using a temporary Java harness, without exposing lyric text. Final response examples: Lemon source 536622304 has 42 original / 42 translated rows; Yellow sources 17177278 and 17177324 have 40 / 40. A real Chinese instrumental candidate discovered during this smoke check was excluded after adding explicit-version recognition and a regression test. These small samples do not establish general coverage or translation quality.
- APK-bundled license, notice, privacy, terms and third-party documents match the source files exactly. Original LICENSE and NOTICE are unchanged. No new third-party runtime dependency or raw real-song fixture is included.
- Interactive Compose previews cover supplied-source status, unavailable translation, fallback OFF and Chinese selection without a model; previews compiled successfully. No emulator, device install or visual rendering was performed. Phone/Auto on-device verification remains a testing handoff, not a claimed result.
- Existing lyric cache limits/retention are preserved; the version-2 schema and new provider/target identity cause previous selected-winner keys to miss. Reset restores fallback ON but retains lyric caches and downloaded models.
- Official NetEase terms and reference-project licenses were checked; platform/content permission has not been obtained. This is not cleared for licensed/commercial distribution merely by passing implementation tests.
- Branch-wide scope and personal-main baseline reviewed. The initial integration ended in personal Draft PR #8; the authorized opt-in follow-up above updates that same PR. No merge/release/review request is authorized.
- Deferred: QQ, YRC word timing, cross-source translation alignment, manual source picker, commercial/content licensing agreements.
