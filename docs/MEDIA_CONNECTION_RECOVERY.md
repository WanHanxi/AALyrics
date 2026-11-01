# Media listener connection recovery

## Problem and ownership

Notification access being enabled does not prove that Android has bound AALyrics' `NotificationListenerService`. The 2026-10-31 Xiaomi 15 Pro / Android 16 incident showed two successive Phone processes with access granted, no service created and zero session observations. Restarting Spotify coincided with eventual binding, but application logs did not establish why Android delayed it. An earlier Automotive incident took about 37 seconds to bind after a rebind request returned.

`MediaListenerConnectionSink` carries actual connected/disconnected/destroyed callbacks from `:platform:media` to the application. It is independent of Debug diagnostics and selected media sessions. A bound listener with no player is healthy and does not need rebinding. `MediaSessionObservation` still waits for `onListenerConnected()` before using active-session APIs; source selection, metadata stabilization and playback ownership are unchanged.

The application owns `MediaConnectionRecovery` on the main thread. Phone process foreground, Automotive projection, and Automotive browser-service demand use the existing aggregate demand gate. The platform service reports binding events and exposes the Android request helper; it no longer owns a separate retry path. A healthy listener continues observing media when lyrics demand goes away. Only recovery requests are demand-gated.

## Recovery policy

- On demand activation with notification access and a missing binding, request immediately, then retry after 5 and 15 seconds from the start of the episode. At 30 seconds, stop and publish `TIMED_OUT`. Android scheduling may delay execution of these deadlines.
- An API return means only that the request returned. Only an actual connection callback publishes `CONNECTED` and cancels pending work.
- A late connection clears timeout, even after the retry budget has ended.
- Repeated lifecycle notifications and manual taps during an active episode cannot create overlapping jobs or extend its deadline.
- Losing the final demand source cancels pending work. The next demand activation can start a new episode. Removing Phone demand alone does not cancel Automotive recovery, and vice versa.
- Missing access prevents requests. Access is checked before each request and at timeout; Phone resume also rechecks it immediately. Restoring access can start recovery without a process restart.
- A manual retry after timeout starts a fresh bounded episode. It never disconnects an already healthy listener.
- System access remains user-owned. There is no force-unbind, component enable/disable workaround, started-service keepalive, boot receiver, Spotify-specific integration, or permission change.

This addresses missing notification-listener binding. A healthy binding with no active player, a source eligibility rejection, a session query/attach failure, or a presentation package mismatch retains its existing meaning; none is treated as proof that rebinding is necessary.

## Phone behavior

The application maps recovery and existing source facts into presentation-only Phone state:

- While waiting for a missing binding, the top-right `Connecting` pill opens **Playback connection**. It explains that automatic attempts end after 30 seconds; Reconnect is disabled during that attempt.
- After timeout, the pill says **Connection failed**. Its dialog offers **Reconnect** and **Notification access settings**. If retry fails, the copy explains how to turn AALyrics access off and on in Android Settings.
- Successful binding removes the recovery UI and resumes normal source presentation. A bound listener without a player displays the existing Disconnected state.
- Missing access retains the mandatory Phone onboarding gate. The recovery dialog also has access-required copy if permission changes while a surface is active.
- Connected player-launch behavior and source eligibility/error tooltips remain intact. Narrow status text can ellipsize without removing the action. Dialog content scrolls for narrow/landscape/large-text layouts.

Android Auto receives automatic recovery through application demand. System permission setup and the manual recovery dialog stay on the phone; no Automotive template or driving interaction is added.

## Diagnostics and reset

Production recovery does not depend on logging. Debug builds add `recovery_attempt`, `recovery_state`, and `recovery_manual` events to existing bounded, exportable connection diagnostics. A timeout remains a stalled episode for incident capture even though Phone now presents an error instead of an endless Connecting spinner.

On Android 11+, startup also asks Android for at most three recent exits of AALyrics processes. Only PID, UTC exit time, reason/status codes and importance are recorded. Descriptions, traces, song metadata and notification content are excluded. Android may have no exit record, and an exit reason alone does not explain a later system binding delay.

Recovery state/jobs are process-local, with no durable settings and no new reset target. Reset does not revoke system access or force-disconnect a healthy listener. Existing bounded diagnostic retention intentionally survives Reset AALyrics; Android Clear storage or uninstall removes it. See `PHONE_SETTINGS.md` and `PRIVACY.md`.

## Verification and phone handoff

Deterministic tests cover cold start, exact attempt budget, request-return semantics, synchronous/late connection, duplicate events/taps, background cancellation, permission revocation/restoration, and overlapping Phone/projection/host demand. Mapper tests distinguish missing binding, timeout, access loss and a healthy listener without media. Production-component Compose Previews cover the timeout pill and connecting/timeout/access dialogs at normal, narrow/large-text and landscape sizes.

Physical-device checks remain necessary because JVM tests cannot reproduce Xiaomi's notification-service scheduling:

1. Play Spotify before opening AALyrics; try a cold Phone launch and an Automotive-only launch.
2. Confirm that a normal connected player, pause, no-player state and source filtering do not start rebind loops.
3. During a delayed binding, tap Connecting; after timeout, tap Reconnect and verify that a late successful connection clears the dialog/status.
4. Open Notification access settings, turn AALyrics access off/on, return, and confirm fresh detection without restarting Spotify.
5. Background/return, rotate the phone, and overlap Phone use with Android Auto; verify no duplicate requests and no cancellation while a car demand source remains active.
6. Export diagnostics later from Settings > Advanced > Debug. Record the local incident time; the export itself uses UTC.

The test APK is based on personal main, with the separate unmerged Phone Performance-mode branch excluded. No claim of Xiaomi/Android Auto device validation or of fixing the system's underlying scheduling cause is implied by a successful build.
