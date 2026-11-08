package io.github.whoxamxl.aalyrics

import io.github.whoxamxl.aalyrics.core.model.PlaybackSnapshot
import io.github.whoxamxl.aalyrics.core.model.PlaybackSource
import io.github.whoxamxl.aalyrics.core.model.PlaybackStatus
import io.github.whoxamxl.aalyrics.core.model.Track
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidAutoActivationTest {
    @Test
    fun `play then connect activates once without phone or lyrics input`() = runTest {
        val fixture = Fixture(this)
        fixture.play()
        runCurrent()
        assertEquals(0, fixture.bindings.size)
        fixture.activation.onProjectionChanged(true)
        runCurrent()
        assertEquals(1, fixture.bindings.size)
        repeat(10) {
            fixture.activation.onProjectionChanged(true)
            fixture.play()
        }
        runCurrent()
        assertEquals(1, fixture.bindings.size)
    }

    @Test
    fun `connect then play waits for eligible playing track and source`() = runTest {
        val fixture = Fixture(this)
        fixture.activation.onProjectionChanged(true)
        val invalid = listOf(
            playing.copy(track = null),
            playing.copy(source = null),
            playing.copy(status = PlaybackStatus.PAUSED),
            playing.copy(status = PlaybackStatus.BUFFERING),
            playing.copy(status = PlaybackStatus.STOPPED),
            PlaybackSnapshot(),
        )
        invalid.forEach {
            fixture.activation.onPlaybackSnapshot(it, sourceEligible = true)
            runCurrent()
        }
        fixture.play(eligible = false)
        runCurrent()
        assertEquals(0, fixture.bindings.size)
        // Settings can make an already-playing source eligible without a new track event.
        fixture.play()
        runCurrent()
        assertEquals(1, fixture.bindings.size)
    }

    @Test
    fun `pause track change and temporary missing playback retain binding`() = runTest {
        val fixture = Fixture(this)
        fixture.activate()
        val binding = fixture.bindings.single()
        fixture.activation.onPlaybackSnapshot(playing.copy(status = PlaybackStatus.PAUSED), true)
        fixture.activation.onPlaybackSnapshot(PlaybackSnapshot(), false)
        fixture.activation.onPlaybackSnapshot(playing.copy(track = Track("Another song")), true)
        runCurrent()
        assertEquals(1, fixture.bindings.size)
        assertEquals(0, binding.unbinds)
        fixture.activation.onProjectionChanged(false)
        assertEquals(1, binding.unbinds)
    }

    @Test
    fun `projection disconnect releases even while host and phone demand remain active`() = runTest {
        val fixture = Fixture(this)
        val gate = LyricsDemandGate({}) {}
        gate.setPhoneProcessForeground(true)
        gate.setAutomotiveProjectionConnected(true)
        fixture.activate()
        // The self-created browser service contributes host demand just like a host-created one.
        gate.setAutomotiveHostActive(true)
        gate.setAutomotiveProjectionConnected(false)
        fixture.activation.onProjectionChanged(false)
        fixture.play()
        advanceUntilIdle()
        assertEquals(true, gate.isActive())
        assertEquals(1, fixture.bindings.size)
        assertEquals(1, fixture.bindings.single().unbinds)
    }

    @Test
    fun `disconnect before queued bind cancels creation`() = runTest {
        val fixture = Fixture(this)
        fixture.play()
        fixture.activation.onProjectionChanged(true)
        fixture.activation.onProjectionChanged(false)
        advanceUntilIdle()
        assertEquals(0, fixture.bindings.size)
    }

    @Test
    fun `pending binding releases on disconnect and late callbacks cannot reactivate it`() = runTest {
        val fixture = Fixture(this)
        fixture.activate()
        val old = fixture.bindings.single()
        fixture.activation.onProjectionChanged(false)
        old.listener.onConnected()
        old.listener.onBindingLost("binding_died")
        old.listener.onDisconnected()
        advanceUntilIdle()
        assertEquals(1, old.unbinds)
        assertEquals(1, fixture.bindings.size)
        assertEquals(0, fixture.events.count { it == "automotive_activation_connected" })
    }

    @Test
    fun `a new car connection can reactivate but paused playback alone cannot`() = runTest {
        val fixture = Fixture(this)
        fixture.activate()
        fixture.activation.onProjectionChanged(false)
        fixture.activation.onPlaybackSnapshot(playing.copy(status = PlaybackStatus.PAUSED), true)
        fixture.activation.onProjectionChanged(true)
        runCurrent()
        assertEquals(1, fixture.bindings.size)
        fixture.play()
        runCurrent()
        assertEquals(2, fixture.bindings.size)
    }

    @Test
    fun `new process restores from current projection and replayed playback in either order`() = runTest {
        for (playbackFirst in listOf(true, false)) {
            val fixture = Fixture(this)
            if (playbackFirst) fixture.play()
            fixture.activation.onProjectionChanged(true)
            if (!playbackFirst) fixture.play()
            runCurrent()
            assertEquals(1, fixture.bindings.size)
            fixture.activation.stop()
        }
    }

    @Test
    fun `rejected and throwing binds release and stop after three spaced attempts`() = runTest {
        for (throws in listOf(false, true)) {
            val fixture = Fixture(this, accepted = false, throws = throws)
            fixture.activate()
            assertEquals(1, fixture.bindings.size)
            advanceTimeBy(999)
            assertEquals(1, fixture.bindings.size)
            advanceTimeBy(1)
            runCurrent()
            assertEquals(2, fixture.bindings.size)
            advanceUntilIdle()
            assertEquals(3, fixture.bindings.size)
            assertEquals(listOf(1, 1, 1), fixture.bindings.map { it.unbinds })
            repeat(20) { fixture.play() }
            advanceUntilIdle()
            assertEquals(3, fixture.bindings.size)
            fixture.activation.onProjectionChanged(false)
            fixture.activation.onProjectionChanged(true)
            runCurrent()
            assertEquals(4, fixture.bindings.size)
            fixture.activation.stop()
        }
    }

    @Test
    fun `temporary service disconnect leaves system binding intact`() = runTest {
        val fixture = Fixture(this)
        fixture.activate()
        val binding = fixture.bindings.single()
        binding.listener.onConnected()
        binding.listener.onDisconnected()
        advanceUntilIdle()
        assertEquals(0, binding.unbinds)
        assertEquals(1, fixture.bindings.size)
        binding.listener.onConnected()
        assertEquals(2, fixture.events.count { it == "automotive_activation_connected" })
    }

    @Test
    fun `dead and null bindings retry during pause and ignore callbacks from older bindings`() = runTest {
        val fixture = Fixture(this)
        fixture.activate()
        fixture.activation.onPlaybackSnapshot(playing.copy(status = PlaybackStatus.PAUSED), true)
        val first = fixture.bindings.single()
        first.listener.onBindingLost("binding_died")
        advanceUntilIdle()
        assertEquals(2, fixture.bindings.size)
        first.listener.onBindingLost("null_binding")
        assertEquals(1, first.unbinds)
        val second = fixture.bindings.last()
        second.listener.onBindingLost("null_binding")
        advanceUntilIdle()
        assertEquals(3, fixture.bindings.size)
        fixture.bindings.last().listener.onBindingLost("binding_died")
        advanceUntilIdle()
        assertEquals(3, fixture.bindings.size)
        assertEquals(listOf(1, 1, 1), fixture.bindings.map { it.unbinds })
    }

    @Test
    fun `stop cancels delayed retries and releases only the owned binding once`() = runTest {
        val fixture = Fixture(this, accepted = false)
        fixture.activate()
        fixture.activation.stop()
        fixture.activation.stop()
        advanceUntilIdle()
        assertEquals(1, fixture.bindings.size)
        assertEquals(1, fixture.bindings.single().unbinds)
    }

    @Test
    fun `synchronous binding loss cannot leak ownership or exceed retry budget`() = runTest {
        val fixture = Fixture(this, loseSynchronously = true)
        fixture.activate()
        advanceUntilIdle()
        assertEquals(3, fixture.bindings.size)
        assertEquals(listOf(1, 1, 1), fixture.bindings.map { it.unbinds })
    }

    private class Fixture(
        val scope: TestScope,
        accepted: Boolean = true,
        throws: Boolean = false,
        loseSynchronously: Boolean = false,
    ) {
        val bindings = mutableListOf<FakeBinding>()
        val events = mutableListOf<String>()
        val activation = AndroidAutoActivation(
            scope = scope,
            createBinding = {
                FakeBinding(accepted, throws, loseSynchronously).also(bindings::add)
            },
            record = { event, _ -> events += event },
        )

        fun play(eligible: Boolean = true) = activation.onPlaybackSnapshot(playing, eligible)

        fun activate() {
            activation.onProjectionChanged(true)
            play()
            scope.runCurrent()
        }
    }

    private class FakeBinding(
        private val accepted: Boolean,
        private val throws: Boolean,
        private val loseSynchronously: Boolean,
    ) : AutomotiveActivationBinding {
        lateinit var listener: AutomotiveActivationBinding.Listener
        var unbinds = 0

        override fun bind(listener: AutomotiveActivationBinding.Listener): Boolean {
            this.listener = listener
            if (throws) throw SecurityException("Test rejection")
            if (loseSynchronously) listener.onBindingLost("null_binding")
            return accepted
        }

        override fun unbind() { unbinds++ }
    }

    private companion object {
        val playing = PlaybackSnapshot(
            track = Track(title = "Song", artists = listOf("Artist")),
            source = PlaybackSource("test.audio"),
            status = PlaybackStatus.PLAYING,
        )
    }
}
