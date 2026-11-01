# Media listener connection recovery

## Scope and authorization

- On 2026-10-31 the owner approved improving automatic and in-app recovery after the exported Xiaomi 15 Pro / Android 16 logs showed notification access granted but no listener created in two consecutive Phone processes. Spotify restart coincided with eventual binding; the system's reason remains unknown.
- Personal topic `fix/media-connection-recovery`, based on `origin/main` at `ee865bc`. Target `WanHanxi/AALyrics:main`; complete validation before Draft PR creation, then stop before merge.
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
- [ ] Add callback-based listener state and testable bounded recovery; wire Phone/Automotive demand and permission checks.
- [ ] Add actionable Phone connection status, timeout copy and production-component Previews; preserve diagnostics/export and record process exit facts.
- [ ] Run focused tests, architecture checks, full regression/build, bounded branch review; align docs and phone handoff.
- [ ] Push final coherent checkpoints and create personal Draft PR if the GitHub integration permits it.

## Verification constraints

- Before Gradle, inspect memory and stop only task-owned emulator work. Run Gradle/tests/emulator sequentially, one worker, no parallel/daemon reuse, 1 GiB JVM, in-process Kotlin, under a verified 3 GiB systemd scope with zero swap.
- Reuse passing builds after documentation-only changes. Do not modify IDE settings or the user's Gradle daemon JVM file.
- Existing API 37 emulator exceeds this workstation's budget; use compiled Compose Previews and a physical Xiaomi/Android Auto handoff unless a suitable bounded device is already available.
