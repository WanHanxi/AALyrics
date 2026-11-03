# Personal fork workflow

`WanHanxi/AALyrics` is developed independently as a personal fork. Its stable integration branch is `main`; personal features do not need to be accepted by the original repository before they can be used here.

Unless the owner explicitly requests upstream work, repository changes, PRs, reviews and merges target this personal fork only. An unspecified request to merge does not authorize changes to upstream or to an upstream-facing contribution branch.

## Repositories and branches

| Name | Role |
| --- | --- |
| `origin` | `git@github.com:WanHanxi/AALyrics.git`; default push destination |
| `upstream` | `https://github.com/whoxamxl/AALyrics.git`; fetch original updates |
| `main` / `origin/main` | Local / remote personal integration branch |
| `upstream/main` | Remote-tracking reference to the original main; no extra mirror branch is needed |
| `feature/*`, `fix/*`, other approved prefixes | Personal work based on personal `main` |
| `chore/sync-upstream` | Integrate an upstream update into the personal fork |
| `feature/lyrics-sync-offset` | Existing contribution branch for [upstream PR #91](https://github.com/whoxamxl/AALyrics/pull/91) |

Personal `main` was initialized by a user-authorized fast-forward from `e85c21c` to the completed Sync revision `3d13584`. This preserved its 15 commits and did not change the head of upstream PR #91. That PR's development and validation record remains available in `3d13584:TASK.md`.

## Local setup

In a fresh clone of the personal repository, add the upstream remote if it is absent:

```sh
git remote add upstream https://github.com/whoxamxl/AALyrics.git
git fetch --no-tags upstream
git config --local remote.pushDefault origin
git config --local push.default simple
git config --local branch.main.pushRemote origin
git branch --set-upstream-to=origin/main main
```

These settings belong to this checkout; a new clone needs them again. They choose the Git push destination, not the GitHub PR destination. Select `WanHanxi/AALyrics` explicitly when opening an internal PR.

Workstation-only instructions stay in the ignored root `AGENTS.override.md`, which explicitly loads the tracked `AGENTS.md` before adding local limits. The override and `.local-notes/` are excluded through `.git/info/exclude`, not shared project ignore rules. They are local files and are not copied by cloning the repository.

## Personal development

Start with the personal integration branch:

```sh
git fetch origin
git switch main
git merge --ff-only origin/main
git switch -c feature/my-feature
```

Implement and validate coherent checkpoints on the topic branch, keep `TASK.md` current for substantial work, and commit/push checkpoints to `origin`. When the work and final validation are complete, open a Draft PR with:

- base repository: `WanHanxi/AALyrics`;
- base branch: `main`;
- head: the personal topic branch.

Stop after opening the Draft PR. Merge only after explicit owner authorization; ordinary feature PRs prefer squash merge. Updating personal `main` does not change upstream PR #91, which tracks its separate head branch.

Use an actual GitHub PR for integration. If the connector cannot create or merge a PR, leave the completed work on its topic branch and report the permission blocker. Authorization to merge does not imply authorization to bypass the PR with a direct push to `main`; that alternative requires an explicit request.

After a confirmed PR merge, fetch `origin`, fast-forward local `main`, and delete the task's completed remote and local topic branches plus temporary integration branches. For a squash merge, confirm the PR is merged and that its merged head contains all work on the branch before deleting it; `git branch --merged` alone is insufficient. Keep branches with unmerged changes, unrelated active work, and reserved upstream contributions. If PR integration is blocked, retain the pending source branch and clean only temporary branches whose work is already safely recorded.

The existing Build workflow listens to pushes to `main`, pull requests, and manual dispatch. Ordinary feature-branch pushes alone do not start Build. GitHub Actions must be enabled in the fork for its workflows to run.

## Periodic upstream updates

When an upstream synchronization is requested, first update personal `main` from `origin/main` as above, then:

```sh
git fetch --no-tags upstream
git switch -c chore/sync-upstream main
git merge --no-ff upstream/main
```

Resolve actual conflicts, preserve personal behavior, and run checks appropriate to the imported changes. Push the synchronization branch and open a Draft PR against personal `main`. After authorization, integrate it with merge history preserved rather than squashing away the upstream ancestry; this gives later synchronizations the correct common base. Use a new descriptive synchronization branch name if an earlier one is still active.

Do not reset personal `main` to `upstream/main` or rebase published personal history merely to match upstream. Fetching upstream does not itself change the working tree or integrate code.

## Upstream contributions and the existing PR

Do not append personal features or fork-specific policy changes to `feature/lyrics-sync-offset`; every pushed commit there changes upstream PR #91. Keep it available for explicitly requested review follow-up. If a review fix is also needed in the personal version, bring that focused fix into a separate personal topic branch.

A newly authorized upstream contribution should start from `upstream/main`, not personal `main`, and include only the intended changes. Consult the original repository's contribution policy and select `whoxamxl/AALyrics` explicitly for that PR. No upstream submission or merge is implied by personal development.

## Personal application distribution

The personal release channel targets `WanHanxi/AALyrics`. In-app update discovery, release download pages and the Source code row use this repository. Kotlin namespaces and the original license/notices remain unchanged.

- Release application ID: `io.github.wanhanxi.aalyrics`; launcher/Android Auto label: `AALyrics Fork`.
- Debug application ID: `io.github.wanhanxi.aalyrics.debug`; label: `AALyrics Fork Debug`.
- Personal releases use their own durable signing key, independent of the original author and Android Debug keys.
- First personal target: `v1.1.0-beta.1`. Version tags remain independent of upstream's release schedule.

The new package can coexist with upstream and earlier Debug installs. It is a fresh installation: app settings/cache and Android permissions are separate, with no automatic migration. Later personal releases must retain this package identity and signing key and increase `versionCode`.

Existing upstream community/support links remain attributed to upstream; this change does not create a personal Issues/Discussions service. Personal Issues and Discussions are currently disabled. Do not report personal-only defects upstream as if they were original-app bugs.

Release signing setup, missing provider credentials, verification and the no-tag-before-approval procedure are documented in `docs/RELEASES.md`. Repository setup and a successful local build alone do not establish a working hosted release channel.
