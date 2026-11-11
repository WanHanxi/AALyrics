package io.github.whoxamxl.aalyrics.platform.media

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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

    @Test
    fun `list notifications retain order and identical callbacks without reading metadata`() {
        val events = mutableListOf<Map<String, String>>()
        MediaConnectionDiagnostics.attach { event, fields ->
            assertEquals("sessions_observed", event)
            events += fields
        }
        val lyrics = Controller("lyrics-token", "app.lyrics")
        val player = Controller("player-token", "app.player")

        MediaConnectionDiagnostics.sessions(listOf(player, lyrics), "query")
        repeat(2) {
            MediaConnectionDiagnostics.sessions(listOf(lyrics, player), "active_sessions_callback")
        }

        assertEquals(3, events.size)
        assertEquals("query", events[0]["reason"])
        assertEquals("active_sessions_callback", events[1]["reason"])
        assertTrue(events[0].getValue("session_0").contains("package=app.player"))
        assertEquals(events[0]["session_1"], events[1]["session_0"])
        assertEquals(events[1]["session_0"], events[2]["session_0"])
        assertNotEquals(events[1]["sequence"], events[2]["sequence"])
        assertFalse(events.toString().contains("lyrics-token"))
        assertFalse(events.toString().contains("player-token"))
    }

    @Test
    fun `replacement session from same package is distinguishable and matches own session log`() {
        val events = mutableListOf<Map<String, String>>()
        MediaConnectionDiagnostics.attach { _, fields -> events += fields }
        val old = Controller("old-token", "app.player")
        val replacement = Controller("new-token", "app.player")

        MediaConnectionDiagnostics.controllerEvent("controller_switch", old)
        MediaConnectionDiagnostics.controllerEvent("controller_switch", replacement)
        MediaConnectionDiagnostics.sessionEvent("automotive_session_activated", replacement.token, emptyMap())

        assertNotEquals(events[0]["token_hash"], events[1]["token_hash"])
        assertEquals(events[1]["token_hash"], events[2]["token_hash"])
        assertEquals(events[0]["package"], events[1]["package"])
    }

    @Test
    fun `one unreadable controller does not hide other sessions and list size stays bounded`() {
        val events = mutableListOf<Map<String, String>>()
        MediaConnectionDiagnostics.attach { _, fields -> events += fields }
        val controllers = (0 until 20).map { Controller("token-$it", "app.$it") }
        controllers[0].failRead = true

        MediaConnectionDiagnostics.sessions(controllers, "active_sessions_callback")

        val event = events.single()
        assertEquals("20", event["count"])
        assertEquals("4", event["omitted"])
        assertTrue(event.getValue("session_0").contains("read_error=IllegalStateException"))
        assertTrue(event.getValue("session_1").contains("package=app.1"))
        assertFalse(event.containsKey("session_16"))
        assertFalse(event.toString().contains("private failure text"))
    }

    @Test
    fun `playback churn is deduplicated but buffering and restored playing are retained`() {
        val states = mutableListOf<String>()
        MediaConnectionDiagnostics.attach { _, fields -> states += fields.getValue("playback_state") }
        val controller = Controller("token", "app.player")

        repeat(100) {
            MediaConnectionDiagnostics.controllerEvent("controller_playback_callback", controller, deduplicate = true)
        }
        controller.state = 6
        MediaConnectionDiagnostics.controllerEvent("controller_playback_callback", controller, deduplicate = true)
        controller.state = 3
        MediaConnectionDiagnostics.controllerEvent("controller_playback_callback", controller, deduplicate = true)

        assertEquals(listOf("3", "6", "3"), states)
    }

    @Test
    fun `disabled session diagnostics never read controller facts or token hashes`() {
        MediaConnectionDiagnostics.detach()
        val controller = Controller("token", "app.player")
        MediaConnectionDiagnostics.sessions(listOf(controller), "query")
        MediaConnectionDiagnostics.controllerEvent("controller_playback_callback", controller)
        MediaConnectionDiagnostics.controllerEvent("controller_playback_callback", controller, deduplicate = true)
        val token = object {
            override fun hashCode(): Int = error("must not read disabled token")
        }
        MediaConnectionDiagnostics.sessionEvent("automotive_session_activated", token, emptyMap())
        assertEquals(0, controller.reads)
    }

    @Test
    fun `published state deduplication retains new connection requests and activation events`() {
        val events = mutableListOf<Pair<String, Map<String, String>>>()
        MediaConnectionDiagnostics.attach { event, fields -> events += event to fields }
        val fields = mapOf("request" to "1", "playback" to "PLAYING")
        repeat(100) {
            MediaConnectionDiagnostics.sessionEvent("automotive_playback_published", "token", fields, deduplicate = true)
        }
        MediaConnectionDiagnostics.sessionEvent(
            "automotive_playback_published", "token", fields + ("request" to "2"), deduplicate = true,
        )
        repeat(2) {
            MediaConnectionDiagnostics.sessionEvent("automotive_session_activated", "token", fields)
        }
        assertEquals(4, events.size)
        assertEquals("2", events[1].second["request"])
        assertEquals(2, events.count { it.first == "automotive_session_activated" })
    }

    private class Controller(
        override val token: String,
        override val packageName: String,
    ) : RuntimeMediaController<String> {
        var state = 3
        var failRead = false
        var reads = 0
        override val isPlaying get() = state == 3
        override fun diagnosticPlayback(): Map<String, String> {
            reads++
            if (failRead) error("private failure text")
            return mapOf("playback_state" to state.toString(), "playback_type" to "1")
        }
        override fun snapshot(): Nothing = error("diagnostics must not read track metadata")
        override fun controlState(): Nothing = error("diagnostics must not read queue metadata")
        override fun attach(callback: RuntimeMediaControllerCallback): Nothing = error("must not attach")
        override fun detach(callback: RuntimeMediaControllerCallback): Nothing = error("must not detach")
        override fun play(): Nothing = error("must not control playback")
        override fun pause(): Nothing = error("must not control playback")
        override fun skipToPrevious(): Nothing = error("must not control playback")
        override fun skipToNext(): Nothing = error("must not control playback")
        override fun seekTo(positionMs: Long): Nothing = error("must not control playback")
        override fun skipToQueueItem(queueItemId: Long): Nothing = error("must not control playback")
        override fun openSessionActivity(): Nothing = error("must not launch")
    }
}
