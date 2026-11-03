package io.github.whoxamxl.aalyrics.ui.phone.lyrics

import kotlin.math.roundToInt

/** Bound the derived image while preserving the source aspect ratio for either orientation. */
internal fun performanceArtworkSize(width: Int, height: Int): Pair<Int, Int> {
    require(width > 0 && height > 0)
    val scale = minOf(1f, 192f / maxOf(width, height))
    return (width * scale).roundToInt().coerceAtLeast(1) to
        (height * scale).roundToInt().coerceAtLeast(1)
}

/** Three small separable box passes soften detail without losing the cover's shapes/colors. */
internal fun blurPerformanceArtwork(pixels: IntArray, width: Int, height: Int): IntArray {
    require(width > 0 && height > 0 && pixels.size == width * height)
    val result = pixels.copyOf()
    val scratch = IntArray(pixels.size)
    repeat(3) {
        blurArtworkPass(result, scratch, width, height, horizontal = true)
        blurArtworkPass(scratch, result, width, height, horizontal = false)
    }
    return result
}

private fun blurArtworkPass(
    source: IntArray,
    target: IntArray,
    width: Int,
    height: Int,
    horizontal: Boolean,
) {
    val radius = 3
    val samples = radius * 2 + 1
    val length = if (horizontal) width else height
    val lines = if (horizontal) height else width
    val stride = if (horizontal) 1 else width
    repeat(lines) { line ->
        val start = if (horizontal) line * width else line
        var alpha = 0
        var red = 0
        var green = 0
        var blue = 0
        fun accumulate(position: Int, sign: Int) {
            val color = source[start + position.coerceIn(0, length - 1) * stride]
            alpha += (color ushr 24) * sign
            red += (color ushr 16 and 255) * sign
            green += (color ushr 8 and 255) * sign
            blue += (color and 255) * sign
        }
        for (offset in -radius..radius) accumulate(offset, 1)
        repeat(length) { position ->
            target[start + position * stride] =
                ((alpha / samples) shl 24) or ((red / samples) shl 16) or
                    ((green / samples) shl 8) or (blue / samples)
            accumulate(position - radius, -1)
            accumulate(position + radius + 1, 1)
        }
    }
}
