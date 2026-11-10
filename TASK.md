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
- [ ] Physical-phone/DHU checks cover already playing, playback after connection,
  repeated reconnects, return to maps, and passive playback across track changes.
  Record actual results, including any failed scenario, rather than treating
  successful service creation as successful AA selection.
- [ ] Public documentation uses generic device descriptions and states what was
  observed. Raw logs and location-containing screenshots remain local only.

Reset review: all new request/activation bookkeeping is process-local. No new
persisted preference or cache, so `Reset AALyrics` has no additional reset target.
No user-facing layout/copy changes; existing Compose Previews remain applicable.

## Status

Investigation found stale AA session ordering despite an already-correct Android
stack. A controlled list-refresh experiment changed AA's selection without
playback transport or audio-focus actions. Product implementation/validation is
in progress; that experiment alone is not acceptance of this fix.

Implementation checkpoint: 462 Debug unit tests passed across `:app` and
`:ui:automotive`; architecture and whitespace checks passed. Debug APK built
successfully with one Gradle worker, a 1 GiB JVM heap, and a verified 3 GiB
systemd memory limit with no swap. Device validation is next.
