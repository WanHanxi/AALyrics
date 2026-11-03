# Phone fullscreen Performance mode

## Product contract

The Lyrics Track Card's fullscreen action opens a Phone-local Performance surface. The owner selected the previous/current/next presentation on 2026-10-31. The normal Lyrics destination keeps its responsive scrolling document.

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

The current line includes its already identity-gated translated text when available. Neighbor slots show canonical text. Text wraps without ellipsis or font-size reduction; each slot can scroll when a long logical line or large system font exceeds its available height. Scroll position resets when its displayed line changes. Short windows reduce spacing while preserving font scaling and the exit control.

The surface selects at most three existing rows. It does not copy or remap the entire lyrics document on the playback tick. Seeks, track changes, pauses, and the global timing offset are reflected through the existing app-owned Phone mapping.

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

## Validation and device handoff

Focused JVM tests cover intro, first/middle/last rows, invalid indices, blank intervals, backward seek/Browse independence, Translation row identity, PLAIN fallback, Karaoke range validation, and lifecycle acquisition/restoration.

Production-component Previews cover portrait/landscape, current Translation, WORD sweep, intro/end, long lines with enlarged fonts, PLAIN, loading/no-media/not-found/failure, and interactive entry/exit. These are deterministic debug fixtures, not alternative production renderers.

Device checks should verify:

1. Enter from Lyrics and exit by both the button and system Back; the next Back follows normal shell navigation.
2. Rotate in fullscreen; inspect cutouts, gesture/button navigation and temporary system bars.
3. Background and return; confirm system bars and screen-awake flags are released and restored appropriately.
4. Exercise live WORD Karaoke and Translation, pause, seek backward, switch tracks and lose/recover the source.
5. Read long original/translated rows at large font scale, including scrolling an oversized current slot.
6. Open PLAIN lyrics with and without playback duration, browse manually, then exit to normal Follow.

Branch-specific build/test and device evidence belongs in `TASK.md`.
