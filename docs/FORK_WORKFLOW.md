# Personal fork workflow

`WanHanxi/AALyrics` is developed independently as a personal fork. Its stable integration branch is `main`; personal features do not need to be accepted by the original repository before they can be used here.

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

## Separate application distribution decisions

Git remote setup does not change the APK's update destination or installation identity. The update client currently queries `whoxamxl/AALyrics` releases; source/release/help links and application IDs also retain the original project's values.

Before establishing a personal release channel, separately decide the update source or whether to disable in-app update discovery, personal release/versioning policy, and signing identity. A distinct application ID is needed if the personal release should coexist with the original release. These are future product/build changes, not part of this workflow setup; see `docs/RELEASES.md` and `docs/UPDATE_UX.md`.
