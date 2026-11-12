package io.github.whoxamxl.aalyrics

import io.github.whoxamxl.aalyrics.cache.files.FileLyricsCache
import io.github.whoxamxl.aalyrics.core.lyrics.CandidateSelectionPreferences
import io.github.whoxamxl.aalyrics.core.lyrics.LyricsState
import io.github.whoxamxl.aalyrics.core.model.*
import io.github.whoxamxl.aalyrics.provider.api.*
import io.github.whoxamxl.aalyrics.provider.selection.CrossProviderCandidateSelector
import io.github.whoxamxl.aalyrics.translation.api.TranslationSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import java.nio.file.Files
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class KaraokePreferenceIntegrationTest {
    private val directory = Files.createTempDirectory("aalyrics-karaoke-test").toFile()
    private val track = Track("Evening Light", listOf("Example Artist"), "Album", 100_000L)
    private val providers = listOf(Source("netease"), Source("musixmatch"), Source("synclrc"))

    @AfterTest fun cleanUp() { directory.deleteRecursively() }

    @Test fun `all enabled sources participate and toggling reuses separately cached winners`() = runTest {
        val graph = graph()
        graph.play()
        advanceUntilIdle()
        assertEquals("netease", graph.winner())
        assertEquals(listOf(1, 1, 1), providers.map { it.requests.size })
        assertTrue(providers.all { it.requests.single().preferredSyncType == null })

        graph.updateKaraokePreference(featureEnabled = true, modeEnabled = false)
        advanceUntilIdle()
        assertEquals(listOf(1, 1, 1), providers.map { it.requests.size })

        graph.updateKaraokePreference(featureEnabled = true, modeEnabled = true)
        advanceUntilIdle()
        assertEquals("musixmatch", graph.winner())
        assertEquals(listOf(2, 2, 2), providers.map { it.requests.size })
        assertTrue(providers.all { it.requests.last().preferredSyncType == LyricsSyncType.WORD })

        // A translation setting update must not accidentally clear the active Karaoke preference.
        graph.updateTranslationPreference(TranslationSettings(true, "zh", false))
        advanceUntilIdle()
        assertEquals("musixmatch", graph.winner())
        assertEquals(listOf(2, 2, 2), providers.map { it.requests.size })

        graph.updateKaraokePreference(featureEnabled = true, modeEnabled = false)
        advanceUntilIdle()
        assertEquals("netease", graph.winner())
        assertTrue(graph.lyricsLookupDiagnostics.value.cacheHit)
        assertEquals(listOf(2, 2, 2), providers.map { it.requests.size })

        graph.updateKaraokePreference(featureEnabled = true, modeEnabled = true)
        advanceUntilIdle()
        assertEquals("musixmatch", graph.winner())
        assertTrue(graph.lyricsLookupDiagnostics.value.cacheHit)
        assertEquals(listOf(2, 2, 2), providers.map { it.requests.size })
    }

    @Test fun `persisted switches restore preference and reset turns it off without disabling sources`() = runTest {
        val values = mutableMapOf<String, Boolean>()
        val settings = PhonePresentationSettingsPersistence(
            readBoolean = { key, default -> values[key] ?: default },
            writeBooleans = { values.putAll(it) },
        )
        settings.setKaraokeFeatureEnabled(true)
        settings.setKaraokeModeEnabled(true)
        val graph = graph()
        val restored = settings.read()
        graph.updateKaraokePreference(restored.karaokeFeatureEnabled, restored.karaokeModeEnabled)
        graph.play()
        advanceUntilIdle()
        assertEquals("musixmatch", graph.winner())

        settings.resetToDefaults()
        val reset = settings.read()
        graph.updateKaraokePreference(reset.karaokeFeatureEnabled, reset.karaokeModeEnabled)
        advanceUntilIdle()
        assertEquals("netease", graph.winner())
        assertEquals(setOf("netease", "musixmatch", "synclrc"), graph.enabledProviderIds)
        assertTrue(providers.all { it.requests.last().preferredSyncType == null })

        graph.updateKaraokePreference(featureEnabled = false, modeEnabled = true)
        advanceUntilIdle()
        assertEquals(listOf(2, 2, 2), providers.map { it.requests.size })
    }

    @Test fun `switching without lyrics demand waits until resume and preserves the translation target`() = runTest {
        val graph = graph()
        graph.updateKaraokePreference(true, true)
        graph.updateTranslationPreference(TranslationSettings(true, "en"))
        graph.play()
        advanceUntilIdle()
        assertEquals("musixmatch", graph.winner())
        graph.lyricsDemandGate.setPhoneProcessForeground(false)
        graph.updateKaraokePreference(true, false)
        graph.updateTranslationPreference(TranslationSettings(true, "zh"))
        advanceUntilIdle()
        assertEquals(listOf(1, 1, 1), providers.map { it.requests.size })
        graph.lyricsDemandGate.setPhoneProcessForeground(true)
        advanceUntilIdle()
        assertEquals("netease", graph.winner())
        assertEquals(listOf(2, 2, 2), providers.map { it.requests.size })
    }

    private fun TestScope.graph() = ApplicationGraph(
        providers, CrossProviderCandidateSelector(), this, CandidateSelectionPreferences(),
        FileLyricsCache(directory, dispatcher = StandardTestDispatcher(testScheduler)), neteaseEnabled = true,
    ).apply { updateTranslationPreference(TranslationSettings(true, "zh")) }

    private fun ApplicationGraph.play() {
        val snapshot = PlaybackSnapshot(track = track)
        playbackSnapshotSink.onPlaybackSnapshot(snapshot)
        lyricsDemandGate.onPlaybackSnapshot(snapshot, sourceEligible = true)
        lyricsDemandGate.setPhoneProcessForeground(true)
    }

    private fun ApplicationGraph.winner() = assertIs<LyricsState.Ready>(lyricsState.value).lyrics.attribution?.providerId

    private inner class Source(private val id: String) : LyricsProvider {
        override val descriptor = LyricsProviderDescriptor(LyricsProviderId(id), id, setOf(LyricsSyncType.LINE, LyricsSyncType.WORD))
        val requests = mutableListOf<LyricsRequest>()
        override suspend fun search(request: LyricsRequest): List<LyricsCandidate> {
            requests += request
            val attribution = LyricsAttribution(id, sourceId = "test-source")
            val lines = (1..10).map { index ->
                TimedLyricLine("Evening light $index", index * 9_000L, words = if (id != "netease") {
                    listOf(TimedWord("Evening", index * 9_000L), TimedWord(" light $index", index * 9_000L + 500))
                } else emptyList())
            }
            return listOf(LyricsCandidate(descriptor.id,
                request.track.copy(durationMs = if (id == "synclrc") 103_000L else 100_000L),
                LyricsDocument(lines, attribution = attribution, suppliedTranslation = if (id == "netease") {
                    SuppliedLyricsTranslation("zh", lines.indices.map { if (it < 6) "暮色 $it" else null }, attribution)
                } else null),
            ))
        }
    }
}
