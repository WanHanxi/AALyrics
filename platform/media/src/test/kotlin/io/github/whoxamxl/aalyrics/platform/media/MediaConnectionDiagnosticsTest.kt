package io.github.whoxamxl.aalyrics.platform.media

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MediaConnectionDiagnosticsTest {
    @AfterTest fun detach() = MediaConnectionDiagnostics.detach()

    @Test
    fun `disabled diagnostics never evaluate media reads`() {
        MediaConnectionDiagnostics.detach()
        MediaConnectionDiagnostics.record("disabled") { error("must not evaluate") }
        MediaConnectionDiagnostics.changed("disabled") { error("must not evaluate") }
    }

    @Test
    fun `failed diagnostic sink or field read cannot break playback`() {
        MediaConnectionDiagnostics.attach { _, _ -> error("storage unavailable") }
        MediaConnectionDiagnostics.record("event")
        MediaConnectionDiagnostics.changed("event") { error("session gone") }
    }

    @Test
    fun `unchanged session summaries are not repeatedly logged`() {
        val events = mutableListOf<Map<String, String>>()
        MediaConnectionDiagnostics.attach { _, fields -> events += fields }
        repeat(100) { MediaConnectionDiagnostics.changed("sessions") { mapOf("count" to "1") } }
        MediaConnectionDiagnostics.changed("sessions") { mapOf("count" to "0") }
        assertEquals(listOf(mapOf("count" to "1"), mapOf("count" to "0")), events)
    }
}
