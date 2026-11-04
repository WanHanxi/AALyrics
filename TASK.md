# Personal fork attribution and feedback

## Scope and authorization

- The owner approved the attribution plan: identify WanHanxi as this fork's maintainer, retain Yuta Miura's original copyright, unify the fork's contact/feedback destinations, and label the existing donations as support for the original author.
- Follow-up: the owner supplied `https://buymeacoffee.com/wancy` for a separate personal donation entry. Add it alongside the original-author entry in the app, README and GitHub funding configuration.
- Branch: `fix/personal-fork-attribution`, based on personal `main` `ebf7af2` (published `v1.1.0-beta.1`). Target only `WanHanxi/AALyrics:main`.
- Existing personal [Draft PR #5](https://github.com/WanHanxi/AALyrics/pull/5) is open. The owner now requests one Support entry in the Settings list, opening two stacked recipient cards. Update this PR after implementation and validation, then stop; no merge, review request, tag or new release is authorized.

## Checkpoints

- [x] Separate original-project and fork-maintainer credits, preserve the original donation recipient, and align bundled policy/branding documentation and Compose Previews.
- [x] Enable and verify the personal repository's feedback channels; move app/documentation feedback routes and the LRCLIB User-Agent to this fork.
- [x] Run focused routing/provider tests, architecture checks and a Debug build under the workstation resource budget; inspect the resulting APK and review the full diff.
- [x] Add the owner-supplied personal donation entry with matching policy/docs, Preview coverage and navigation validation.
- [x] Complete final local verification and branch review after the donation follow-up.

Delivery gate: validate and push this follow-up, require successful hosted CI, update the existing Draft PR description to the final layout, then stop.

## Decisions and acceptance criteria

- Keep `LICENSE`, `NOTICE`, original brand assets, Kotlin namespaces, application IDs and signing identity unchanged.
- Settings keeps the original-project copyright credit with a link to the original repository, and adds a distinct WanHanxi maintainer credit linking to this fork.
- Donation destinations are separate: `https://buymeacoffee.com/wancy` for WanHanxi and `https://buymeacoffee.com/whoxamxi` for the original author. Identify the selected recipient before any external handoff; preserve the original author's destination.
- This fork owns bug reports, questions, ideas, general discussion and private security reports. Check actual GitHub feature/category availability before declaring routes ready.
- GitHub inspection found Issues, Discussions and private vulnerability reporting disabled initially. Configure the required channels for this fork only; do not create sample issues, discussions or security reports.
- Preserve historical upstream provenance and the already published release notes. Describe this change under Unreleased.
- Reset contract re-evaluated: no durable settings, caches or onboarding state change. The new support destination is transient Settings navigation and returns to MAIN through the existing Back/root-reset paths.
- No new UI-text tests are needed. Adapt the existing route expectations and provider request assertion; add dedicated footer Previews for typical, narrow and enlarged-font layouts.
- Run Gradle sequentially, with no emulator, one worker, no parallelism/daemon, 1 GiB heap, 512 MiB metaspace and in-process Kotlin. Verify a task-specific systemd scope has `MemoryMax=3G` and `MemorySwapMax=0`; do not change IDE or generated JVM settings.

## Validation and status

- Baseline: clean personal main, matching remote `ebf7af2863bb39050cbe3fa1c5a3cbfd47b92808`.
- The attribution and donation implementation is complete in Draft PR #5. The combined-support-layout follow-up is now in progress. The published Release and version channel are unchanged.

- Attribution checkpoint: architecture checks and `git diff --check` pass; all Settings/runtime/Preview call sites carry the separate original-project callback. Dedicated footer Previews cover typical, narrow and enlarged-font layouts. Compilation is deferred to the final source checkpoint to avoid redundant local builds.

- Feedback checkpoint: enabled Issues, Discussions and private vulnerability reporting on `WanHanxi/AALyrics`. Read-back confirms all enabled, with `q-a`, `ideas` and `general` category slugs. No sample reports or discussions were posted. App routes, SUPPORT/SECURITY documentation, Settings specification and LRCLIB request identity now target this fork.

- Source checkpoint `36b085b`: focused `HelpFeedbackRoutingTest` (2 tests), `LrcLibProviderTest` (21 tests) and `:app:assembleDebug` passed in one sequential Gradle invocation (54 seconds). Zero failures, errors or skipped tests.
- The active systemd scope verified `MemoryMax=3221225472` and `MemorySwapMax=0`; peak memory was 2,499,579,904 bytes (2.33 GiB), with zero OOM/limit events. No emulator was started. The scope ended after verification; no task-owned build process is kept running.
- Initial attribution APK (superseded by the donation follow-up): `app/build/outputs/apk/debug/app-debug.apk`, 92,158,211 bytes, SHA256 `83484fe19d2e96966ed5e1cf86add1858d1e206db8965c6f987dd5fd5803d4e3`. Package `io.github.wanhanxi.aalyrics.debug`, label `AALyrics Debug`, version `1.1.0-beta.1-dev+36b085b`, code 1.
- Inspected APK resources and DEX: both attribution strings and the explicit original-author donation copy are present; all five feedback links and the LRCLIB User-Agent point to the personal repository. The separate original-project link and original Buy Me a Coffee recipient remain present.
- All six APK documents exactly match the repository sources. `LICENSE`, `NOTICE`, third-party licenses, brand assets, application IDs and namespaces have no branch changes. Generated Gradle JVM settings retain SHA256 `6c13aa5ba36690ccfbbb25a8807d01d98c15833e446d7ae82ddc726472cb8ef7`.
- GitHub read-back verified all three feedback features enabled. Public Q&A/Ideas/General category pages return HTTP 200; issue creation and private-report entry points return the expected login redirects. No reports were submitted.
- Architecture checks and branch-wide `git diff --check` pass. Source/Preview callback wiring and current documentation were reviewed; historical upstream references remain intact.
- UI validation is compilation/resource inspection plus Compose Preview coverage. No emulator or on-device visual test was run; typical/narrow/enlarged-font footer Previews and the existing Support screen Previews are available for visual review.
- Initial hosted Build [41778846680](https://github.com/WanHanxi/AALyrics/actions/runs/41778846680) passed on `027ec69`, including full unit tests, Debug build and artifact uploads.
- The owner-supplied personal support URL returned HTTP 200 with page title `WHX`. No payment was initiated. Final app/documentation changes retain two distinct recipients.

## Final donation follow-up validation

- Source checkpoint `d393218`: `SettingsNavigationTest` (3 tests) and `:app:assembleDebug` passed in 48 seconds. Existing navigation coverage includes the new support child and returns it to Settings home; no new persistence/reset behavior was introduced.
- The verified 3 GiB / zero-swap scope peaked at 2,508,328,960 bytes (2.34 GiB), with zero OOM/limit events. The scope ended after verification. No emulator was started and generated JVM settings remain unchanged.
- Current Debug APK: `app/build/outputs/apk/debug/app-debug.apk`, 92,160,303 bytes; SHA256 `35671be711e27f64a555d73a8524e5de9225e480934bc05489143d130813dad5`. Package `io.github.wanhanxi.aalyrics.debug`, version `1.1.0-beta.1-dev+d393218`, label `AALyrics Debug`.
- APK inspection confirms both Buy Me a Coffee URLs, both recipient-specific labels/accessibility descriptions, separate maintainer/original credits, and the personal feedback routes. All six bundled documents exactly match current source. Funding YAML parses with `buy_me_a_coffee: wancy` and the original-author URL as a separate custom link.
- Both support pages reuse the same native presentation while supplying distinct copy and callbacks. Added personal-support Previews cover typical, narrow and enlarged-font layouts; original-author Previews remain available. Source review traced each callback to its corresponding URL. Visual/on-device verification remains an optional handoff, with no emulator run claimed.
- Final branch-wide scope review, architecture checks and diff checks pass. The prior 23 routing/provider tests remain applicable to unchanged code; final hosted CI runs the complete suite and Debug build. No remaining implementation work is deferred.

## Combined support screen follow-up

- [x] Replace the two Settings rows/subscreens with one `Support AALyrics` entry and one screen containing two stacked cards: WanHanxi first, original author second.
- [x] Preserve the two URLs and independent callbacks, shorten repeated card copy, and keep a shared voluntary/external-payment note.
- [ ] Update combined-screen Previews and current documentation; validate navigation, build/APK content, architecture and the follow-up diff under the same workstation limits.
- [ ] Push the completed follow-up and update Draft PR #5 after CI; keep it Draft and stop before merge.

Reset contract: only Settings-local navigation changes. The single support child returns to MAIN with existing Back/root-reset handling; no durable preferences, caches or onboarding state are added.
