# Compact lyrics offset panel

## Scope

- Continue the personal `feature/lyrics-sync-offset` branch from `35775b9`. No PR or merge; preserve user-owned Gradle/SDK files.
- Reduce the Lyrics Offset card height: combine the editable value and Reset in one header, use a smaller value size with a pencil affordance, and put −500 / −50 / +50 / +500 on one row with shared direction/unit labels.
- Replace repeated help/range paragraphs with a short scope footer. Full direction/scope/range guidance stays in the exact-value editor.
- Keep at least 48 dp touch heights, full accessibility action descriptions, and two grouped rows on very narrow displays or large font scales.
- Preserve all timing, exact input, three-tap, persistence, limits, Reset, and Phone/Android Auto behavior. No new persisted state; the reset contract is unchanged.

## Plan

- [x] Implement the compact card and normal/narrow/large-font/limit previews.
- [x] Verify build, existing timing/input regressions, and architecture; align docs and prepare the personal-branch handoff.
- [x] Attempt bounded emulator verification and record its limits without increasing resource usage.

## Validation

- Resource constraint from the user: keep compilation and emulator checks sequential. Reuse the successful existing build. Future builds use one worker, no parallel execution, no persistent daemon, a 1 GiB Gradle heap, and in-process Kotlin compilation. Bound the temporary emulator to a 3 GiB cgroup with no extra swap; release it immediately after verification.
- No new unit tests for this layout-only change; retain existing behavioral coverage and add targeted Compose previews.
- Build and 31 focused regression tests passed (26 timing/calibration tests and 5 exact-input tests). Architecture guardrails and `git diff --check` passed. Six targeted interactive Compose fixtures cover normal/narrow/large-font/limit/zero/unavailable states. Actual visual verification could not finish: the API 37 emulator hung during boot and crashed before rendering the app. The isolated systemd scope enforced a 3 GiB hard limit with no additional swap; systemd reported a 2.5 GiB peak. No larger or repeated emulator run was attempted after that bounded check.
- Cleanup confirmed: no task-owned emulator, Gradle daemon, or waiting emulator command remains. Available host memory recovered to about 9.9 GiB. The user can inspect the compact layout by running the app from Android Studio on their phone; no exported APK is needed.
- Changes remain on the personal feature branch; no PR or merge. The generated Gradle JVM file remains untouched.
