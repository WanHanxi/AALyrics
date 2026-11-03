package io.github.whoxamxl.aalyrics.ui.phone.lyrics

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
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
    artwork: ImageBitmap? = null,
    onViewportInteractionModeChange: (LyricsViewportInteractionMode) -> Unit = {},
) {
    BackHandler(onBack = onExit)
    Surface(modifier = modifier.fillMaxSize(), color = AALyricsColors.BackgroundBase) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val landscape = maxWidth > maxHeight
            PerformanceArtworkBackground(artwork, Modifier.matchParentSize())
            Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                PerformanceHeader(state.trackCard, onExit)
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
                        landscape = landscape,
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
                state.trackCard.providerLabel?.takeIf(String::isNotBlank)?.let {
                    Text(
                        text = it,
                        style = AALyricsTypography.Label,
                        color = AALyricsColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceHeader(track: TrackCardUiState, onExit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
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
    landscape: Boolean,
    modifier: Modifier = Modifier,
) {
    val currentStyle = AALyricsTypography.LyricsCurrent.copy(
        fontSize = if (landscape) 60.sp else 48.sp,
        lineHeight = if (landscape) 70.sp else 56.sp,
        lineBreak = LineBreak.Heading,
    )
    val neighborStyle = AALyricsTypography.LyricsSupporting.copy(
        fontSize = if (landscape) 28.sp else 26.sp,
        lineHeight = if (landscape) 34.sp else 32.sp,
        lineBreak = LineBreak.Heading,
    )
    val translationStyle = AALyricsTypography.LyricsSupporting.copy(
        fontSize = if (landscape) 22.sp else 20.sp,
        lineHeight = if (landscape) 28.sp else 26.sp,
    )
    Box(
        modifier.fillMaxWidth().padding(horizontal = if (landscape) 40.dp else 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Each row is measured once at its actual wrapped size. Only the current block scrolls;
        // neighbors that do not fit are left unplaced (and absent from accessibility traversal).
        Layout(
            modifier = Modifier.widthIn(max = 960.dp).fillMaxSize(),
            content = {
                Box {
                    frame.previous?.let { PerformanceLine(it, neighborStyle, current = false) }
                }
                key(frame.currentIndex, frame.current?.text) {
                    Column(
                        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PerformanceLine(frame.current, currentStyle, current = true, karaoke = karaoke)
                        frame.current?.translatedText?.takeIf(String::isNotBlank)?.let {
                            PerformanceText(
                                text = it,
                                style = translationStyle,
                                color = AALyricsColors.TextSecondary,
                            )
                        }
                    }
                }
                Box {
                    frame.next?.let { PerformanceLine(it, neighborStyle, current = false) }
                }
            },
        ) { measurables, constraints ->
            val rowConstraints = Constraints(maxWidth = constraints.maxWidth)
            val previous = measurables[0].measure(rowConstraints)
            val current = measurables[1].measure(rowConstraints.copy(maxHeight = constraints.maxHeight))
            val next = measurables[2].measure(rowConstraints)
            val placement = performanceLyricsPlacement(
                height = constraints.maxHeight,
                previousHeight = previous.height,
                currentHeight = current.height,
                nextHeight = next.height,
                gap = (if (landscape) 16.dp else 24.dp).roundToPx(),
            )
            layout(constraints.maxWidth, constraints.maxHeight) {
                placement.previousTop?.let { previous.placeRelative(0, it) }
                current.placeRelative(0, placement.currentTop)
                placement.nextTop?.let { next.placeRelative(0, it) }
            }
        }
    }
}

@Composable
private fun PerformanceLine(
    line: LyricsViewportLineUiState?,
    style: TextStyle,
    current: Boolean,
    karaoke: KaraokeLineUiState? = null,
) {
    val instrumental = line?.text.isNullOrBlank()
    val text = if (instrumental) "♪" else line.text
    val safeKaraoke = if (current && !instrumental) validKaraokeLine(text, karaoke) else null
    val noteDescription = stringResource(R.string.performance_instrumental)
    PerformanceText(
        text = text,
        style = style,
        color = if (current) AALyricsColors.TextPrimary else AALyricsColors.TextSecondary,
        karaoke = safeKaraoke,
        modifier = if (instrumental) Modifier.semantics { contentDescription = noteDescription } else Modifier,
    )
}

@Composable
private fun PerformanceText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    karaoke: KaraokeLineUiState? = null,
) {
    val textMeasurer = rememberTextMeasurer()
    val resolvedStyle = LocalTextStyle.current.merge(style)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width = constraints.maxWidth
        // Recompute for text/width/font changes, never for progress-only Karaoke updates.
        val displayText = remember(text, resolvedStyle, width, textMeasurer) {
            performanceWrappedText(text) { candidate ->
                textMeasurer.measure(
                    text = candidate,
                    style = resolvedStyle,
                    softWrap = false,
                ).multiParagraph.maxIntrinsicWidth <= width
            }
        }
        if (karaoke != null) {
            KaraokeLineText(displayText, karaoke, resolvedStyle, textAlign = TextAlign.Center)
        } else {
            Text(
                text = displayText,
                style = resolvedStyle,
                color = color,
                textAlign = TextAlign.Center,
                modifier = modifier.fillMaxWidth(),
            )
        }
    }
}
