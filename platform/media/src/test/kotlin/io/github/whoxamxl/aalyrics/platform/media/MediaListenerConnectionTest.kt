package io.github.whoxamxl.aalyrics.platform.media

import kotlin.test.Test
import kotlin.test.assertEquals

class MediaListenerConnectionTest {
    @Test
    fun `application attachment receives current binding independently of diagnostics`() {
        val events = mutableListOf<Boolean>()
        val sink = MediaListenerConnectionSink(events::add)
        try {
            MediaSessionRuntimeHost.forwardListenerConnection(true)
            MediaSessionRuntimeHost.attachListenerConnection(sink)
            MediaSessionRuntimeHost.forwardListenerConnection(false)
            assertEquals(listOf(true, false), events)
            MediaSessionRuntimeHost.detachListenerConnection(sink)
            MediaSessionRuntimeHost.forwardListenerConnection(true)
            assertEquals(listOf(true, false), events)
        } finally {
            MediaSessionRuntimeHost.detachListenerConnection(sink)
            MediaSessionRuntimeHost.forwardListenerConnection(false)
        }
    }
}
