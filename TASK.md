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
- [Refresh run 42779790900](https://github.com/WanHanxi/AALyrics/actions/runs/42779790900)
  passed from documentation source `993df7c`. It rebuilt and replaced the signed
  v1.2.1 APK/checksum, sanitized the Release body, and removed the four affected
  debug artifacts from runs `42773060705` and `42774561114`.
- Independent public downloads passed the package/version, signing-certificate,
  non-debuggable-build, bundled-Changelog, and SHA-256 checks. All six bundled
  documents and the updated PR/Release text pass the known-device-details scan.
  APK SHA-256: `3b5dfbd154062a6cd769e1445a91e78ee29e1586ea61c69f618fcdb453eee165`.
- Remote `main` and the original annotated v1.2.1 tag were verified unchanged.
  The temporary maintenance workflow has been removed from the branch's final
  tree; the final diff contains documentation changes only.
- Architecture, whitespace, and branch-wide scope checks passed. The current
  documents still need normal Draft PR integration into personal `main`.
