package io.github.whoxamxl.aalyrics.ui.automotive.service

import io.github.whoxamxl.aalyrics.core.model.PlaybackSnapshot
import io.github.whoxamxl.aalyrics.core.model.PlaybackSource
import io.github.whoxamxl.aalyrics.core.model.PlaybackStatus
import io.github.whoxamxl.aalyrics.core.model.Track
import kotlin.test.Test
import kotlin.test.assertEquals

class AutomotiveSessionActivationTest {
    @Test
    fun `already playing on creation needs only initial activation`() {
        val fixture = Fixture()
        repeat(10) { fixture.publish(playing, request = 1L) }
        assertEquals(listOf(false), fixture.activations)
    }

    @Test
    fun `host opened while paused refreshes only after real playing publication`() {
        val fixture = Fixture()
        val invalid = listOf(
            PlaybackSnapshot(),
            playing.copy(track = null),
            playing.copy(source = null),
            playing.copy(status = PlaybackStatus.PAUSED),
            playing.copy(status = PlaybackStatus.BUFFERING),
            playing.copy(status = PlaybackStatus.STOPPED),
        )
        invalid.forEach { fixture.publish(it, request = 1L) }
        assertEquals(listOf(false), fixture.activations)
        fixture.publish(playing, request = 1L)
        assertEquals(listOf(false, true), fixture.activations)
    }

    @Test
    fun `connection request arrives after playing publication`() {
        val fixture = Fixture()
        fixture.publish(playing, request = null)
        fixture.publish(playing, request = 1L)
        assertEquals(listOf(false, true), fixture.activations)
    }

    @Test
    fun `ticks track and source changes pause resume and gaps never repeat refresh`() {
        val fixture = Fixture()
        fixture.publish(PlaybackSnapshot(), request = null)
        fixture.publish(playing, request = 1L)
        repeat(100) {
            fixture.publish(playing.copy(positionMs = it * 250L), request = 1L)
        }
        fixture.publish(playing.copy(status = PlaybackStatus.PAUSED), request = 1L)
        fixture.publish(PlaybackSnapshot(), request = 1L)
        fixture.publish(playing.copy(track = Track("Next")), request = 1L)
        fixture.publish(playing.copy(source = PlaybackSource("other.audio")), request = 1L)
        assertEquals(listOf(false, true), fixture.activations)
    }

    @Test
    fun `retained host session handles a new connection without recreation`() {
        val fixture = Fixture()
        fixture.publish(playing, request = 1L)
        fixture.publish(playing, request = null)
        fixture.publish(playing.copy(status = PlaybackStatus.PAUSED), request = 2L)
        assertEquals(listOf(false), fixture.activations)
        repeat(10) { fixture.publish(playing, request = 2L) }
        assertEquals(listOf(false, true), fixture.activations)
        // The generation also handles a conflated disconnect/reconnect notification.
        fixture.publish(playing, request = 3L)
        assertEquals(listOf(false, true, true), fixture.activations)
    }

    @Test
    fun `disconnected playing cannot finish a pending connection refresh`() {
        val fixture = Fixture()
        fixture.publish(playing.copy(status = PlaybackStatus.PAUSED), request = 1L)
        repeat(10) { fixture.publish(playing, request = null) }
        assertEquals(listOf(false), fixture.activations)
        fixture.publish(playing, request = 2L)
        assertEquals(listOf(false, true), fixture.activations)
    }

    private class Fixture {
        val activations = mutableListOf<Boolean>()
        private val activation = AutomotiveSessionActivation { activations += it }

        fun publish(playback: PlaybackSnapshot, request: Long?) =
            activation.onPlaybackPublished(playback, request)
    }

    private companion object {
        val playing = PlaybackSnapshot(
            track = Track("Song"),
            source = PlaybackSource("test.audio"),
            status = PlaybackStatus.PLAYING,
        )
    }
}
