package io.github.whoxamxl.aalyrics.ui.phone.lyrics

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.whoxamxl.aalyrics.core.model.LyricsSyncType
import io.github.whoxamxl.aalyrics.ui.designsystem.icon.AALyricsIcons
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsColors
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsTypography
import io.github.whoxamxl.aalyrics.ui.phone.R

/** Phone-only presentation. The app owns immersive window effects and all timing facts. */
@Composable
fun PerformanceLyricsScreen(
    state: LyricsScreenUiState,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    artwork: (@Composable BoxScope.() -> Unit)? = null,
    onViewportInteractionModeChange: (LyricsViewportInteractionMode) -> Unit = {},
) {
    BackHandler(onBack = onExit)
    Surface(modifier = modifier.fillMaxSize(), color = AALyricsColors.BackgroundBase) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            PerformanceHeader(state.trackCard, onExit, artwork)
            val viewport = state.viewport
            val frame = performanceLyricsFrame(viewport)
            when {
                state.trackCard.lyricsStatus != TrackCardLyricsStatus.READY ||
                    viewport.lines.isEmpty() -> {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(
                                when (state.trackCard.lyricsStatus) {
                                    TrackCardLyricsStatus.IDLE -> R.string.performance_no_media
                                    TrackCardLyricsStatus.LOADING -> R.string.details_lyrics_loading
                                    TrackCardLyricsStatus.FAILED -> R.string.details_lyrics_failed
                                    else -> R.string.details_lyrics_not_found
                                },
                            ),
                            modifier = Modifier.padding(24.dp),
                            style = AALyricsTypography.LyricsSupporting,
                            color = AALyricsColors.TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                frame != null -> PerformanceTimedLyrics(
                    frame = frame,
                    karaoke = viewport.karaokeLine.takeIf { viewport.syncType == LyricsSyncType.WORD },
                    modifier = Modifier.weight(1f),
                )

                else -> {
                    Text(
                        text = stringResource(R.string.performance_plain),
                        style = AALyricsTypography.Label,
                        color = AALyricsColors.TextSecondary,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    )
                    LyricsViewport(
                        state = viewport,
                        modifier = Modifier.weight(1f),
                        onInteractionModeChange = onViewportInteractionModeChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceHeader(
    track: TrackCardUiState,
    onExit: () -> Unit,
    artwork: (@Composable BoxScope.() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (artwork != null) {
            Box(Modifier.size(40.dp), content = artwork)
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = track.title.ifBlank { stringResource(R.string.performance_title) },
                style = AALyricsTypography.AppTitle,
                color = AALyricsColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            track.artist?.takeIf(String::isNotBlank)?.let {
                Text(
                    text = it,
                    style = AALyricsTypography.TrackArtist,
                    color = AALyricsColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            track.providerLabel?.takeIf(String::isNotBlank)?.let {
                Text(
                    text = it,
                    style = AALyricsTypography.Label,
                    color = AALyricsColors.AccentCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        IconButton(onClick = onExit) {
            Icon(
                imageVector = AALyricsIcons.ExitFullscreen,
                contentDescription = stringResource(R.string.performance_exit),
                tint = AALyricsColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun PerformanceTimedLyrics(
    frame: PerformanceLyricsFrame,
    karaoke: KaraokeLineUiState?,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        // Keep system font scaling. Short windows reduce spacing, never truncate the current line.
        val compact = maxHeight < 320.dp
        val spacing = if (compact) 8.dp else 20.dp
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = spacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            PerformanceLineSlot(
                line = frame.previous,
                current = false,
                modifier = Modifier.weight(1f),
            )
            key(frame.currentIndex, frame.current?.text) {
                PerformanceLineSlot(
                    line = frame.current,
                    current = true,
                    karaoke = karaoke.takeIf { frame.current != null },
                    modifier = Modifier.weight(2.5f),
                )
            }
            PerformanceLineSlot(
                line = frame.next,
                current = false,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PerformanceLineSlot(
    line: LyricsViewportLineUiState?,
    current: Boolean,
    modifier: Modifier = Modifier,
    karaoke: KaraokeLineUiState? = null,
) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        if (!current && line == null) return@Box
        key(line?.text) {
            Column(
                modifier = Modifier.widthIn(max = 880.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val instrumental = line?.text.isNullOrBlank()
                val text = if (instrumental) "♪" else line!!.text
                val style = if (current) {
                    AALyricsTypography.LyricsCurrent.copy(fontSize = 36.sp, lineHeight = 46.sp)
                } else {
                    AALyricsTypography.LyricsSupporting
                }
                val safeKaraoke = if (current && !instrumental) validKaraokeLine(text, karaoke) else null
                if (safeKaraoke != null) {
                    KaraokeLineText(text, safeKaraoke, style, textAlign = TextAlign.Center)
                } else {
                    val noteDescription = stringResource(R.string.performance_instrumental)
                    Text(
                        text = text,
                        modifier = Modifier.fillMaxWidth().then(
                            if (instrumental) Modifier.semantics { contentDescription = noteDescription }
                            else Modifier,
                        ),
                        style = style,
                        color = if (current) AALyricsColors.TextPrimary
                            else AALyricsColors.TextSecondary.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                    )
                }
                if (current) {
                    line?.translatedText?.takeIf(String::isNotBlank)?.let {
                        Text(
                            text = it,
                            style = AALyricsTypography.LyricsSupporting,
                            color = AALyricsColors.TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
