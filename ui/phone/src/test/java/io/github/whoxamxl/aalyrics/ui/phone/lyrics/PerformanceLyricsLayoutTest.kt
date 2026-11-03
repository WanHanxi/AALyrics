package io.github.whoxamxl.aalyrics.ui.phone.lyrics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PerformanceLyricsLayoutTest {
    @Test fun `short portrait lyrics stay together near visual center`() {
        val placement = performanceLyricsPlacement(720, 32, 88, 32, 24)
        assertEquals(273, placement.currentTop)
        assertEquals(217, placement.previousTop)
        assertEquals(385, placement.nextTop)
    }

    @Test fun `wrapped landscape lyrics drop previous before upcoming context`() {
        val placement = performanceLyricsPlacement(264, 34, 176, 34, 16)
        assertNull(placement.previousTop)
        assertNotNull(placement.nextTop)
        assertTrue(placement.nextTop + 34 <= 264)
    }

    @Test fun `oversized current scroll container gets the whole height`() {
        assertEquals(
            PerformanceLyricsPlacement(null, 0, null),
            performanceLyricsPlacement(240, 70, 240, 70, 16),
        )
    }

    @Test fun `missing neighbors reserve no empty slots or gaps`() {
        val intro = performanceLyricsPlacement(600, 0, 60, 34, 24)
        val end = performanceLyricsPlacement(600, 34, 60, 0, 24)
        assertNull(intro.previousTop)
        assertNull(end.nextTop)
        assertEquals(intro.currentTop, end.currentTop)
    }

    @Test fun `measured font scales and translations never overlap or escape viewport`() {
        for (height in listOf(0, 100, 240, 300, 720)) {
            for (current in listOf(0, 56, 176, 400, 1000)) {
                for (neighbor in listOf(0, 34, 100, 600)) {
                    val currentHeight = current.coerceAtMost(height)
                    val placement = performanceLyricsPlacement(height, neighbor, currentHeight, neighbor, 24)
                    assertTrue(placement.currentTop >= 0)
                    assertTrue(placement.currentTop + currentHeight <= height)
                    placement.previousTop?.let {
                        assertTrue(it >= 0)
                        assertEquals(placement.currentTop, it + neighbor + 24)
                    }
                    placement.nextTop?.let {
                        assertEquals(placement.currentTop + currentHeight + 24, it)
                        assertTrue(it + neighbor <= height)
                    }
                }
            }
        }
    }
}
