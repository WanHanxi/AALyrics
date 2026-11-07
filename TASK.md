# AALyrics 1.1.0 stable release

## Scope and authorization

- On 2026-11-06, the owner approved merging personal PR #5 and publishing stable **1.1.0**, removing the beta designation.
- Target only `WanHanxi/AALyrics`. Keep the original license/notices, personal application ID, signing key, provider configuration and reserved upstream contribution branch unchanged.
- PR #5 merged by squash as `f9d58722defb5e8fa870c6b491c68fcf30d23cae`. Its final head `70c0c781228cb0e9ea17143cb19b703c7345768f` passed hosted Build `41781430055`; merged-tree equality was checked before cleaning its completed branch.
- Release preparation starts on `release/1.1.0` from the merged personal `main`. Integrate the preparation through a personal PR under the owner's merge/release authorization, then publish the tag only after checks pass.

## Acceptance criteria

- Curate a newest `CHANGELOG.md` entry for `1.1.0`, dated 2026-11-06 in the owner's timezone, and align current README/release documentation with stable status. Preserve historical beta entries.
- Publish tag `v1.1.0` on personal `main`. The existing Release workflow supplies Android `versionName=1.1.0`, a monotonically increased `versionCode`, durable personal signing, APK verification and a normal GitHub Release.
- Preserve package `io.github.wanhanxi.aalyrics` and the pinned certificate so the personal beta can be updated in place. Keep Debug and upstream installs separate.
- Confirm successful Build and Release CI, the APK and checksum assets, exact artifact version/signature/bundled documents, normal release labeling and update-feed discoverability.
- Synchronize personal `main` and clean only this task's completed branches after confirmed merges.

## Progress and validation

- PR #5 is merged and personal `main` is synchronized. Its completed topic branch was removed after verifying the merged PR head and tree.
- Stable changelog, README and release documentation are prepared. App code and workflow behavior need no change: stable tag parsing and beta-to-stable precedence already have unit coverage.
- Static validation passed: architecture boundaries, whitespace checks, exact preservation of all historical Changelog entries, the stable heading/date and the five-file documentation-only scope. Publication remains gated on successful hosted Build and preparation-PR integration.
- After integration, publish `v1.1.0`, await Release CI, inspect the downloaded public assets and confirm release/update metadata. The public Release and workflow run retain the publication outcome; the final handoff records artifact verification.

No new persisted app state is introduced, so reset behavior is unchanged. Reuse the successful application checks; do not rebuild locally for documentation alone. Hosted Build and Release run the full unit suite and build their respective artifacts. No new emulator or on-device validation is claimed.
