package io.github.whoxamxl.aalyrics.ui.phone.lyrics

import android.graphics.Bitmap
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import io.github.whoxamxl.aalyrics.ui.designsystem.theme.AALyricsColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun PerformanceArtworkBackground(artwork: ImageBitmap?, modifier: Modifier = Modifier) {
    // Preparation is keyed only by the artwork, not the 33 ms Karaoke tick or viewport size.
    // Preview runs the same preparation synchronously because its render clock may not advance.
    val background = if (LocalInspectionMode.current) {
        remember(artwork) { artwork?.let(::preparePerformanceArtwork) }
    } else {
        val prepared by produceState<ImageBitmap?>(null, artwork) {
            value = artwork?.let { withContext(Dispatchers.Default) { preparePerformanceArtwork(it) } }
        }
        // A removed source must not retain the previous cover while the effect is restarting.
        if (artwork == null) null else prepared
    }
    Crossfade(
        targetState = background,
        modifier = modifier,
        animationSpec = tween(400),
        label = "Performance artwork",
    ) { image ->
        if (image != null) {
            Box(Modifier.fillMaxSize()) {
                Image(
                    bitmap = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.Medium,
                    modifier = Modifier.fillMaxSize(),
                )
                // Even a white cover stays subordinate to text. The center gets extra contrast,
                // while the top/bottom retain enough color and shape to recognize the album.
                Box(Modifier.fillMaxSize().background(AALyricsColors.BackgroundBase.copy(alpha = 0.64f)))
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.08f),
                    0.30f to Color.Black.copy(alpha = 0.28f),
                    0.55f to Color.Black.copy(alpha = 0.32f),
                    1f to Color.Black.copy(alpha = 0.12f),
                )))
            }
        }
    }
}

private fun preparePerformanceArtwork(image: ImageBitmap): ImageBitmap? {
    return try {
        val source = image.asAndroidBitmap()
        val (width, height) = performanceArtworkSize(source.width, source.height)
        val sampled = Bitmap.createScaledBitmap(source, width, height, true)
        // A tiny source can be returned unchanged by createScaledBitmap. Hardware bitmaps cannot
        // be read on the CPU; copy only that sampled result and never modify/recycle shared art.
        val readable = if (sampled.config == Bitmap.Config.HARDWARE) {
            sampled.copy(Bitmap.Config.ARGB_8888, false)
        } else sampled
        try {
            val pixels = IntArray(width * height)
            readable.getPixels(pixels, 0, width, 0, 0, width, height)
            Bitmap.createBitmap(
                blurPerformanceArtwork(pixels, width, height),
                width, height, Bitmap.Config.ARGB_8888,
            ).asImageBitmap()
        } finally {
            if (readable !== sampled) readable.recycle()
            if (sampled !== source) sampled.recycle()
        }
    } catch (_: RuntimeException) {
        // A player may release or supply an unreadable bitmap. Lyrics still have a dark fallback.
        null
    }
}
