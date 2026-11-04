# Personal fork attribution and feedback

## Scope and authorization

- The owner approved the attribution plan: identify WanHanxi as this fork's maintainer, retain Yuta Miura's original copyright, unify the fork's contact/feedback destinations, and label the existing donations as support for the original author.
- Branch: `fix/personal-fork-attribution`, based on personal `main` `ebf7af2` (published `v1.1.0-beta.1`). Target only `WanHanxi/AALyrics:main`.
- Complete implementation and validation, push coherent checkpoints, then open a Draft PR and stop. No merge, review request, tag or new release is authorized.

## Checkpoints

- [x] Separate original-project and fork-maintainer credits, preserve the original donation recipient, and align bundled policy/branding documentation and Compose Previews.
- [x] Enable and verify the personal repository's feedback channels; move app/documentation feedback routes and the LRCLIB User-Agent to this fork.
- [ ] Run focused routing/provider tests, architecture checks and a Debug build under the workstation resource budget; inspect the resulting APK and review the full diff.
- [ ] Push final validation evidence, confirm CI, open a Draft PR against the personal repository and stop.

## Decisions and acceptance criteria

- Keep `LICENSE`, `NOTICE`, original brand assets, Kotlin namespaces, application IDs and signing identity unchanged.
- Settings keeps the original-project copyright credit with a link to the original repository, and adds a distinct WanHanxi maintainer credit linking to this fork.
- The donation destination remains `https://buymeacoffee.com/whoxamxi`; every current user-facing support description must identify the original author as the recipient.
- This fork owns bug reports, questions, ideas, general discussion and private security reports. Check actual GitHub feature/category availability before declaring routes ready.
- GitHub inspection found Issues, Discussions and private vulnerability reporting disabled initially. Configure the required channels for this fork only; do not create sample issues, discussions or security reports.
- Preserve historical upstream provenance and the already published release notes. Describe this change under Unreleased.
- Reset contract re-evaluated: no persisted settings, caches or onboarding state change; existing reset behavior remains correct.
- No new UI-text tests are needed. Adapt the existing route expectations and provider request assertion; add dedicated footer Previews for typical, narrow and enlarged-font layouts.
- Run Gradle sequentially, with no emulator, one worker, no parallelism/daemon, 1 GiB heap, 512 MiB metaspace and in-process Kotlin. Verify a task-specific systemd scope has `MemoryMax=3G` and `MemorySwapMax=0`; do not change IDE or generated JVM settings.

## Validation and status

- Baseline: clean personal main, matching remote `ebf7af2863bb39050cbe3fa1c5a3cbfd47b92808`.
- In progress. Existing Release APK is from the published baseline; a new Debug APK will validate this unreleased change without changing the release channel.

- Attribution checkpoint: architecture checks and `git diff --check` pass; all Settings/runtime/Preview call sites carry the separate original-project callback. Dedicated footer Previews cover typical, narrow and enlarged-font layouts. Compilation is deferred to the final source checkpoint to avoid redundant local builds.

- Feedback checkpoint: enabled Issues, Discussions and private vulnerability reporting on `WanHanxi/AALyrics`. Read-back confirms all enabled, with `q-a`, `ideas` and `general` category slugs. No sample reports or discussions were posted. App routes, SUPPORT/SECURITY documentation, Settings specification and LRCLIB request identity now target this fork.
