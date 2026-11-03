package io.github.whoxamxl.aalyrics.ui.phone.lyrics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsColors

/** Shared continuous sweep renderer for the scrolling and fullscreen Phone surfaces. */
@Composable
internal fun KaraokeLineText(
    text: String,
    karaoke: KaraokeLineUiState,
    style: androidx.compose.ui.text.TextStyle,
    textAlign: TextAlign = TextAlign.Start,
) {
    val primary = AALyricsColors.TextPrimary
    val secondary = AALyricsColors.TextSecondary.copy(alpha = 0.72f)
    val base = buildAnnotatedString {
        withStyle(SpanStyle(color = primary)) { append(text, 0, karaoke.completedEnd) }
        withStyle(SpanStyle(color = secondary)) { append(text, karaoke.completedEnd, text.length) }
    }
    val sweep = karaoke.sweep
    var layout by remember(text) { mutableStateOf<TextLayoutResult?>(null) }

    Box(Modifier.fillMaxWidth()) {
        Text(text = base, style = style, textAlign = textAlign, modifier = Modifier.fillMaxWidth())
        if (sweep != null) {
            val overlay = buildAnnotatedString {
                withStyle(SpanStyle(color = Color.Transparent)) { append(text, 0, sweep.start) }
                withStyle(SpanStyle(color = primary)) { append(text, sweep.start, sweep.end) }
                withStyle(SpanStyle(color = Color.Transparent)) {
                    append(text, sweep.end, text.length)
                }
            }
            Text(
                text = overlay,
                style = style,
                textAlign = textAlign,
                onTextLayout = { layout = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics {}
                    .drawWithContent {
                        val textLayout = layout ?: return@drawWithContent
                        val visualBoxes = (sweep.start until sweep.end).map { index ->
                            KaraokeVisualBox(
                                line = textLayout.getLineForOffset(index),
                                bounds = textLayout.getBoundingBox(index),
                            )
                        }
                        val clip = Path()
                        karaokeVisualSweepClipRects(
                            boxes = visualBoxes,
                            progress = sweep.progress,
                        ).forEach(clip::addRect)
                        val contentScope = this
                        clipPath(clip) { contentScope.drawContent() }
                    },
            )
        }
    }
}

internal data class KaraokeVisualBox(
    val line: Int,
    val bounds: Rect,
)

internal fun karaokeVisualSweepClipRects(
    boxes: List<KaraokeVisualBox>,
    progress: Float,
): List<Rect> {
    val segments = boxes
        .filter { it.bounds.width > 0f && it.bounds.height > 0f }
        .groupBy(KaraokeVisualBox::line)
        .toSortedMap()
        .values
        .flatMap { lineBoxes ->
            val sorted = lineBoxes
                .map(KaraokeVisualBox::bounds)
                .sortedWith(compareBy<Rect> { it.left }.thenBy { it.right })
            if (sorted.isEmpty()) {
                emptyList()
            } else {
                buildList {
                    var current = sorted.first()
                    sorted.drop(1).forEach { next ->
                        current = if (next.left <= current.right) {
                            Rect(
                                left = current.left,
                                top = minOf(current.top, next.top),
                                right = maxOf(current.right, next.right),
                                bottom = maxOf(current.bottom, next.bottom),
                            )
                        } else {
                            add(current)
                            next
                        }
                    }
                    add(current)
                }
            }
        }

    val totalWidth = segments.sumOf { it.width.toDouble() }.toFloat()
    var remaining = totalWidth * progress.coerceIn(0f, 1f)

    return buildList {
        for (segment in segments) {
            if (remaining <= 0f) break
            val width = remaining.coerceAtMost(segment.width)
            add(
                Rect(
                    left = segment.left,
                    top = segment.top,
                    right = segment.left + width,
                    bottom = segment.bottom,
                ),
            )
            remaining -= width
        }
    }
}


internal fun validKaraokeLine(text: String, karaoke: KaraokeLineUiState?): KaraokeLineUiState? =
    karaoke?.takeIf {
        val sweepValid = it.sweep?.let { sweep ->
            sweep.start >= 0 && sweep.end <= text.length &&
                sweep.start < sweep.end && sweep.progress.isFinite()
        } ?: true
        it.completedEnd in 0..text.length && sweepValid
    }
