package io.github.whoxamxl.aalyrics.ui.phone.lyrics

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PerformanceArtworkBlurTest {
    @Test fun `large artwork is bounded without stretching or enlarging small sources`() {
        assertEquals(192 to 192, performanceArtworkSize(4096, 4096))
        assertEquals(192 to 96, performanceArtworkSize(2048, 1024))
        assertEquals(96 to 192, performanceArtworkSize(1024, 2048))
        assertEquals(32 to 64, performanceArtworkSize(32, 64))
        assertEquals(1 to 192, performanceArtworkSize(1, 8192))
    }

    @Test fun `solid bright dark and transparent covers keep their color at the edges`() {
        for (color in listOf(0xFFFFFFFF.toInt(), 0xFF020812.toInt(), 0x802080C0.toInt(), 0)) {
            val pixels = IntArray(17 * 13) { color }
            assertContentEquals(pixels, blurPerformanceArtwork(pixels, 17, 13))
        }
    }

    @Test fun `busy details soften without mutating the shared source pixels`() {
        val white = 0xFFFFFFFF.toInt()
        val black = 0xFF000000.toInt()
        val pixels = IntArray(25 * 25) { if (it % 25 < 12) black else white }
        val original = pixels.copyOf()
        val blurred = blurPerformanceArtwork(pixels, 25, 25)
        assertContentEquals(original, pixels)
        assertEquals(black, blurred[12 * 25])
        assertEquals(white, blurred[12 * 25 + 24])
        assertTrue((blurred[12 * 25 + 11] and 255) in 1..254)
        assertTrue((blurred[12 * 25 + 12] and 255) in 1..254)
        assertTrue(blurred.all { it ushr 24 == 255 })
    }

    @Test fun `one pixel and thin artwork have valid clamped blur edges`() {
        val color = 0xFF5070A0.toInt()
        assertContentEquals(intArrayOf(color), blurPerformanceArtwork(intArrayOf(color), 1, 1))
        assertContentEquals(IntArray(10) { color }, blurPerformanceArtwork(IntArray(10) { color }, 1, 10))
        assertContentEquals(IntArray(10) { color }, blurPerformanceArtwork(IntArray(10) { color }, 10, 1))
    }
}
