# Public device-test documentation cleanup

## Scope

Remove the owner's device model, ROM, and OS details from current documentation,
personal PR descriptions, the v1.2.1 Release notes, and its APK's bundled Changelog.
Use generic wording while retaining the actual tested scenarios and limitations.
Add a repository instruction to keep future public test reports equally generic.

Work starts from personal `main` at `c552b06e77608795326d0362a65de567dde35d81`
on `docs/remove-device-details` and targets `WanHanxi/AALyrics:main`.
The owner chose to preserve existing Git history and tags. Historical commits,
diffs, source archives, and already-downloaded copies are outside this cleanup.
No upstream work, runtime changes, version bump, or signing-identity change.

## Checkpoints

1. Sanitize current docs and PR descriptions; record the public-reporting rule.
2. Rebuild v1.2.1 with the updated bundled Changelog using the existing release
   identity, package `io.github.wanhanxi.aalyrics`, and versionCode `1004`.
   Replace the existing APK/checksum and sanitize the Release description.
3. Delete the four identified CI debug artifacts containing the old Changelog.
4. Verify published assets and metadata, remove temporary maintenance automation,
   review the final documentation-only diff, and open a personal Draft PR.
   Stop before merge as required by the repository workflow.

## Status and validation

- Personal PR descriptions #1, #2, and #10 have been sanitized. The other personal
  PR descriptions and inspected comments did not require edits.
- Current Changelog, activation, media-strategy, and recovery documentation use
  generic test descriptions. Successful repeated connection-trigger testing is
  retained; overnight, reboot, and process-reclamation checks remain unconfirmed.
- The existing 116 suites / 919 passing tests and successful main/release CI cover
  the unchanged runtime. Reuse that validation; rebuild only for bundled notes.
- Reset, persisted state, and UI layout are unchanged; no reset-path or Compose
  Preview change is needed.
- Pending: signed APK refresh, Release verification, debug-artifact removal,
  final scope/whitespace checks, and Draft PR creation.
