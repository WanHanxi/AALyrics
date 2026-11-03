# Phone Performance mode: centered lyrics and album background

## Active follow-up: space-preferred wrapping (2026-11-03)

- The owner approved prioritizing existing spaces for fullscreen line breaks after a phone screenshot split `這天` across lines. Continue on the existing personal topic branch; the visual-design gate still defers PR creation.
- Measure real text width, keep complete space-delimited phrases together when possible, and retain platform wrapping for oversized phrases. Cover original/neighbor/translated text, orientation/font changes and existing explicit newlines.
- Keep canonical text and UTF-16 Karaoke offsets unchanged; do not alter timing, providers, normal scrolling lyrics, durable state or reset behavior.
- [x] Implement focused wrapping and regression tests, including the reported Chinese line and WORD offsets.
- [x] Add native Compose Preview cases for portrait/landscape, large fonts and wrapped WORD sweep; align the fullscreen documentation.
- [ ] Validate Phone tests, architecture/whitespace and the updated Debug APK within the workstation memory budget.
- [ ] Publish the coherent checkpoint and deliver the updated privately uploaded APK for phone verification. No PR yet.

Follow-up validation: `:ui:phone:testDebugUnitTest` passed (85 tests in 12 suites, including 11 new wrapping regressions; zero failures/errors/skips). The run verified `memory.max=3221225472` and `memory.swap.max=0`, using one worker and in-process Kotlin. Production Preview fixtures compile, including the reported Chinese line and a WORD sweep across the replaced separator. Architecture and whitespace checks passed. Native rendering remains a phone-verification handoff rather than a claimed emulator result.

## Approved scope

- The owner selected the compact centered design and approved implementation with a softly blurred, darkened album-art background on 2026-11-02. Cover portrait and landscape; keep current lyrics and their translation readable at large font scales.
- Continue the unmerged Phone fullscreen implementation on personal main `2d72cb2`, retaining its notification-listener recovery fixes. Branch `feature/phone-performance-redesign` follows the repository's CI branch-name policy. The original Performance branch and the reserved upstream Sync branch remain untouched.
- Carry forward fullscreen entry/exit, resumed-only immersive/screen-awake effects, authoritative three-row selection, shared WORD Karaoke and PLAIN fallback. Do not alter providers, timing, Translation execution, playback controls or Android Auto.
- The owner explicitly asked for no PR before the visual design is settled. Implement the selected direction, finish validation, and hand over the preview/APK for visual feedback; defer PR creation until that design gate is cleared. No merge or review request is authorized.

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
- [x] Validate tests/build/architecture/whitespace under workstation limits; inspect complete branch and align docs.
- [x] Prepare visible portrait/landscape previews and an installable Debug APK; record actual device-validation limits.
- [x] Publish coherent checkpoints to the personal feature branch through the GitHub connector, verifying every tree against the local commit.
- Publication handoff: publish this final documentation checkpoint and provide the preview/APK, then stop for the owner's visual feedback. Do not open a PR yet under the explicit design gate above. A later authorized Draft PR must target `WanHanxi/AALyrics:main`.

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

## Final validation and scope review

- `test :app:assembleDebug` passed in 59 seconds at local source revision `d2b6e65`: 852 tests across 108 suites, zero failures/errors/skips. Production Compose Preview fixtures compiled. No emulator or physical-device rendering result is claimed.
- Architecture and branch-wide whitespace checks passed. Reviewed the complete branch against personal main `2d72cb2`: the shared Karaoke extraction preserves normal scrolling behavior, recovery code/callbacks remain intact, the fullscreen layout consumes existing authoritative rows, and only Phone presentation/window ownership changes. Providers, durable preferences/caches, Android Auto and reset retention semantics are unchanged.
- Both Gradle runs verified a 3 GiB memory cap with zero swap. The isolated browser preview used a separate verified 1 GiB cap with zero swap after Gradle finished. All three scopes are inactive/not loaded; no task Java/emulator process remains. No IDE/JVM settings were altered; `gradle/gradle-daemon-jvm.properties` still hashes to `6c13aa5ba36690ccfbbb25a8807d01d98c15833e446d7ae82ddc726472cb8ef7`.
- The conversation mockup was visually inspected and exercised across 144 combinations of 320/360/760px host widths, dark/white/busy/missing covers, short/long Chinese/English text, translation on/off and 1.0/1.8 font scale. No JavaScript errors or geometry overlap/overflow were found. These are browser-rendered design illustrations, not Android screenshots. Production Compose Previews and the APK are provided for native inspection within the workstation's resource budget.
- Native device follow-up is limited to the listed rotation/insets, lifecycle, live artwork transition, Karaoke and oversized-text checks. No new runtime feature is deferred. The previous API 37 emulator was not restarted because it exceeded the workstation budget.

## Publication and APK handoff

- The connected GitHub API can write the personal repository. Published the seven coherent local checkpoints as equivalent Git trees on `feature/phone-performance-redesign`; each returned tree SHA was checked before updating that branch with an expected-head lease. No PR was opened during implementation.
- Published source commit `16e9e10` and locally built `d2b6e65` have the identical tree `2f13d0078ef1850442da497fdf7cf2ad3642843f`. Commit IDs differ because the API generated new commit metadata. The clean local topic now follows the published branch; `backup/phone-performance-build-d2b6e65` retains the exact local build revision. Original Performance and reserved Sync branches remain untouched. Personal main was re-fetched and remains `2d72cb2`.
- APK: `app/build/outputs/apk/debug/aalyrics-performance-d2b6e65.apk`; package `io.github.whoxamxl.aalyrics.debug`, version `1.0.0-beta.1-dev+d2b6e65`, versionCode 1.
- APK SHA256: `24918b44a299576b8ecc1b2622091fdb7f5c439b4e7a3fd6a801568dbb6584ab`.
- Debug signing certificate SHA256: `bbcffafa7296076c0a8c9565798a83c49f4860856e6743d758fa4d5aad1f5703`, matching the earlier Debug handoff for an in-place install.
- Only documentation follows the successful source build; reuse the verified APK without rebuilding. No remote CI or physical-device success is claimed.
