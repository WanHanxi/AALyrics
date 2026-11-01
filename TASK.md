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
- [ ] Implement manual-only reset policy and the API-guarded unbind/rebind pair, with focused lifecycle/fallback tests.
- [ ] Align recovery/runtime/settings documentation and phone testing instructions.
- [ ] Run constrained tests/build, architecture and whitespace checks; review the complete branch against personal main.
- [ ] Produce a signature-compatible Debug APK, record validation/limitations, push final checkpoints, and open the personal Draft PR.

## Verification constraints and handoff

- Inspect host memory before Gradle. Run builds/tests/emulator work sequentially with one worker, no parallel/daemon reuse, a 1 GiB Gradle JVM, in-process Kotlin, and a verified 3 GiB systemd scope with zero swap. Do not alter IDE/Gradle JVM settings or stop unrelated processes.
- No emulator is required for this binding-policy change. JVM tests validate ordering, lifecycle guards, timing and fallback behavior; they cannot reproduce Xiaomi's system notification-service state. Reuse a successful build after documentation-only changes.
- Verify the new Debug package/signing identity against the previous APK. Physical Xiaomi/Android Auto recovery remains a follow-up device check, not a claimed test pass. If manual recovery still fails, use the already verified notification-access off/on fallback and export the new events.
- The original process-exit/system scheduling cause remains unresolved and outside this narrow improvement. The separately unmerged Phone Performance mode is not included in the personal-main-based APK.
