# AALyrics 1.2.0 stable release preparation

## Authorized scope

On 2026-11-07 the owner approved preparing personal stable release `v1.2.0` after the NetEase feature was merged in PR #8. Work starts from personal `main` at `3f36034ef88954f205b9cbe21fb0ffafe088ca78` and targets `WanHanxi/AALyrics:main` through `release/1.2.0`.

This slice prepares release documentation and validation, ending in a Draft PR. Merge and public tag publication require explicit owner approval. No runtime, workflow, signing, application identity, provider credentials, license, or reset behavior changes are planned.

## Plan and acceptance

1. Add the newest `1.2.0` Changelog entry with the opt-in NetEase source, paired Chinese translations, optional machine fallback, and material upgrade/availability limitations.
2. Align README and release instructions. Link the published stable version to GitHub Releases instead of describing an unpublished candidate as already available.
3. Document that personal 1.1.0 / 1.1.0-beta.1 settings survive the upgrade but their older cache entries need online retrieval again, even with NetEase off. Reuse the merged feature's existing setting/reset semantics.
4. Verify tag/changelog consistency, bundled documents, release scripts, architecture guardrails and the final branch scope. Reuse successful code validation for this documentation-only change and obtain CI on the preparation branch before opening the Draft PR.
5. Commit/push, open the personal Draft PR, then stop for review and explicit merge/release authorization. Recheck the Changelog release date before publication if delayed.

## Baseline evidence

- PR #8 merged into personal `main`; [Build run 42447207069](https://github.com/WanHanxi/AALyrics/actions/runs/42447207069) passed on that exact merge commit.
- The feature's local `./gradlew test :app:assembleDebug` validation passed with 115 suites / 906 executions, zero failures/errors/skips, in the workstation's verified 3 GiB / zero-swap scope. The owner subsequently reported successful real-device testing. This does not claim a new signed 1.1.0-to-1.2.0 device upgrade test.
- Published `v1.1.0` has APK and checksum assets. Its [Release run 42337576453](https://github.com/WanHanxi/AALyrics/actions/runs/42337576453) passed with workflow run number 2. Signing configuration and the pinned public certificate remain unchanged; the next Release workflow will revalidate the actual tagged artifact.
- Remote `main` matches the local baseline. No remote `v1.2.0` tag exists at preparation start.

## Preparation status

- Release notes and README/release instructions complete; the branch diff is limited to five documentation files.
- `:app:syncBundledDocumentsAssets` passed in a verified 3 GiB / zero-swap scope with the workstation Gradle flags; the scope exited. All six generated documents match their repository sources.
- Executing the existing Release workflow's version, Changelog-validation and note-extraction steps locally passed for `v1.2.0`: display title `AALyrics 1.2`, sample run 3 gives `versionCode` 1003, and the extracted notes match the newest Changelog section exactly. No signing or publishing step was executed.
- Architecture guardrails, all three release-script tests and whitespace checks passed. Existing full code validation is reused; no new local APK build, emulator or device test was performed for this documentation-only slice.
- Preparation-branch CI must pass before the Draft PR is opened; record its exact commit/run in the PR validation section.
- No release tag or public Release has been created.

## Publication handoff

After explicit owner authorization, merge the reviewed preparation PR, synchronize personal `main`, confirm the release date and create `v1.2.0` on the merged revision. Pushing the tag automatically publishes a signed APK and checksum when the Release workflow succeeds. Verify the final package/version/certificate checks, stable labeling, curated notes, both assets and update discovery afterward. Preserve the existing signing key and monotonically increasing workflow-derived `versionCode`.

Deferred: QQ integration, word-timed NetEase lyrics, manual source selection, automatic cache refresh, and platform/content licensing agreements. These are not part of release preparation.
