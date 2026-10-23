package io.github.whoxamxl.aalyrics.ui.phone.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsTheme
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncAdjustment
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncOffsetCard
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncOffsetEditor
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncOffsetUiState
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncOffsetScope

@Preview(name = "Compact offset", group = "Sync offset", widthDp = 412)
@Composable
private fun SyncOffsetCompactPreview() = OffsetCardPreview(width = 412.dp)

@Preview(name = "Compact offset - narrow large font", group = "Sync offset", widthDp = 320)
@Composable
private fun SyncOffsetCompactNarrowPreview() = OffsetCardPreview(width = 320.dp, fontScale = 1.3f)

@Preview(name = "Compact offset - 200% font", group = "Sync offset", widthDp = 320)
@Composable
private fun SyncOffsetCompactLargeFontPreview() = OffsetCardPreview(width = 320.dp, fontScale = 2f)

@Preview(name = "Compact offset - advance limit", group = "Sync offset", widthDp = 320)
@Composable
private fun SyncOffsetCompactLimitPreview() = OffsetCardPreview(width = 320.dp, fontScale = 1.3f, offsetMs = 30_000L)

@Preview(name = "Compact offset - reset disabled", group = "Sync offset", widthDp = 320)
@Composable
private fun SyncOffsetCompactZeroPreview() = OffsetCardPreview(width = 320.dp, offsetMs = 0L)

@Preview(name = "Compact offset - no timed lyrics", group = "Sync offset", widthDp = 320)
@Composable
private fun SyncOffsetCompactUnavailablePreview() = OffsetCardPreview(width = 320.dp, timed = false)

@Preview(name = "AA offset and combined total", group = "Sync offset", widthDp = 412)
@Composable
private fun AndroidAutoOffsetPreview() = OffsetCardPreview(width = 412.dp, scope = SyncOffsetScope.ANDROID_AUTO)

@Preview(name = "AA offset - 200% font", group = "Sync offset", widthDp = 320)
@Composable
private fun AndroidAutoOffsetLargePreview() = OffsetCardPreview(width = 320.dp, fontScale = 2f, scope = SyncOffsetScope.ANDROID_AUTO)

@Preview(name = "AA offset - delay limit", group = "Sync offset", widthDp = 320)
@Composable
private fun AndroidAutoOffsetLimitPreview() = OffsetCardPreview(width = 320.dp, offsetMs = -30_000L, scope = SyncOffsetScope.ANDROID_AUTO)

@Preview(name = "AA reset disabled - global retained", group = "Sync offset", widthDp = 320)
@Composable
private fun AndroidAutoOffsetZeroPreview() = OffsetCardPreview(width = 320.dp, offsetMs = 0L, scope = SyncOffsetScope.ANDROID_AUTO)

/** Fixed logical width/font scale also work when running the fixture in PreviewActivity. */
@Composable
private fun OffsetCardPreview(
    width: Dp,
    fontScale: Float = 1f,
    offsetMs: Long = -275L,
    timed: Boolean = true,
    scope: SyncOffsetScope = SyncOffsetScope.GLOBAL,
) {
    var offset by remember { mutableStateOf(offsetMs) }
    var editing by remember { mutableStateOf(false) }
    CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
        AALyricsTheme {
            Surface {
                Box(Modifier.width(width).padding(20.dp)) {
                    SyncOffsetCard(
                        state = SyncOffsetUiState(
                            offsetMs = offset,
                            canDelay = timed && offset > -30_000L,
                            canAdvance = timed && offset < 30_000L,
                        ),
                        scope = scope,
                        totalOffsetMs = (300L + offset).takeIf { scope == SyncOffsetScope.ANDROID_AUTO },
                        onAdjust = {
                            val delta = when (it) {
                                SyncAdjustment.DELAY_COARSE -> -500L
                                SyncAdjustment.DELAY_FINE -> -50L
                                SyncAdjustment.ADVANCE_FINE -> 50L
                                SyncAdjustment.ADVANCE_COARSE -> 500L
                            }
                            offset = (offset + delta).coerceIn(-30_000L, 30_000L)
                        },
                        onReset = { offset = 0L },
                        onEdit = { editing = true },
                    )
                }
                if (editing) {
                    SyncOffsetEditor(
                        initialOffsetMs = offset,
                        scope = scope,
                        maxOffsetMs = 30_000L,
                        onSave = { offset = it; editing = false },
                        onDismissRequest = { editing = false },
                    )
                }
            }
        }
    }
}
