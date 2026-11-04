## Unreleased

### Changed

- Send this fork's bug reports, questions, ideas, discussion and private security reports to `WanHanxi/AALyrics`; identify the fork in LRCLIB request headers.
- Identify WanHanxi as the personal fork maintainer in Settings, alongside a separate link and copyright credit for the original author, Yuta Miura (`whoxamxl`).
- Clarify that the existing Buy Me a Coffee links support the original author, and align the bundled Terms of Use and Privacy Policy with the fork's maintenance and donation attribution.
- Add a separate **Support this fork** destination for WanHanxi alongside **Support the original author**, with both recipients identified in the app and repository donation links.

## [1.1.0-beta.1] - 2026-11-03

This first personal-fork beta adds a fullscreen lyrics view with album artwork, tools to align lyrics with what you hear, and offline reuse of previously loaded lyrics. Playback connections now have bounded recovery attempts and a clear way to retry when Android leaves the listener disconnected.

### Installation notes

This is the **WanHanxi personal fork**, based on upstream AALyrics 1.0 Beta 1. It installs as **AALyrics**, alongside the original app, and checks for updates in `WanHanxi/AALyrics`.

The new package has its own signing identity and storage. It does not replace the original app or earlier Debug builds, and their settings and cached lyrics are not copied. Enable Notification access for the new app; Android Auto users should also select **AALyrics** in the launcher settings.

This release uses **LRCLIB, Musixmatch, and SyncLRC**. PetitLyrics is optional and disabled in this build, so songs available only from that source will not be found.

### New features

- **Fullscreen lyrics:** a centered current line and translation, previous/next context, and a softened album-art background. Portrait and landscape layouts support long lyrics, large text and word-synced Karaoke; lines prefer existing spaces when wrapping.
- **Lyrics calibration:** tap along with three lyric starts to save an average offset, adjust by 50 or 500 ms, or enter an exact signed millisecond value. A live preview helps fine-tune the result. The saved offset is shared by Phone and Android Auto; Reset AALyrics restores it to zero.
- **Offline lyrics:** previously resolved original lyrics, their timing and provider attribution survive app restarts and can be reused without another provider request. Storage is bounded, and Reset AALyrics preserves the cache.

### Improvements and bug fixes

- Recover missing media-listener connections with a limited retry sequence while Phone or Android Auto needs lyrics. A failed attempt now shows a connection dialog instead of leaving an endless Connecting state.
- On Android 14 and later, manual Reconnect can reset a stalled listener binding before retrying. Notification access settings remain available when Android still does not reconnect.
- Separate personal Release and Debug installations from upstream, with independent package IDs and a personal signed-update channel.
- Make PetitLyrics optional at build time: builds and releases work without its credentials; supplying all four configuration values enables it.

### Development builds

- Debug builds record bounded connection diagnostics and can export a report from Settings > Advanced. Reports remain local unless shared by the user; this recorder/export action is not included in Release builds.

### Known limitations

- This is a Beta release. Device-specific notification-listener behavior can still require toggling Notification access in Android settings.
- Calibration is global; per-song and automatic calibration are not available.
- Offline reuse requires a matching song already in this app's cache. Translations are not stored in that cache, and there is no cache-management screen yet.
- Phone word-synced Karaoke remains experimental, defaults off, and requires genuine word timing from the provider.
- Android Auto remains focused on line-oriented Now Playing; Phone fullscreen presentation and Karaoke are not available there.

## [1.0.0-beta.1] - 2026-09-26

### Beta milestone

- AALyrics now presents its core product experience as a 1.0 Beta: synchronized lyrics on Phone and Android Auto, production lyric-provider selection, Phone Translation, experimental WORD_SYNC Karaoke, playback controls, and signed in-app updates.
- The primary lyrics experience is functional; remaining work is concentrated in advanced calibration, experimental Karaoke refinement, and broader Android Auto feature parity rather than initial product construction.

### Added

- Completed the current Android Auto Now Playing experience with artwork, supported transport actions, line-oriented synchronized lyrics, eligible translated lines, and playback/lyrics demand recovery when the Phone UI is not active.
- Added bounded cold-start lyrics lookup recovery and user-visible lyrics lookup diagnostics for transient media-session startup failures.

### Changed

- Phone lyrics rendering now lazily composes visible/near-visible rows instead of eagerly composing the full lyrics document, reducing presentation work for long lyrics while preserving Follow/Browse behavior, Translation rows, and Karaoke presentation.
- Android Auto runtime handling is more resilient to process/lifecycle and media-session listener rebinding while the automotive host remains active.
- Provider timing normalization preserves additional explicit end-timing evidence for Enhanced LRC and Musixmatch RichSync.

### Fixed

- Fixed open-ended final Karaoke sweeps that could stretch a final word across an inter-line pause when explicit provider timing or defensible local cadence was available.
- Fixed lazy Phone lyrics edge cases around PLAIN auto-scroll extent, Browse ownership/re-arm behavior, and off-screen playback geometry.
- Fixed Android Auto lyrics demand paths that could stop resolving lyrics after the Phone UI was closed or the media-session listener needed to recover.

### Known limitations

- Sync calibration controls and persistence are still in development.
- Phone WORD_SYNC Karaoke remains experimental, defaults OFF, and requires genuine word-synchronized lyrics.
- Android Auto remains focused on line-oriented Now Playing presentation; Karaoke and broader Phone feature parity are not yet implemented.

## [0.2.0-alpha.3] - 2026-09-26

### Added

- End-to-end Phone Translation presentation with translated lyric rows, Track Card status/retry feedback, and Translation diagnostics/model inventory in Details.
- Shared framework-neutral timing semantics for synchronized lyrics, including line, word, progress, and boundary projection.
- Experimental Phone Karaoke for genuine WORD_SYNC lyrics behind the Advanced feature gate and Expanded Player Quick-controls toggle.

### Changed

- Translation language profiling and model routing are more conservative, with unsupported routes rejected before execution.
- Phone Lyrics, Details, and the Playback Surface now share one stable projected playback clock when MediaSession source timing is unavailable or inconsistent.
- Karaoke presentation preserves canonical lyric text/timestamps while applying display-only grouping, boundary states, and final-word completion behavior.
- MediaSession track transitions keep playback identity and timeline coherent during metadata stabilization.

### Fixed

- Fixed playback/lyrics timing that could jump ahead or lag after mid-track app attachment, pause/resume, source timestamp inconsistencies, or track transitions.
- Fixed Karaoke sweep behavior around BEFORE_FIRST/GAP/AFTER_LAST boundaries, open-ended final words, interludes, line-wide pseudo-word timing, multi-word timing ranges, and bidirectional text.
- Fixed Translation secondary-language activation and unsupported-model routing cases that could produce incorrect or unavailable translation paths.

### Known limitations

- Sync timing / calibration controls and persistence are still in development.
- Phone Karaoke remains experimental, defaults OFF, and requires genuine WORD_SYNC lyrics.
- Android Auto Karaoke and broader Android Auto feature parity remain planned work.

## [0.2.0-alpha.2] - 2026-09-24

### Added

- Complete in-app update flow with manual and automatic update discovery, signed APK download, SHA-256 verification, Android install-source permission guidance, PackageInstaller handoff, retry/recovery, and post-update success feedback.
- In-app Changelog, Privacy Policy, Terms of Use, Help & Feedback, and Support surfaces under Settings.
- Playback source identity, status, app metadata, eligibility handling, and non-audio/unclassified source controls.
- Storage/reset controls for app-owned state and downloaded Translation models.
- Shared semantic VersionChip presentation for installed and available AALyrics versions.

### Changed

- Refined the Phone lyrics viewport, current-line focus motion, vertical positioning, and metadata marquee behavior.
- Refined Playback Surface behavior, queue artwork handling, playback-app routing, and media-session selection.
- Unified the complete post-Update process in a dedicated update dialog, with normal updates automatically continuing from verified download into the existing install stage.
- Improved Settings navigation, Back behavior, nested-screen return behavior, and update-process re-entry.
- Strengthened update recovery across process recreation, installer-session interruption, package replacement, and retained verified APK reuse.
- Development builds now derive their version label from the latest reachable release tag plus the current Git commit.

### Fixed

- Fixed Phone system Back paths that could exit the app before returning through the main Lyrics surface.
- Fixed stale update/install presentation and recovery paths discovered during device validation and review.
- Fixed playback-source and retained-lyrics regressions encountered during Phone UI refinement.

### Known limitations

- Translation is still under active development; the end-to-end translated-lyrics presentation is not yet complete.
- Sync timing / calibration workflow is still in development.
- Karaoke mode remains unavailable / experimental-only.
- Android Auto does not yet have full Phone UI feature parity; further Now Playing / split-view work remains planned.

## [0.2.0-alpha.1] - 2026-09-21

### Added

- Production Phone UI hosted directly in the application with live playback, lyrics, artwork, Settings, Details, and Translation state.
- Persistent collapsed and expanded Playback Surface with transport controls, direct seek, relative seek, queue handling, and playback-app fallback.
- Production Settings and Details surfaces, including Advanced / Verbose Details.
- In-app Markdown-rendered License.
- Notification Access and Android Auto compatibility onboarding.

### Changed

- Refined responsive lyrics follow/browse behavior and Phone layout across narrow and normal device widths.
- Refined track identity overflow with row-aware marquee behavior, manual inspection, and fixed ellipsis in the collapsed Playback Bar.
- Refined Playback Surface expansion/collapse into one finger-following transformation model.

### Known limitations

- Sync timing / calibration workflow is still in development.
- Karaoke mode is not yet available.
- In-app update and Changelog runtime are not yet available in this release.

## [0.1.0-alpha.1] - 2026-09-19

### Added

- Initial AALyrics application, core lyrics domain, provider-selection architecture, and race-safe lyrics lifecycle.
- LRCLIB, PetitLyrics, Musixmatch, and SyncLRC provider integrations.
- Live MediaSession playback runtime and Android Auto Now Playing support.
- Initial Compose Phone shell, Track Card, design system, and responsive UI foundation.
- Signed GitHub Release distribution with APK and SHA-256 checksum assets.

### Development status

- This is the first signed alpha snapshot and establishes the foundation for later Phone UI, Settings, Translation, and playback work.
