package io.github.whoxamxl.aalyrics.ui.phone.preview

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

internal enum class PerformanceCover { NIGHT, WHITE, BUSY }

/** Synthetic cover fixtures, drawn locally: no downloaded or bundled album artwork. */
@Composable
internal fun rememberPerformanceCover(cover: PerformanceCover): ImageBitmap = remember(cover) {
    val bitmap = Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    when (cover) {
        PerformanceCover.WHITE -> canvas.drawColor(Color.WHITE)
        PerformanceCover.NIGHT -> {
            canvas.drawColor(Color.rgb(24, 48, 64))
            paint.color = Color.rgb(218, 139, 88)
            canvas.drawCircle(228f, 88f, 90f, paint)
            paint.color = Color.rgb(70, 123, 135)
            canvas.drawRect(0f, 182f, 320f, 320f, paint)
            paint.color = Color.rgb(123, 167, 169)
            canvas.drawRect(0f, 198f, 320f, 212f, paint)
            paint.color = Color.rgb(201, 149, 104)
            canvas.drawRect(194f, 222f, 262f, 235f, paint)
            canvas.drawRect(207f, 254f, 250f, 262f, paint)
            paint.color = Color.rgb(17, 33, 49)
            canvas.drawOval(-160f, 240f, 280f, 470f, paint)
        }
        PerformanceCover.BUSY -> {
            val colors = intArrayOf(Color.WHITE, Color.MAGENTA, Color.CYAN, Color.YELLOW, Color.BLACK)
            for (row in 0 until 16) {
                for (column in 0 until 16) {
                    paint.color = colors[(row + column) % colors.size]
                    canvas.drawRect(column * 20f, row * 20f, column * 20f + 20f, row * 20f + 20f, paint)
                }
            }
        }
    }
    bitmap.asImageBitmap()
}
