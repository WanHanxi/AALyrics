package io.github.whoxamxl.aalyrics

import io.github.whoxamxl.aalyrics.cache.files.FileLyricsCache
import io.github.whoxamxl.aalyrics.core.lyrics.CandidateSelectionPreferences
import io.github.whoxamxl.aalyrics.core.lyrics.CandidateSelector
import io.github.whoxamxl.aalyrics.core.lyrics.LyricsState
import io.github.whoxamxl.aalyrics.core.model.LyricsAttribution
import io.github.whoxamxl.aalyrics.core.model.LyricsDocument
import io.github.whoxamxl.aalyrics.core.model.LyricsSyncType
import io.github.whoxamxl.aalyrics.core.model.PlaybackSnapshot
import io.github.whoxamxl.aalyrics.core.model.SuppliedLyricsTranslation
import io.github.whoxamxl.aalyrics.core.model.TimedLyricLine
import io.github.whoxamxl.aalyrics.core.model.Track
import io.github.whoxamxl.aalyrics.provider.api.LyricsCandidate
import io.github.whoxamxl.aalyrics.provider.api.LyricsProvider
import io.github.whoxamxl.aalyrics.provider.api.LyricsProviderDescriptor
import io.github.whoxamxl.aalyrics.provider.api.LyricsProviderId
import io.github.whoxamxl.aalyrics.provider.api.LyricsRequest
import java.nio.file.Files
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NeteaseOptInIntegrationTest {
    private val directory = Files.createTempDirectory("aalyrics-opt-in-test").toFile()
    private val track = Track("Synthetic song", listOf("Test artist"))
    private val original = Provider("lrclib")
    private val netease = Provider("netease")
    private val selector = object : CandidateSelector {
        override fun select(
            track: Track,
            candidates: List<LyricsCandidate>,
            preferences: CandidateSelectionPreferences,
        ) = candidates.firstOrNull { it.providerId.value == "netease" } ?: candidates.firstOrNull()
    }

    @AfterTest fun cleanUp() { directory.deleteRecursively() }

    @Test fun `default off never requests NetEase and toggling isolates disk caches across restarts`() = runTest {
        val graph = graph()
        graph.play()
        advanceUntilIdle()
        assertEquals("lrclib", graph.source())
        assertEquals(0, netease.calls)

        graph.updateNeteaseEnabled(true)
        advanceUntilIdle()
        assertEquals("netease", graph.source())
        assertEquals(1, netease.calls)
        assertEquals("zh", assertIs<LyricsState.Ready>(graph.lyricsState.value).lyrics.suppliedTranslation?.languageTag)

        graph.updateNeteaseEnabled(false)
        assertIs<LyricsState.Loading>(graph.lyricsState.value)
        advanceUntilIdle()
        assertEquals("lrclib", graph.source())
        assertTrue(graph.lyricsLookupDiagnostics.value.cacheHit)
        assertEquals(1, netease.calls)
        assertEquals(null, assertIs<LyricsState.Ready>(graph.lyricsState.value).lyrics.suppliedTranslation)

        // No cache deletion on disable/reset: both choices still work after process recreation.
        val restoredOff = graph()
        restoredOff.play()
        advanceUntilIdle()
        assertEquals("lrclib", restoredOff.source())
        val restoredOn = graph(enabled = true)
        restoredOn.play()
        advanceUntilIdle()
        assertEquals("netease", restoredOn.source())
        assertEquals(1, netease.calls)
        assertEquals(2, original.calls)
    }

    @Test fun `disabling clears retained lyrics without creating demand or reusing them on resume`() = runTest {
        val graph = graph(enabled = true)
        graph.play()
        advanceUntilIdle()
        assertEquals("netease", graph.source())
        graph.lyricsDemandGate.setPhoneProcessForeground(false)
        assertIs<LyricsState.Ready>(graph.lyricsState.value)

        graph.updateNeteaseEnabled(false)
        advanceUntilIdle()
        assertIs<LyricsState.Idle>(graph.lyricsState.value)
        assertEquals(1, original.calls)
        graph.lyricsDemandGate.setPhoneProcessForeground(true)
        advanceUntilIdle()
        assertEquals("lrclib", graph.source())
        assertEquals(1, netease.calls)
        assertEquals(2, original.calls)
    }

    @Test fun `disable cancels ongoing lookup and a late NetEase result cannot replace the original source`() = runTest {
        val release = CompletableDeferred<Unit>()
        var cancelled = false
        val delayed = object : LyricsProvider {
            override val descriptor = netease.descriptor
            override suspend fun search(request: LyricsRequest): List<LyricsCandidate> {
                try {
                    awaitCancellation()
                } catch (_: CancellationException) {
                    cancelled = true
                    withContext(NonCancellable) { release.await() }
                    return netease.search(request)
                }
            }
        }
        val graph = ApplicationGraph(
            listOf(original, delayed), selector, this, CandidateSelectionPreferences(),
            neteaseEnabled = true,
        )
        graph.play()
        runCurrent()
        assertIs<LyricsState.Loading>(graph.lyricsState.value)
        graph.updateNeteaseEnabled(false)
        runCurrent()
        assertTrue(cancelled)
        assertEquals("lrclib", graph.source())
        release.complete(Unit)
        advanceUntilIdle()
        assertEquals("lrclib", graph.source())
        assertEquals(1, netease.calls)
    }

    @Test fun `enabling while a playback source is ineligible cannot query providers`() = runTest {
        val graph = graph()
        graph.lyricsDemandGate.setPhoneProcessForeground(true)
        graph.lyricsDemandGate.onPlaybackSnapshot(PlaybackSnapshot(track = track), sourceEligible = false)
        graph.updateNeteaseEnabled(true)
        advanceUntilIdle()
        assertEquals(0, netease.calls)
        assertEquals(0, original.calls)
        graph.lyricsDemandGate.setSourceEligible(true)
        advanceUntilIdle()
        assertEquals(1, netease.calls)
    }

    private fun TestScope.graph(enabled: Boolean = false) = ApplicationGraph(
        listOf(original, netease), selector, this, CandidateSelectionPreferences(),
        lyricsCache = FileLyricsCache(directory, dispatcher = StandardTestDispatcher(testScheduler)),
        neteaseEnabled = enabled,
    )

    private fun ApplicationGraph.play() {
        lyricsDemandGate.setPhoneProcessForeground(true)
        val snapshot = PlaybackSnapshot(track = track)
        playbackSnapshotSink.onPlaybackSnapshot(snapshot)
        lyricsDemandGate.onPlaybackSnapshot(snapshot, sourceEligible = true)
    }

    private fun ApplicationGraph.source() = assertIs<LyricsState.Ready>(lyricsState.value).lyrics.attribution?.providerId

    private class Provider(id: String) : LyricsProvider {
        override val descriptor = LyricsProviderDescriptor(LyricsProviderId(id), id, setOf(LyricsSyncType.LINE))
        var calls = 0
        override suspend fun search(request: LyricsRequest): List<LyricsCandidate> {
            calls++
            val attribution = LyricsAttribution(descriptor.id.value, descriptor.displayName)
            return listOf(LyricsCandidate(
                descriptor.id, request.track,
                LyricsDocument(
                    lines = listOf(TimedLyricLine("Synthetic test line", 1000)),
                    attribution = attribution,
                    suppliedTranslation = if (descriptor.id.value == "netease") {
                        SuppliedLyricsTranslation("zh", listOf("测试译文"), attribution)
                    } else null,
                ),
            ))
        }
    }
}
