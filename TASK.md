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
- Implementation complete. NetEase adapter, bilingual selection, cache persistence, supplied-artifact publication, fallback setting/reset, Phone/Auto projection, credits and model-independent Chinese selection are integrated.
- Final `./gradlew test :app:assembleDebug` passed sequentially with the workstation flags in a verified 3 GiB / zero-swap systemd scope. Result files contain 112 suites / 889 successful test executions, zero failures/errors. Architecture guardrails, `git diff --check`, and three release-script Python tests passed.
- Compiled production provider was exercised anonymously against Lemon and Yellow using a temporary Java harness, without exposing lyric text. Final response examples: Lemon source 536622304 has 42 original / 42 translated rows; Yellow sources 17177278 and 17177324 have 40 / 40. A real Chinese instrumental candidate discovered during this smoke check was excluded after adding explicit-version recognition and a regression test. These small samples do not establish general coverage or translation quality.
- APK-bundled license, notice, privacy, terms and third-party documents match the source files exactly. Original LICENSE and NOTICE are unchanged. No new third-party runtime dependency or raw real-song fixture is included.
- Interactive Compose previews cover supplied-source status, unavailable translation, fallback OFF and Chinese selection without a model; previews compiled successfully. No emulator, device install or visual rendering was performed. Phone/Auto on-device verification remains a testing handoff, not a claimed result.
- Existing lyric cache limits/retention are preserved; the version-2 schema and new provider/target identity cause previous selected-winner keys to miss. Reset restores fallback ON but retains lyric caches and downloaded models.
- Official NetEase terms and reference-project licenses were checked; platform/content permission has not been obtained. This is not cleared for licensed/commercial distribution merely by passing implementation tests.
- Branch-wide scope and personal-main baseline reviewed. Complete by opening the personal Draft PR; no merge/release/review request is authorized.
- Deferred: QQ, YRC word timing, cross-source translation alignment, manual source picker, commercial/content licensing agreements.
