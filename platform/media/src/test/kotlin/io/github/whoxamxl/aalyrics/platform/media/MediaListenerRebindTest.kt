package io.github.whoxamxl.aalyrics.platform.media

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MediaListenerRebindTest {
    @AfterTest
    fun detachDiagnostics() = MediaConnectionDiagnostics.detach()

    @Test
    fun `ordinary retries leave the current binding untouched`() {
        val calls = mutableListOf<String>()
        requestMediaListenerRebind(
            resetBinding = false,
            requestUnbind = { calls.add("unbind") },
            requestRebind = { calls.add("rebind") },
        )
        assertEquals(listOf("rebind"), calls)
    }

    @Test
    fun `manual reset requests unbind then rebind without reporting a connected listener`() {
        val events = recordEvents()
        val calls = mutableListOf<String>()
        requestMediaListenerRebind(
            resetBinding = true,
            requestUnbind = { calls.add("unbind") },
            requestRebind = { calls.add("rebind") },
        )
        assertEquals(listOf("unbind", "rebind"), calls)
        assertEquals(
            listOf("unbind_requested", "unbind_request_returned", "rebind_requested", "rebind_request_returned"),
            events.map { it.first },
        )
    }

    @Test
    fun `platform without component unbind still requests ordinary rebind`() {
        val events = recordEvents()
        var requests = 0
        requestMediaListenerRebind(
            resetBinding = true,
            requestUnbind = null,
            requestRebind = { requests++ },
        )
        assertEquals(1, requests)
        assertEquals(
            listOf("unbind_request_unsupported", "rebind_requested", "rebind_request_returned"),
            events.map { it.first },
        )
    }

    @Test
    fun `failed unbind still rebinds and diagnostics retain only the exception class`() {
        val events = recordEvents()
        var requests = 0
        requestMediaListenerRebind(
            resetBinding = true,
            requestUnbind = { throw SecurityException("private platform details") },
            requestRebind = { requests++ },
        )
        assertEquals(1, requests)
        assertEquals(
            listOf("unbind_requested", "unbind_request_failed", "rebind_requested", "rebind_request_returned"),
            events.map { it.first },
        )
        assertEquals(mapOf("exception" to "SecurityException"), events[1].second)
    }

    @Test
    fun `failed rebind is contained and cannot report success`() {
        val events = recordEvents()
        requestMediaListenerRebind(
            resetBinding = true,
            requestUnbind = {},
            requestRebind = { throw IllegalStateException("private platform details") },
        )
        assertEquals(
            listOf("unbind_requested", "unbind_request_returned", "rebind_requested", "rebind_request_failed"),
            events.map { it.first },
        )
        assertEquals(mapOf("exception" to "IllegalStateException"), events.last().second)
    }

    private fun recordEvents(): MutableList<Pair<String, Map<String, String>>> {
        val events = mutableListOf<Pair<String, Map<String, String>>>()
        MediaConnectionDiagnostics.attach { event, fields -> events.add(event to fields) }
        return events
    }
}
