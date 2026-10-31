# Personal fork development workflow

## Scope

- Establish `WanHanxi/AALyrics:main` as the personal integration baseline, with `whoxamxl/AALyrics` available as the `upstream` fetch source.
- Perform the user-authorized one-time fast-forward of personal `main` from `e85c21c` to the completed Sync revision `3d13584`; preserve commit history.
- Keep `feature/lyrics-sync-offset` at `3d13584` and upstream Draft PR #91 unchanged.
- Record durable fork-specific development and PR routing rules on `chore/personal-fork-workflow`, based on the new personal `main`.
- Preserve application behavior, provider configuration, signing, update endpoints, user-owned Gradle/SDK files, and local verification limits.

## Checkpoints

- [x] Verify local/remote baseline and upstream PR head; retain a local checksum of the user-owned Gradle file.
- [x] Add/fetch `upstream`; set default pushes to `origin` with simple branch matching and explicit `main` push routing.
- [x] Fast-forward and push personal `main`; create the independent workflow documentation branch.
- [x] Document personal feature branches, own-repository PRs, periodic upstream synchronization, and isolated upstream contributions.
- [x] Verify configuration, branch isolation, documentation, and architecture.
- [x] Prepare the completed documentation checkpoint for a Draft PR against `WanHanxi/AALyrics:main`; stop once published.

## Acceptance

- Local `main` tracks `origin/main`, and both include the completed Sync implementation without waiting for upstream acceptance.
- `origin` remains the personal fork; `upstream/main` remains a separate remote-tracking reference. Default pushes and PR targets resolve to the personal fork.
- Personal features start from personal `main`. New work does not enter upstream PR #91 through its existing head branch.
- Periodic upstream synchronization uses a dedicated topic branch and preserves upstream merge ancestry. New upstream contributions require explicit authorization and start from `upstream/main`.
- The workflow documentation branch contains only instructions/documentation. No new application state or reset semantics are introduced.

## Validation

- Before setup, local/remote personal `main` and upstream `main` were `e85c21c`; both Sync branch references and upstream PR #91 were `3d13584`.
- Personal `main` was advanced with `git merge --ff-only` and pushed without force. No new commit was authored directly on `main`.
- Reuse the successful Sync build/test evidence recorded in `3d13584:TASK.md`; configuration and documentation changes do not require another Gradle or emulator run.
- Final checks passed: architecture guardrails, `git diff --check`, default push/tracking configuration, expected local/remote branch heads, documentation-only scope, ignored local guidance, and the unchanged user-owned Gradle file checksum.
- The remote personal `main` was confirmed at `3d13584`. Upstream PR #91 remains open and Draft with the same `3d13584` head and `e85c21c` base.
- The fork's existing Build and Release workflow definitions report `active`. Its Actions API had no run records during setup; no successful CI run is claimed for the main bootstrap. The documentation PR follows the existing `pull_request` trigger.

## Delivery and deferred work

- The one-time personal-main bootstrap is explicitly authorized. Subsequent workflow-documentation integration follows the ordinary Draft PR and merge-approval process in the personal repository.
- Workflow documentation is committed and pushed on `chore/personal-fork-workflow`. Internal PR publication is pending: the GitHub connector returned HTTP 403 (`Resource not accessible by integration`), and the browser access host was unavailable. No internal PR was created. Use [the personal-repository compare page](https://github.com/WanHanxi/AALyrics/compare/main...chore/personal-fork-workflow?expand=1) to create it as Draft when access is available.
- Application update source, release assets/versioning, installation identity/signing, and help links remain separate follow-up decisions. Adding Git remotes does not change the current upstream-facing application URLs.
- Do not change, close, mark ready, or merge upstream PR #91. Do not begin an unrelated personal feature as part of this setup.
