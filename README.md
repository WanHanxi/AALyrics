<p align="center">
  <img src="docs/branding/aalyrics-readme-icon-rounded.svg" alt="AALyrics" width="112" />
</p>

<h1 align="center">AALyrics</h1>

<p align="center">
  <strong>Synchronized lyrics for Android and Android Auto.</strong>
</p>

<p align="center">
  Works with Spotify, YouTube Music, Apple Music and other Android MediaSession players.
</p>

<p align="center">
  <a href="https://www.android.com/"><img src="https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android 8.0+" /></a>
  <a href="https://github.com/WanHanxi/AALyrics/releases/latest"><img src="https://img.shields.io/github/v/release/WanHanxi/AALyrics?label=AALyrics&style=for-the-badge&color=43DFF5" alt="Latest stable AALyrics release" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-PolyForm_Noncommercial-59636E?style=for-the-badge" alt="PolyForm Noncommercial" /></a>
</p>

<p align="center">
  <a href="https://buymeacoffee.com/wancy"><img src="https://img.shields.io/badge/Support_This_Fork-WanHanxi-FFDD00?style=for-the-badge&logo=buymeacoffee&logoColor=000000" height="40" alt="Support WanHanxi, this fork's maintainer, on Buy Me a Coffee" /></a>
  <a href="https://buymeacoffee.com/whoxamxi"><img src="https://img.shields.io/badge/Support_Original_Author-Yuta_Miura-FFDD00?style=for-the-badge&logo=buymeacoffee&logoColor=000000" height="40" alt="Support Yuta Miura, the original author, on Buy Me a Coffee" /></a>
  <a href="https://github.com/WanHanxi/AALyrics/releases"><img src="https://img.shields.io/badge/Download_APK-GitHub_Releases-181717?style=for-the-badge&logo=github&logoColor=white" height="40" alt="Download AALyrics APK from GitHub Releases" /></a>
</p>

### Android Auto experience

AALyrics brings the active lyric context into Android Auto Now Playing and stays usable in split view alongside navigation.

<div align="center">
  <img src="docs/screenshots/android-auto-now-playing-vcrop.webp" width="720" alt="AALyrics Android Auto Now Playing with synchronized lyrics" />
</div>

<p align="center"><sub>Now Playing with the active synchronized lyric, artwork, seek state, and available transport controls.</sub></p>

<div align="center">
  <img src="docs/screenshots/android-auto-split-view-vcrop.webp" width="720" alt="AALyrics Android Auto split view alongside navigation" />
</div>

<p align="center"><sub>AALyrics in Android Auto split view alongside navigation.</sub></p>

### Phone experience

<div align="center">
  <img src="docs/screenshots/phone-lyrics-cropped.webp" width="31%" alt="AALyrics synchronized lyrics screen" />
  <img src="docs/screenshots/phone-expanded-player-cropped.webp" width="31%" alt="AALyrics expanded player" />
  <img src="docs/screenshots/phone-queue-cropped.webp" width="31%" alt="AALyrics playback queue" />
</div>

AALyrics observes the active Android media session, matches the current track against multiple lyrics providers, and presents the best available lyrics in real time. The core experience is already functional on Phone and Android Auto, with on-device Translation, experimental word-synced Karaoke, playback controls, and signed in-app updates built around it.

## Highlights

- **Synchronized lyrics** — plain, line-synced, and genuine word-synced lyrics when the selected provider supplies them.
- **Android Auto Now Playing** — current lyric line, artwork, translated line when eligible, and the transport controls exposed by the active media app.
- **Offline lyrics reuse** — previously resolved lyrics are cached locally for track switches and app restarts, subject to storage limits.
- **Multiple lyrics providers** — LRCLIB, Musixmatch, and SyncLRC with provider-independent candidate selection. NetEase Cloud Music is optional, off by default, and can be enabled after a source notice in **Settings > Lyrics**. PetitLyrics is an optional build-time source, enabled only with complete [configuration](docs/providers/PETITLYRICS.md#configuration-source).
- **Paired Chinese translations** — when NetEase is enabled, prefer matching originals and supplied translations; on-device machine translation remains an optional fallback, enabled by default. Source and translation rights remain separate from the app license; see [source rights and references](docs/LYRICS_SOURCE_RIGHTS.md).
- **Experimental WORD_SYNC Karaoke** — opt-in Phone presentation for genuine word-timed lyrics.
- **Fullscreen Phone lyrics** — centered lyrics and translations over a softened album-art background, with portrait and landscape support.
- **Lyrics calibration** — three-tap sync, fine adjustments and exact offset entry shared with Android Auto.
- **Phone playback surface** — play/pause, previous/next, seek, relative seek, and Queue support where the active media app exposes them.
- **Built-in updates** — signed GitHub Release APK discovery, SHA-256 verification, package/signing preflight, and Android PackageInstaller handoff.

This personal fork is maintained by [WanHanxi](https://github.com/WanHanxi/AALyrics), based on [the original AALyrics project by Yuta Miura (`whoxamxl`)](https://github.com/whoxamxl/AALyrics). See [GitHub Releases](https://github.com/WanHanxi/AALyrics/releases/latest) for the latest published stable version and [release setup and validation](docs/RELEASES.md) for the distribution process. The original license and required copyright notice are preserved. The donation links below let you support this fork's maintainer or the original author separately.

## Get AALyrics

AALyrics requires **Android 8.0 (API 26) or later** and is distributed outside Google Play as a **signed APK through GitHub Releases**.

**[Open GitHub Releases →](https://github.com/WanHanxi/AALyrics/releases)**

1. Download the current signed `AALyrics-vX.Y.Z[-channel.N].apk`.
2. Install the APK. Android may ask you to allow the browser or file manager to install unknown apps.
3. Open AALyrics.
4. Enable **Notification access** for AALyrics. This is required so AALyrics can identify and follow the active Android media session.

Each Release also includes a matching `.sha256` file for APK integrity verification.

After the first release-signed installation, later AALyrics releases signed with the same key can update the installed app normally. AALyrics can also discover, verify, and hand off newer releases through its in-app update flow.

**Upgrading to personal 1.2.0:** install over the existing personal 1.1.0 or 1.1.0-beta.1 Release app when the new version is published, or use its in-app update check. The package and signing identity are unchanged, so no uninstall is needed and settings are retained. Older lyrics cache entries cannot be reused: load songs online again before relying on them offline, even if NetEase stays off.

> [!NOTE]
> The personal app is labeled **AALyrics** (or **AALyrics Debug**) and can coexist with upstream. Install it as a new app: original-app settings, cached lyrics and Android permissions do not transfer. Enable Notification access and select the new app in Android Auto. Future personal Release builds use the same personal signing key and can update this installation.

## Android Auto

AALyrics currently uses the sideloaded Android Auto media compatibility path and focuses on a clean **Now Playing** lyrics experience.

To enable it:

1. Install and open AALyrics on the phone and enable Notification access.
2. Enable **Android Auto developer mode**.
3. Open Android Auto **Developer settings** and enable **Unknown sources**.
4. Reconnect Android Auto if needed.
5. Make sure AALyrics is enabled in the Android Auto launcher/customize list.

The host receives the active track, artwork, supported playback actions, current synchronized lyric line, and an eligible translated line when available. Android Auto controls the final layout and text truncation.

The development branch also automatically prepares the lyrics media session when Android
Auto is connected and an eligible song is playing. This does not set the default media
app. See the [activation behavior and phone/car test guide](docs/ANDROID_AUTO_ACTIVATION.md).

## What's new in 1.2.0

AALyrics 1.2.0 adds optional NetEase Cloud Music originals and Chinese translations. Enable **Settings > Lyrics > NetEase Cloud Music** after reading the source notice, then enable Translation and choose Chinese. NetEase defaults off; ordinary upgrades preserve an existing choice, while Reset AALyrics restores it to off.

Matching supplied translations need no downloaded translation model and are cached with their original lyrics. **Settings > Translation > Machine translation fallback** remains on by default and can be disabled. Missing rows inside a supplied translation are left original-only. NetEase currently supplies line timing, so a preferred bilingual match may replace another source's word-timed lyrics.

Some advanced features retain their existing scope:

- Manual Sync calibration is available in this fork: three-tap calibration, ±50/500 ms, exact millisecond entry, live preview, and a saved global offset shared by Phone and Android Auto. Per-song and automatic calibration remain future work.
- Phone WORD_SYNC Karaoke remains experimental and opt-in.
- Android Auto is currently line-oriented Now Playing rather than full Phone feature parity or Karaoke presentation.

Lyric availability depends on the active media app and providers. Device-specific notification-listener behavior can still require toggling Notification access; see the [Changelog](CHANGELOG.md) for current limitations.

## Lyrics, Translation & Karaoke

AALyrics can consume plain, line-synchronized, and genuine word-synchronized lyrics depending on what the selected provider returns.

Phone lyrics automatically follow playback while still allowing manual browsing. Shared timing semantics keep Phone and Android Auto aligned to the same canonical lyric document. Genuine word-synchronized data can additionally drive the experimental Phone Karaoke presentation.

Translation prefers a matching source-supplied translation when available in the selected language; otherwise it can use on-device language models where supported and machine fallback is enabled. Translated text is secondary presentation only: it remains tied to the identity and timing of the canonical lyrics.

Actual lyric availability and timing quality depend on the metadata exposed by the active media app and on matching lyric data available from the providers.

### Manual lyrics sync (this fork)

While a song with timed lyrics is playing, open **Sync → Start 3-tap sync**. The app shows three upcoming lyric lines and highlights the first one. Tap **Tap at line start** when you hear the highlighted line begin; repeat for the next two lines. After three taps, their average offset is saved automatically. **Cancel calibration** keeps the old offset. Pause, a track/lyrics change, a detected seek, leaving Sync, or manual adjustment ends an unfinished calibration; start again when ready. If fewer than three lines remain, seek to an earlier part of the song first.

While playing a song with timed lyrics, open **Sync**. If lyrics are late, use **Advance (+50/+500 ms)**; if they are early, use **Delay (−50/−500 ms)**. The four compact controls are ordered **−500 / −50 / +50 / +500 ms**, with Delay and Advance labels. The live preview follows the same timing as the Lyrics page. **Reset** beside the current value restores the original timing.

For an exact value, tap the number under **LYRICS OFFSET** (or its pencil icon). Enter a whole number of milliseconds, such as `125` to advance or `-275` to delay. Use **±** if your keyboard has no minus key. **Save** or keyboard **Done** applies it; **Cancel**, Back, or dismissing the dialog keeps the previous value. Opening the editor cancels unfinished three-tap calibration. Exact entry is available even without timed lyrics.

The offset is global, saved across songs and app restarts, and shared with Android Auto. It is limited to ±30 seconds and never seeks the music or rewrites source timestamps. Plain lyrics cannot be calibrated. **Settings → Advanced → Reset AALyrics** also clears the saved offset.

## Voluntary support

Choose whom you would like to support through Buy Me a Coffee:

- **Support this fork:** [WanHanxi](https://buymeacoffee.com/wancy), for maintenance and development of this personal fork.
- **Support the original author:** [Yuta Miura (`whoxamxl`)](https://buymeacoffee.com/whoxamxi), for the original AALyrics project.

Contributions go to the selected recipient. Support is optional and does not unlock features or guarantee technical support.

## Development

AALyrics is an Android project built with Gradle. For a local debug installation:

```bash
./gradlew :app:installDebug
```

CI uses JDK 17. Local/CI debug APKs are development artifacts and are not interchangeable with the signed GitHub Release channel.

<details>
<summary><strong>Project structure & developer documentation</strong></summary>

```text
app                    Android application / composition root
core:model             Shared domain models
core:lyrics            Lyrics lifecycle and orchestration
core:timing            Shared synchronized-lyrics timing semantics
provider:api           Provider contracts
provider:matching      Provider-neutral matching semantics
provider:lrc           Shared LRC parsing
provider:selection     Cross-provider candidate selection
provider:lrclib        LRCLIB integration
provider:petitlyrics   PetitLyrics integration
provider:musixmatch    Musixmatch integration
provider:synclrc       SyncLRC integration
platform:media         Android media-session / playback integration
ui:designsystem        Shared UI tokens and components
ui:phone               Phone presentation
ui:automotive          Android Auto presentation
```

Start with:

- [Personal fork workflow](docs/FORK_WORKFLOW.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Lyrics pipeline](docs/LYRICS_PIPELINE_ARCHITECTURE.md)
- [Timing architecture](docs/TIMING_ARCHITECTURE.md)
- [Translation architecture](docs/TRANSLATION_ARCHITECTURE.md)
- [Android Auto Now Playing](docs/ANDROID_AUTO_NOW_PLAYING.md)
- [Branding](docs/BRANDING.md)
- [Release policy](docs/RELEASES.md)
- [Roadmap](docs/ROADMAP.md)

Release package: `io.github.wanhanxi.aalyrics`

Debug package: `io.github.wanhanxi.aalyrics.debug`; source namespace: `io.github.whoxamxl.aalyrics`.

</details>

## License

AALyrics is **source-available**, not Open Source Initiative (OSI) open source.

The project is licensed under the **PolyForm Noncommercial License 1.0.0**. Personal and other noncommercial uses are permitted under its terms. Commercial use requires separate permission from the copyright holder.

See [LICENSE](LICENSE) for the complete terms.
