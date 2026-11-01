# Media listener connection recovery

## Scope and authorization

- On 2026-10-31 the owner approved improving automatic and in-app recovery after the exported Xiaomi 15 Pro / Android 16 logs showed notification access granted but no listener created in two consecutive Phone processes. Spotify restart coincided with eventual binding; the system's reason remains unknown.
- Personal topic `fix/media-connection-recovery`, originally based on `origin/main` at `ee865bc`. Target `WanHanxi/AALyrics:main`; implementation and validation are complete. The owner authorized integration, then explicitly requested undoing the direct integration and merging through an actual PR with branch cleanup.
- Preserve the separate `feature/phone-performance-mode` and reserved upstream `feature/lyrics-sync-offset` branches. No player-specific APIs, session-selection changes, force-unbind/component toggles, or new permissions.

## Acceptance criteria

- A production listener-binding signal comes from actual service callbacks, independently of Debug diagnostics and whether a player/song exists.
- Phone foreground, Android Auto projection, and active Automotive host demand share one bounded recovery owner. With notification access and a missing binding, request immediately, retry at 5 and 15 seconds, and time out at 30 seconds. Request return is never treated as connection success.
- A real connection cancels retries, including a late connection after timeout. Losing all demand cancels pending retries without disconnecting a healthy listener. Revoked access stops retries; returning from permission settings rechecks access. Repeated lifecycle events/manual taps do not create overlapping jobs.
- Phone Connecting/timeout status offers a connection-details action with Reconnect and Notification access settings. Timeout has explicit user-facing copy; retry is disabled during a running attempt. Normal connected/no-session and source eligibility states retain their meaning.
- Keep bounded phone-exportable diagnostics and add recovery attempt/outcome events. Record limited Android process-exit facts when available without descriptions, traces, notification content, or song metadata.
- No new durable preferences. Recovery state is process-local; existing permission and diagnostic retention/reset contracts remain unchanged.

## Checkpoints

- [x] Inspect current lifecycle, media ownership, Phone presentation, and incident evidence; select personal-main baseline.
- [x] Add callback-based listener state and testable bounded recovery; wire Phone/Automotive demand and permission checks.
- [x] Add actionable Phone connection status, timeout copy and production-component Previews; preserve diagnostics/export and record process exit facts.
- [x] Run focused tests, architecture checks, full regression/build, bounded branch review; align docs and phone handoff.
- [x] Push final coherent checkpoints; attempt the personal Draft PR after validation.
- [x] Revert the direct personal-main integration without rewriting history; synchronize the source branch with the reverted main and remove completed temporary branches.
- [x] Record PR-only integration and cleanup rules in the repository workflow.
- [ ] Create the personal PR, complete its checks, merge with the owner's existing authorization, and clean up the source branch after confirmed integration.

## Verification constraints

- Before Gradle, inspect memory and stop only task-owned emulator work. Run Gradle/tests/emulator sequentially, one worker, no parallel/daemon reuse, 1 GiB JVM, in-process Kotlin, under a verified 3 GiB systemd scope with zero swap.
- Reuse passing builds after documentation-only changes. Do not modify IDE settings or the user's Gradle daemon JVM file.
- Existing API 37 emulator exceeds this workstation's budget; use compiled Compose Previews and a physical Xiaomi/Android Auto handoff unless a suitable bounded device is already available.

## Checkpoint validation

- Core recovery checkpoint: `:platform:media:testDebugUnitTest :app:testDebugUnitTest` passed in 42 seconds; includes 12 deterministic recovery timing/lifecycle cases and the production binding-signal boundary test. Architecture and whitespace checks passed. Actual build cgroup verified MemoryMax=3221225472 and MemorySwapMax=0.

## Final validation and review

- Final production revision `7667815` passed `test :app:assembleDebug` in 1 minute: 821 tests across 103 suites, zero failures/errors/skips. New normal/narrow/large-text/landscape production-component Previews compiled. Architecture and whitespace checks passed.
- Reviewed the full branch against personal main: lifecycle aggregation, real-callback versus request-return semantics, cancellation/late callbacks, permission checks, healthy/no-player behavior, Phone state/action wiring, diagnostic privacy/bounds and reset handling. No unresolved current-scope blocking finding remains. No provider, session-selection, performance-mode or upstream changes are included.
- Both Gradle scopes verified MemoryMax=3221225472 and MemorySwapMax=0; neither remains loaded after completion. No emulator was started. User Gradle JVM file remains SHA256 `6c13aa5ba36690ccfbbb25a8807d01d98c15833e446d7ae82ddc726472cb8ef7`.
- Reuse this successful build after documentation-only changes. Device rendering and Xiaomi/Android Auto binding behavior still need the physical checks in `docs/MEDIA_CONNECTION_RECOVERY.md`; no runtime-device success is claimed.

## APK handoff

- `app/build/outputs/apk/debug/aalyrics-connection-recovery-7667815.apk`
- Package `io.github.whoxamxl.aalyrics.debug`; version `1.0.0-beta.1-dev+7667815`.
- APK SHA256: `7d2b127e24587e9c13e8603b3964a61e92f4ce7a1c15902cfe5b223d499edbc4`.
- Signature verified; local Debug certificate SHA256: `bbcffafa7296076c0a8c9565798a83c49f4860856e6743d758fa4d5aad1f5703`, matching the earlier diagnostic package.
- Based on personal main. The separately unmerged Phone Performance mode is not in this APK; this limitation was disclosed to the owner.
- Final PR targets the personal repository only. After the initial publication attempt, the owner explicitly authorized merging this connection-recovery change into the personal repository; no external review request was made.

## Initial publication limitation

- After final validation and push, Draft PR creation explicitly targeted `WanHanxi/AALyrics:main` from `fix/media-connection-recovery`. The GitHub connector returned HTTP 403 `Resource not accessible by integration` from the create-pull-request endpoint. This is an external connector permission failure, not a test failure or an automatic approval rejection.
- At the initial handoff, implementation and validation were complete and pushed, with no PR, external review request or merge performed. Existing session evidence established that no authenticated GitHub CLI or usable browser fallback was available.
- Personal comparison/handoff: https://github.com/WanHanxi/AALyrics/compare/main...fix/media-connection-recovery
- Only documentation followed the successful production build; the verified APK was reused without another Gradle run.

## PR integration after rollback

- On 2026-10-31 the owner requested another merge attempt and reaffirmed that unspecified repository/PR/merge work targets `WanHanxi/AALyrics`, never upstream. The default is now explicit in tracked `AGENTS.md` and `docs/FORK_WORKFLOW.md`.
- After another connector HTTP 403, direct squash commit `166bbbb` was pushed to personal main. The owner then explicitly requested undoing it and using a PR. Revert `ebc51d6` is now on `origin/main`; its tree exactly matches pre-integration `ee865bc`. Published history was not rewritten.
- Merged the reverted personal main into `fix/media-connection-recovery` without rewriting the published source branch. No runtime/build/test files changed from validated production revision `7667815`; reuse the successful 821-test/Debug build and existing APK.
- Deleted local `chore/merge-connection-recovery` and `chore/revert-direct-connection-merge`; neither was published as a remote branch. Retain the pending repair branch until the PR is actually merged. All unrelated branches, including Performance mode and upstream PR #91, remain unchanged.
- The current request authorizes creating and merging this personal PR and cleaning up its completed branches. If PR publication remains blocked, report the blocker and retain the source branch; do not fall back to another direct feature integration. No external review request or upstream change is authorized.
