# Phone fullscreen Performance mode

## Product contract

The Lyrics Track Card's fullscreen action opens a Phone-local Performance surface. The owner selected the previous/current/next presentation on 2026-10-31, then approved the compact centered layout and album-art background on 2026-11-02. The normal Lyrics destination keeps its responsive scrolling document.

Performance mode replaces the Top Bar, Track Card, Playback Surface and Bottom Navigation with compact track identity, provider attribution, an exit button and lyrics. System Back and the exit button return to Lyrics in Follow mode. Playback controls remain available after exiting. Tapping lyrics does not accidentally exit the surface.

The exit button stays visible in portrait, landscape, loading and empty states. Its touch target is at least the standard Material IconButton size. Layout respects safe drawing insets, including display cutouts and temporarily revealed system bars.

## Timed presentation

LINE and WORD sources consume the existing `LyricsViewportUiState.currentLineIndex`:

- previous logical line above;
- current logical line centered, larger and brighter;
- next logical line below;
- before the first timestamp, a centered musical note with the first line in the next slot;
- no invented previous/next row at document boundaries;
- blank timed lines render a musical note without skipping their timing interval.

The current line includes its already identity-gated translated text when available. Neighbors show canonical text. The measured group replaces the old weighted vertical slots: the current original/translation block sits near 44% of the available lyric area, with adjacent lines only a content-sized gap away. Portrait uses 48sp/56sp current text, 26sp/32sp neighbors, 20sp/26sp translation and 24dp neighbor gaps. Landscape uses 60sp/70sp, 28sp/34sp, 22sp/28sp and 16dp respectively. System font scaling remains enabled. The title/artist header stays compact, and provider attribution sits in a quiet footer.

Current original and translation share the `TextPrimary` white color, with the smaller translation size and 8dp spacing expressing their hierarchy within one current block. Previous/next lyrics retain `TextSecondary`; WORD originals retain their existing progress-dependent Karaoke colors while the translation stays white.

Every row is measured at its actual wrapped size. If the complete group does not fit, hide the previous row first, then the next if necessary. Never ellipsize or shrink the current lyric: its original and translation share a scroll container constrained to the lyric area, so even an oversized line remains fully reachable. Scroll position resets when the displayed current line changes. Missing neighbors reserve no empty slots; hidden rows do not overlap the current block or the exit control.

Fullscreen originals, neighbors and translations prefer wrapping at existing spaces (including tabs and full-width spaces). Pack complete phrases using the actual font metrics and available width; a line that fits stays intact. Only a phrase wider than the available width falls back to platform soft wrapping. Preserve existing explicit line breaks and nonbreaking spaces. The display replaces selected separators with newlines without inserting or removing UTF-16 units, preserving canonical WORD Karaoke offsets in both text layers. Recompute on text, font or width changes, not playback-progress ticks. This is transient presentation; provider text, cached lyrics, timing, normal scrolling lyrics and reset/storage semantics do not change.

The surface selects at most three existing rows. It does not copy or remap the entire lyrics document on the playback tick. Seeks, track changes, pauses, and the global timing offset are reflected through the existing app-owned Phone mapping.

## Album background

The host passes its existing playback artwork directly to the Phone surface. It is cropped to fill each orientation without stretching. A small, softened version preserves recognizable cover shapes/colors, with a dark base scrim and a stronger central gradient protecting lyrics even on white or busy covers. Artwork changes crossfade over 400ms. Missing or unreadable artwork fades to the plain dark base; the branded missing-cover logo is not enlarged behind lyrics.

Phone-local preparation runs on `Dispatchers.Default` once per artwork identity while fullscreen is composed, independently of timing ticks and viewport size. The derived bitmap's longest side is at most 192px; three radius-3 separable box passes provide blur on every supported Android version, including API 26–30. Shared source bitmaps are never modified or recycled. Obsolete preparation cannot publish after its coroutine is cancelled. Debug Preview performs the same preparation synchronously because its render clock may not advance.

There is no new network request, disk cache or setting. Derived images are transient composition state and become reclaimable on exit, replacement, recreation or reset; only the outgoing/incoming images are needed during a normal transition. Existing playback artwork ownership and persistent lyrics-cache retention do not change.

## Karaoke and plain lyrics

WORD Karaoke uses the same `KaraokeLineText` continuous-sweep renderer as the normal viewport, with centered text. Both existing Karaoke switches must still be enabled. Entering fullscreen does not enable Karaoke, request different lyrics or change Translation settings. LINE never synthesizes word timing.

PLAIN retains the existing scrollable `LyricsViewport`, including its optional estimated auto-scroll and Follow/Browse interaction. An explicit Unsynchronized lyrics label prevents the presentation from suggesting authoritative line timing. There is no synthetic three-row clock.

Loading, no media, not found and failure use explicit status text. A transition away from usable lyrics replaces old rows rather than leaving stale lyrics on screen; the mode remains open and escapable while the next track loads.

## Ownership and lifecycle

- `:ui:phone` owns the three-row selection and Compose rendering; it receives presentation facts and emits exit/interaction callbacks.
- `PhoneRuntimeHost` owns saved local mode visibility. The selected destination stays Lyrics, preserving the existing normal/Karaoke tick policy. Exit restores normal Follow behavior. Configuration recreation can restore the mode; no preference or application storage key is added.
- `:app` owns `PerformanceWindowEffect`. While resumed in fullscreen it hides status/navigation bars with swipe-to-reveal behavior and keeps the screen awake. Pause, disposal, Activity replacement and exit restore the captured window flags, system-bar visibility and behavior. The normal-window snapshot is retained for the whole presentation so stale hidden insets on resume cannot become the exit state. Resuming reapplies the fullscreen effects for that foreground interval.
- Reset explicitly clears local mode visibility before re-evaluating onboarding. This is transient navigation cleanup, not an expansion of durable reset state or storage policy.
- Providers, cache, Translation execution, timing semantics, MediaSession selection and Android Auto retain their existing ownership.

## Legacy reference

Current `whoxamxl/auto-lyrics:main` was inspected at `8484bed2dbe8db5ca7b17dec5481b3c22714dc6f`: `PerformanceActivity`, `PerformanceLyricsView`, `activity_performance.xml`, MainActivity entry and manifest. There are no dedicated Performance tests in that baseline.

Preserved behavior: immersive viewing, foreground screen-awake behavior, strong current-line hierarchy and adjacent-line context. The rendering is rewritten for Compose and the existing AALyrics presentation seams. Legacy `MediaTracker`, independent playback-position calculations, active-word size pulses, artwork color extraction and tap-anywhere exit are not migrated.

The owner's additional background reference is `GitUpGitUp/auto-lyrics`: `activity_performance.xml` blob `262db2c9fc0d97ab1536d6e8fd425fc149418d5f` uses a center-cropped album image at alpha 0.15. This version retains that visual idea with bounded blur, contrast scrims and a short transition; it does not import a legacy playback pipeline.

## Validation and device handoff

Focused JVM tests cover intro, first/middle/last rows, invalid indices, blank intervals, backward seek/Browse independence, Translation row identity, PLAIN fallback, Karaoke range validation, lifecycle acquisition/restoration, measured geometry under font/translation pressure, bounded blur behavior/source preservation, and space-preferred wrapping with preserved WORD offsets.

Production-component Previews cover portrait/landscape, dark/white/busy/missing synthetic artwork, current Translation, WORD sweep, intro/end, long lines with enlarged fonts, space-delimited Chinese phrases and WORD sweeps crossing their breaks, PLAIN, loading/no-media/not-found/failure, and interactive entry/exit/artwork replacement. These are deterministic debug fixtures, not alternative production renderers. Conversation mockups illustrate the design but do not substitute for Android rendering/device verification.

Device checks should verify:

1. Enter from Lyrics and exit by both the button and system Back; the next Back follows normal shell navigation.
2. Rotate in fullscreen; inspect cutouts, gesture/button navigation and temporary system bars.
3. Background and return; confirm system bars and screen-awake flags are released and restored appropriately.
4. Exercise live WORD Karaoke and Translation, pause, seek backward, switch tracks and lose/recover the source.
5. Read long original/translated rows at large font scale, including hidden neighbors and scrolling an oversized current block.
6. Open PLAIN lyrics with and without playback duration, browse manually, then exit to normal Follow.
7. Switch among bright/dark/busy covers and missing artwork; inspect portrait/landscape cropping, text contrast and the transition on both older and newer Android versions.

Branch-specific build/test and device evidence belongs in `TASK.md`.
