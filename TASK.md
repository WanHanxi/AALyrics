# Android Auto media selection and 1.2.2 preparation

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
provider change, UI redesign, or change to transport routing. The follow-up below
prepares the 1.2.2 release in this same PR.
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

Host bindings were released on actual disconnects in DHU, so retaining one
service across reconnect has lifecycle-test coverage only. The owner subsequently
reported successful priority selection across several real-car connections.
Vehicle-specific instrument-cluster rendering, overnight/reboot recovery, and
cross-version behavior are not claimed as separately confirmed. Android Auto still owns selection; no permanent pin is
promised. No new settings, persistent state, or Preview/reset changes are needed.

## 1.2.2 follow-up (2026-11-10)

The owner reported that several real-car connections successfully prioritized
AALyrics and requested a clearer PR description, a README Highlights entry, and
release of this PR as 1.2.2. Future Release notes should omit
routine Updating and installation text unless a release requires manual action.

- [x] Describe the user-visible media-selection effect and the one-time
  publication/activation mechanism, keeping actual audio playback with Spotify.
- [x] Record the in-car feedback without device identifiers or unsupported
  instrument-cluster compatibility claims.
- [x] Add the 1.2.2 Changelog entry and update README/release preparation docs.
  Preserve existing signing, package, provider configuration, and versionCode policy.
- [x] Make the action-only installation-notes policy explicit for future releases;
  retain historical release entries and actionable legacy-cache instructions.
- [x] Verify note extraction, documentation consistency, and the updated PR CI.
- [x] Update PR #12's title/description around the complete implementation and
  1.2.2 scope, with the validation results.

Runtime source has not changed since the successfully tested and device-validated
implementation. Reuse the existing local build/test evidence. Build CI run
`42948535245` passed for documentation/release checkpoint `cadba02`, including the
updated bundled Changelog. Release-note extraction matches the 1.2.2 section;
historical Changelog entries are unchanged. Architecture, whitespace, and
documentation consistency checks passed.

The owner's request authorizes publishing this PR as 1.2.2. After the final PR
checks, squash-merge into personal main and create `v1.2.2` on the merged commit.
The Release workflow must pass and publish the signed APK and checksum; verify
the published notes, package/version, signing certificate, and bundled Changelog.
Do not tag the feature-branch head. Publication evidence belongs to the GitHub
Release and its workflow run.
