# Android Auto media-session publication order

## Scope

Start from personal `main` at `4a1562f` on `fix/android-auto-session-order`;
target `WanHanxi/AALyrics:main`. Correct the order in which the lyrics session
publishes playback and announces activation, so Android Auto can observe the
current session ordering when projection playback starts.

Publish metadata and real playback state before initial activation. For a session
already active when projection playback starts, refresh its active state once
after publishing a valid playing snapshot. Carry a connection-scoped request from
the application's existing activation owner so a host-retained browser service
can handle the next connection too. Do not refresh on ordinary ticks, track
changes, pause/resume, or metadata gaps within the same connection.

No new preference, fake playback, extra media session, audio-focus request,
provider change, version bump, UI redesign, or change to transport routing.
Android Auto owns display selection; this is not a permanent pin API. Real car
instrument-cluster behavior remains separate from Desktop Head Unit validation.

## Checkpoints

1. Implement publication/activation sequencing and focused lifecycle tests.
   Verify architecture, unit tests, and Debug build within workstation limits;
   commit and push the implementation checkpoint.
2. Update the existing phone Debug install without removing its data. Validate
   actual AA selection using wireless ADB and Desktop Head Unit, then document
   the observed results and limits.
3. Review the branch-wide diff and final checks, commit/push, and open a personal
   Draft PR. Stop before review requests, ready-for-review, or merge.

## Acceptance and validation

- [x] Initial state is published before the session becomes active.
- [x] Already-playing and connect-before-playing cases refresh at most once per
  connection/session; disconnected or invalid playback cannot consume a request.
- [x] A surviving service can handle a new connection; repeated connection facts,
  pause/resume, lyrics ticks, track/source changes, and gaps do not repeat refresh.
- [x] Focused tests, architecture guardrail, whitespace check, and Debug build pass.
- [x] Physical-phone/DHU checks cover already playing, playback after connection,
  repeated reconnects, return to maps, and passive playback across track changes.
  Record actual results, including any failed scenario, rather than treating
  successful service creation as successful AA selection.
- [x] Public documentation uses generic device descriptions and states what was
  observed. Raw logs and location-containing screenshots remain local only.

Reset review: all new request/activation bookkeeping is process-local. No new
persisted preference or cache, so `Reset AALyrics` has no additional reset target.
No user-facing layout/copy changes; existing Compose Previews remain applicable.

## Completed validation

- Implementation checkpoint `a16ddc2` publishes real playback before activating
  the lyrics session and accepts one connection-scoped refresh request.
- 462 Debug unit tests passed across `:app` and `:ui:automotive`; architecture,
  whitespace, baseline, and branch-wide scope checks passed. Debug APK built
  successfully with one Gradle worker, a 1 GiB JVM heap, and a verified 3 GiB
  systemd memory limit with no swap. Later changes are documentation only;
  reuse the successful build/tests.
- Updated the physical phone's existing Debug installation without removing data.
  Four DHU connections selected AALyrics without manually opening it after each
  connection. Covered music already playing, AA autoplay after connection, and
  a prior foreground Spotify selection.
- Returning from Spotify's main view to maps restored AALyrics. Pause/resume
  through the lyrics card worked without another activation refresh. Confirmed
  the outgoing source separately from the dashboard carousel, which can still
  show a Spotify card.
- Final uninterrupted passive observation lasted 6m31s and crossed one natural
  track transition. No AA source change or extra session activation occurred;
  DHU still reported AALyrics Debug as its output source at the end. Across four
  connections, diagnostics recorded four initial activations and four connection
  refreshes. Spotify remained the system media-button target.
- An earlier passive run ended with a DHU transport disconnect; it is excluded
  from the continuous-duration result. Raw logs/screenshots remain local only.
- DHU, observation, and build processes have finished; removed the task's port
  forward. The updated Debug app and wireless debugging pairing remain available.

Host bindings were released on actual disconnects in this test, so retaining one
service across reconnect has lifecycle-test coverage only. Real vehicle cluster,
overnight/reboot, and cross-version behavior remain follow-up testing, not
current success claims. Android Auto still owns selection; no permanent pin is
promised. No new settings, persistent state, or Preview/reset changes are needed.

Implementation and final validation are complete. Open the personal Draft PR and
stop before review requests, ready-for-review, or merge.
