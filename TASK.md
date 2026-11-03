# Manual notification-listener rebinding

## Scope and evidence

- Continue the owner's connection-recovery work after the 2026-10-31 22:51 PDT incident on Xiaomi 15 Pro / Android 16 / Spotify, using Debug revision `7667815`.
- The exported incident shows notification access granted but no listener created/connected. The manual tap at 22:51:13 triggered three ordinary rebind requests; every request returned without a binding callback. The owner confirmed that turning AALyrics Debug notification access off/on restored detection.
- Android 16 AOSP `ManagedServices.setComponentState` can return immediately when a requested component state is already enabled. This explains a limitation of repeated `requestRebind`, but the application's log cannot establish Xiaomi's internal state or why binding stopped. Recorded process-exit reason 13 means `REASON_OTHER`, not proof of a crash or memory/battery kill.
- Personal topic `fix/manual-listener-rebind` starts at personal main `23e928b`; target only `WanHanxi/AALyrics:main`. Preserve unrelated branches and upstream PR #91. Complete validation, push checkpoints, then open a Draft PR and stop; this follow-up has no merge authorization.

## Acceptance criteria

- On Android 14/API 34+, only a manual Reconnect after timeout requests one component `requestUnbind` immediately followed by `requestRebind`. Use the official static API, which does not require a live listener instance.
- Automatic startup/disconnect recovery and subsequent retries remain ordinary rebind requests at 0/5/15 seconds with a 30-second deadline. A manual episode resets only on its first request; repeated taps cannot overlap jobs or extend the deadline.
- Require active demand and notification access; never reset a known healthy listener, including one with no player. Check conditions again when the coroutine executes. Only actual listener connection callbacks prove success and cancel retries, including synchronous/late callbacks.
- Pair unbind/rebind without a cancellable delay. If unbind is unsupported or fails, still attempt ordinary rebinding. Record request/return/failure separately from actual binding; contain platform request failures and keep diagnostic fields free of exception messages or media/notification contents.
- Keep the existing notification-access Settings fallback because app requests cannot guarantee recovery from an OEM system fault. Do not toggle component enabled state, change permissions, add a keepalive, or change Spotify/session selection.
- No new persisted preferences or caches. System access stays user-owned, recovery stays process-local, and existing bounded diagnostic retention/reset semantics remain unchanged. Existing UI and Compose Previews remain applicable.

## Checkpoints

- [x] Read the new incident, confirm the user's successful Settings recovery, and inspect official Android API/AOSP binding behavior.
- [x] Fetch personal origin and create an isolated topic from current personal main.
- [x] Implement manual-only reset policy and the API-guarded unbind/rebind pair, with focused lifecycle/fallback tests.
- [x] Align recovery/runtime/settings documentation and phone testing instructions.
- [x] Run constrained tests/build, architecture and whitespace checks; review the complete branch against personal main.
- [x] Produce a signature-compatible Debug APK and record validation/limitations for the personal Draft PR.
- Publication handoff: push the final documentation checkpoint, open a Draft PR against `WanHanxi/AALyrics:main`, then stop. No merge or external review request is authorized for this follow-up.

## Verification constraints and handoff

- Inspect host memory before Gradle. Run builds/tests/emulator work sequentially with one worker, no parallel/daemon reuse, a 1 GiB Gradle JVM, in-process Kotlin, and a verified 3 GiB systemd scope with zero swap. Do not alter IDE/Gradle JVM settings or stop unrelated processes.
- No emulator is required for this binding-policy change. JVM tests validate ordering, lifecycle guards, timing and fallback behavior; they cannot reproduce Xiaomi's system notification-service state. Reuse a successful build after documentation-only changes.
- Verify the new Debug package/signing identity against the previous APK. Physical Xiaomi/Android Auto recovery remains a follow-up device check, not a claimed test pass. If manual recovery still fails, use the already verified notification-access off/on fallback and export the new events.
- The original process-exit/system scheduling cause remains unresolved and outside this narrow improvement. The separately unmerged Phone Performance mode is not included in the personal-main-based APK.

## Checkpoint validation

- `:platform:media:testDebugUnitTest :app:testDebugUnitTest` completed successfully: 459 tests across 61 suites, zero failures/errors/skips. This includes 16 recovery timing/lifecycle cases and five platform-request ordering/fallback/privacy cases.
- Architecture and whitespace checks passed. The build helper verified the actual cgroup had `memory.max=3221225472` and `memory.swap.max=0`; no emulator was started.
- An additional `:platform:media:lintDebug` check failed on unchanged `AndroidMediaSessionSource.kt:198` (`NewApi` for the existing pending-intent launch helper). That helper already returns null below API 34; lint does not infer the SDK guard through its result. The report also contains two existing `InlinedApi` warnings in that helper and a newer compileSdk advisory. No lint finding targets the new rebind code. This unrelated static-analysis issue is recorded for follow-up; no baseline/suppression or session-launch change was added here.

## Final validation and review

- Production revision `7439e52` passed `test :app:assembleDebug` in 42 seconds: 830 tests across 104 suites, zero failures/errors/skips. Existing Phone Compose Previews remain compiled and unchanged. Architecture and branch-wide whitespace checks passed.
- Reviewed all changed application/platform code, tests and docs against personal main `23e928b`. Confirmed manual-timeout-only reset, access/demand/healthy-listener guards, lazy-job assignment and reentrant/late callbacks, one reset per episode, immediate request pairing, API 34 guard and failure fallback, diagnostic privacy, and unchanged reset/retention contracts. No unresolved current-scope blocking finding remains.
- The optional lint limitation above is retained; no all-checks-green claim is made. Remote GitHub Actions results are not part of this local validation.
- Both Gradle runs verified a 3 GiB memory ceiling with no swap. Their systemd scopes are now inactive/not loaded; no emulator was started. The user's Gradle JVM file still has SHA256 `6c13aa5ba36690ccfbbb25a8807d01d98c15833e446d7ae82ddc726472cb8ef7`.
- Remote personal main was rechecked at `23e928b`; reserved Sync and unrelated Performance branch heads remain unchanged. Only documentation follows the successful production build, so reuse the verified APK without rebuilding.

## APK and device handoff

- APK: `app/build/outputs/apk/debug/aalyrics-manual-rebind-7439e52.apk`.
- Package `io.github.whoxamxl.aalyrics.debug`; version `1.0.0-beta.1-dev+7439e52`; versionCode 1.
- APK SHA256: `0ca9d839124f6a4c34ac63dbd94d8444a36bcae64b7d714652952f9325557acb`.
- Verified Debug certificate SHA256: `bbcffafa7296076c0a8c9565798a83c49f4860856e6743d758fa4d5aad1f5703`, matching the previous Debug APK for an in-place install.
- On the next incident, keep Spotify playing, wait for timeout, and tap Reconnect once. Check whether the song returns; export diagnostics even if a subsequent notification-access off/on is still needed. The new log distinguishes the unbind/rebind request pair from real service creation/connection. No physical Xiaomi recovery result is claimed yet.
