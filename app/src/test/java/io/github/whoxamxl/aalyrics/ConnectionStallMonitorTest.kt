package io.github.whoxamxl.aalyrics

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConnectionStallMonitorTest {
    @Test
    fun `captures once after thirty seconds and rearms after recovery`() {
        val monitor = ConnectionStallMonitor()
        assertFalse(monitor.sample(10_000, true, true))
        assertFalse(monitor.sample(39_999, true, true))
        assertTrue(monitor.sample(40_000, true, true))
        assertFalse(monitor.sample(100_000, true, true))
        assertFalse(monitor.sample(100_001, false, true))
        assertFalse(monitor.sample(110_000, true, true))
        assertTrue(monitor.sample(140_000, true, true))
    }

    @Test
    fun `time without phone or automotive demand does not count toward a stall`() {
        val monitor = ConnectionStallMonitor()
        assertFalse(monitor.sample(0, true, false))
        assertFalse(monitor.sample(100_000, true, true))
        assertFalse(monitor.sample(129_999, true, true))
        assertFalse(monitor.sample(130_000, true, false))
        assertFalse(monitor.sample(200_000, true, true))
        assertTrue(monitor.sample(230_000, true, true))
    }
}
