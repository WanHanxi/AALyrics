# Phone Performance mode: centered lyrics and album background

## Approved scope

- The owner selected the compact centered design and approved implementation with a softly blurred, darkened album-art background on 2026-11-02. Cover portrait and landscape; keep current lyrics and their translation readable at large font scales.
- Continue the unmerged Phone fullscreen implementation on personal main `2d72cb2`, retaining its notification-listener recovery fixes. Branch `feature/phone-performance-redesign` follows the repository's CI branch-name policy. The original Performance branch and the reserved upstream Sync branch remain untouched.
- Carry forward fullscreen entry/exit, resumed-only immersive/screen-awake effects, authoritative three-row selection, shared WORD Karaoke and PLAIN fallback. Do not alter providers, timing, Translation execution, playback controls or Android Auto.
- No PR during implementation or before the selected design is settled. Complete implementation, validation and visual handoff before a personal Draft PR; no merge or review request is authorized.

## Acceptance criteria

- Replace weighted vertical slots with a compact measured group near the visual center. Current text uses 48sp in portrait and 60sp in landscape, with normal system font scaling; neighbors and translation have a quieter hierarchy.
- Measure wrapped content. Hide the previous row first under pressure, then the next if needed. Keep the entire current original/translation reachable through scrolling when it exceeds the viewport. Never ellipsize or shrink the current lyric.
- Keep a compact title/artist header and accessible exit button; provider attribution is a quiet footer. Respect safe drawing insets and short windows.
- Reuse existing playback artwork, crop without distortion, soften/darken it with additional lyric-region contrast, and crossfade artwork changes. Missing artwork returns to the plain dark base without a giant fallback logo.
- Artwork preparation is bounded, asynchronous and keyed to artwork changes, never playback/Karaoke ticks. No network request, disk cache or durable preference is added. The existing reset clears transient fullscreen visibility; derived artwork is released with the surface.
- Production Compose Previews cover both orientations, bright/dark/busy/no artwork, translated and WORD lyrics, long lines/large fonts, intro/end, PLAIN and empty states.

## Checkpoints

- [x] Inspect approved prototype, existing fullscreen branch, artwork seam and current personal main.
- [x] Carry existing fullscreen source onto current personal main without dropping connection-recovery changes.
- [x] Implement measured centered layout, responsive typography and focused geometry tests.
- [x] Add bounded album-background preparation, contrast/crossfade/fallback and production Previews.
- [ ] Validate tests/build/architecture/whitespace under workstation limits; inspect complete branch and align docs.
- [ ] Provide visible portrait/landscape previews and an installable Debug APK; record actual device-validation limits.
- [ ] Push coherent checkpoints and, once all criteria are complete, open a personal Draft PR and stop.

## Verification budget and handoff

- Inspect available memory before heavy work. Run Gradle/tests/preview work sequentially, with no emulator running during compilation.
- Gradle: one worker, no parallel/daemon reuse, 1 GiB heap, 512 MiB metaspace, in-process Kotlin; verify a task-specific systemd scope has `memory.max=3221225472` and `memory.swap.max=0` before launching it.
- Preserve the user's IDE and generated JVM configuration. Reuse successful builds after docs-only changes. Do not retry the API 37 emulator that previously forced a 4 GiB guest allocation under a 3 GiB limit.
- Physical-device checks remain entry/exit/Back, rotation/cutouts/system bars, background/resume, artwork changes/missing artwork, bright covers, long original/translation at large font scale, WORD sweep, pause/seeks/source loss, and normal Follow restoration.
- Origin SSH fetch currently fails because its agent cannot sign. HTTPS fetch succeeded and personal main remains `2d72cb2`; publication will be attempted through available authorized credentials after implementation.

## Checkpoint evidence

- Focused `:ui:phone:testDebugUnitTest :app:testDebugUnitTest` passed in 55 seconds: 74 Phone tests and 402 app tests, zero failures/errors/skips. This compiles the production Compose Previews, but is not a device-rendering result.
- The test process verified `memory.max=3221225472` and `memory.swap.max=0`. Architecture and whitespace checks passed; the generated Gradle JVM file is unchanged.
- HTTPS checkpoint push cannot authenticate (`could not read Username` with prompts disabled). SSH signing also failed as recorded above; local coherent commits are preserved. Check connected GitHub publication capability before declaring a final blocker.
