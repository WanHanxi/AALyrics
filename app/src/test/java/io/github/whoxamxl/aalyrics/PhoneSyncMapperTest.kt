package io.github.whoxamxl.aalyrics

import io.github.whoxamxl.aalyrics.core.lyrics.LyricsLookup
import io.github.whoxamxl.aalyrics.core.lyrics.LyricsLookupId
import io.github.whoxamxl.aalyrics.core.lyrics.LyricsState
import io.github.whoxamxl.aalyrics.core.model.LyricsDocument
import io.github.whoxamxl.aalyrics.core.model.LyricsSyncType
import io.github.whoxamxl.aalyrics.core.model.PlaybackSnapshot
import io.github.whoxamxl.aalyrics.core.model.PlaybackStatus
import io.github.whoxamxl.aalyrics.core.model.TimedLyricLine
import io.github.whoxamxl.aalyrics.core.model.Track
import io.github.whoxamxl.aalyrics.core.timing.LyricsTimingOffset
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.LyricsScreenUiState
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.LyricsViewportInteractionMode
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.LyricsViewportLineUiState
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.LyricsViewportUiState
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.TrackCardLyricsStatus
import io.github.whoxamxl.aalyrics.ui.phone.lyrics.TrackCardUiState
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncAdjustment
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncAvailability
import io.github.whoxamxl.aalyrics.ui.phone.sync.SyncScreenUiState
import io.github.whoxamxl.aalyrics.ui.phone.sync.TapSyncAvailability
import io.github.whoxamxl.aalyrics.ui.phone.sync.TapSyncMessage
import io.github.whoxamxl.aalyrics.ui.phone.sync.TapSyncUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PhoneSyncMapperTest {
    private val lines = listOf(
        LyricsViewportLineUiState("First", translatedText = "第一句"),
        LyricsViewportLineUiState("Second", translatedText = "第二句"),
        LyricsViewportLineUiState("Third"),
    )

    private fun lyrics(
        status: TrackCardLyricsStatus = TrackCardLyricsStatus.READY,
        sync: LyricsSyncType = LyricsSyncType.LINE,
        index: Int? = 1,
    ) = LyricsScreenUiState(
        trackCard = TrackCardUiState(title = "Song", lyricsStatus = status),
        viewport = LyricsViewportUiState(lines = lines, syncType = sync, currentLineIndex = index),
    )

    private fun mapGlobalSync(
        lyrics: LyricsScreenUiState,
        offset: LyricsTimingOffset,
        tapSync: TapSyncUiState = TapSyncUiState(),
    ) = mapPhoneSyncState(lyrics, LyricsTimingOffsets(globalMs = offset.milliseconds), lyrics, tapSync)

    @Test
    fun `preview reuses canonical line and translation selection`() {
        val state = mapGlobalSync(lyrics(), LyricsTimingOffset(300L))
        assertEquals(300L, state.globalOffset.offsetMs)
        assertEquals(lines[0], state.phonePreview.previousLine)
        assertEquals(lines[1], state.phonePreview.currentLine)
        assertEquals(lines[2], state.phonePreview.nextLine)
        assertTrue(state.globalOffset.canAdvance && state.globalOffset.canDelay)
    }

    @Test
    fun `AA controls keep independent limits and display the combined total`() {
        val state = mapPhoneSyncState(lyrics(), LyricsTimingOffsets(30_000L, -30_000L), lyrics(index = 0))
        assertFalse(state.globalOffset.canAdvance)
        assertTrue(state.globalOffset.canDelay)
        assertTrue(state.androidAutoOffset.canAdvance)
        assertFalse(state.androidAutoOffset.canDelay)
        assertEquals(-30_000L, state.androidAutoOffset.offsetMs)
        assertEquals(0L, state.androidAutoTotalOffsetMs)
        assertEquals(lines[1], state.phonePreview.currentLine)
        assertEquals(lines[0], state.androidAutoPreview.currentLine)
    }

    @Test
    fun `AA changes move only car preview while global changes affect both`() {
        val track = Track(title = "Song", durationMs = 20_000L)
        val playback = PlaybackSnapshot(track = track, positionMs = 4_800L, status = PlaybackStatus.PAUSED)
        val document = LyricsDocument(listOf(
            TimedLyricLine("First", 0L),
            TimedLyricLine("Second", 5_000L),
            TimedLyricLine("Third", 10_000L),
        ))
        val ready = LyricsState.Ready(
            LyricsLookup(LyricsLookupId(1L), track, requireNotNull(playback.trackIdentity)), document,
        )
        val settings = LyricsTimingSettings({ LyricsTimingOffsets(300L, -200L) }, {})
        fun state(): SyncScreenUiState {
            fun project(offset: LyricsTimingOffset) = mapPhoneLyricsState(
                playback = playback,
                lyricsState = ready,
                plainLyricsAutoScrollEnabled = false,
                interactionMode = LyricsViewportInteractionMode.FOLLOW,
                currentMonotonicTimeMs = 20_000L,
                lyricsTimingOffset = offset,
            )
            val offsets = settings.offsets.value
            return mapPhoneSyncState(project(offsets.global), offsets, project(offsets.automotive))
        }

        assertEquals("Second", state().phonePreview.currentLine?.text)
        assertEquals("First", state().androidAutoPreview.currentLine?.text) // 4,800 + 300 - 200, once.
        assertEquals(100L, state().androidAutoTotalOffsetMs)
        settings.setAndroidAutoOffset(0L)
        assertEquals(state().phonePreview, state().androidAutoPreview)
        settings.setAndroidAutoOffset(500L)
        assertEquals("Second", state().phonePreview.currentLine?.text)
        assertEquals("Second", state().androidAutoPreview.currentLine?.text)
        settings.setOffset(-1_000L)
        assertEquals("First", state().phonePreview.currentLine?.text)
        assertEquals("First", state().androidAutoPreview.currentLine?.text)
        assertEquals(500L, state().androidAutoOffset.offsetMs)
        settings.resetToDefaults()
        assertEquals(0L, state().androidAutoTotalOffsetMs)
        assertEquals(state().phonePreview, state().androidAutoPreview)
        assertEquals(4_800L, playback.positionMs)
        assertEquals(5_000L, (document.lines[1] as TimedLyricLine).startMs)
    }

    @Test
    fun `no timed lyrics disables adjustment but retains saved offset for reset`() {
        val cases = listOf(
            lyrics(status = TrackCardLyricsStatus.IDLE) to SyncAvailability.NO_MEDIA,
            lyrics(status = TrackCardLyricsStatus.LOADING) to SyncAvailability.LOADING,
            lyrics(status = TrackCardLyricsStatus.NOT_FOUND) to SyncAvailability.UNAVAILABLE,
            lyrics(status = TrackCardLyricsStatus.FAILED) to SyncAvailability.UNAVAILABLE,
            lyrics(sync = LyricsSyncType.PLAIN) to SyncAvailability.PLAIN,
        )
        for ((input, expected) in cases) {
            val state = mapGlobalSync(input, LyricsTimingOffset(-500L))
            assertEquals(expected, state.availability)
            assertEquals(-500L, state.globalOffset.offsetMs)
            assertFalse(state.globalOffset.canAdvance || state.globalOffset.canDelay)
            assertFalse(state.androidAutoOffset.canAdvance || state.androidAutoOffset.canDelay)
            assertNull(state.androidAutoPreview.currentLine)
            assertNull(state.phonePreview.previousLine)
            assertNull(state.phonePreview.currentLine)
            assertNull(state.phonePreview.nextLine)
        }
    }

    @Test
    fun `before first line shows upcoming line and limits only disable outward actions`() {
        val before = mapGlobalSync(lyrics(index = null), LyricsTimingOffset(-30_000L))
        assertNull(before.phonePreview.currentLine)
        assertEquals(lines.first(), before.phonePreview.nextLine)
        assertTrue(before.globalOffset.canAdvance)
        assertFalse(before.globalOffset.canDelay)
        val end = mapGlobalSync(lyrics(index = 2), LyricsTimingOffset(30_000L))
        assertNull(end.phonePreview.nextLine)
        assertFalse(end.globalOffset.canAdvance)
        assertTrue(end.globalOffset.canDelay)
    }

    @Test
    fun `button actions immediately move paused preview and resetting restores original line`() {
        val track = Track(title = "Song", durationMs = 20_000L)
        val playback = PlaybackSnapshot(track = track, positionMs = 4_800L, status = PlaybackStatus.PAUSED)
        val document = LyricsDocument(listOf(
            TimedLyricLine("First", 0L),
            TimedLyricLine("Second", 5_000L),
        ))
        val ready = LyricsState.Ready(
            LyricsLookup(LyricsLookupId(1L), track, requireNotNull(playback.trackIdentity)), document,
        )
        var stored = 0L
        val settings = LyricsTimingSettings({ LyricsTimingOffsets(globalMs = stored) }, { stored = it.globalMs })
        fun preview() = mapPhoneLyricsState(
            playback = playback,
            lyricsState = ready,
            plainLyricsAutoScrollEnabled = true,
            interactionMode = LyricsViewportInteractionMode.FOLLOW,
            currentMonotonicTimeMs = 20_000L,
            lyricsTimingOffset = settings.offsets.value.global,
        ).let { mapGlobalSync(it, settings.offsets.value.global) }

        repeat(3) {
            settings.adjustBy(SyncAdjustment.ADVANCE_FINE.deltaMs())
            assertEquals("First", preview().phonePreview.currentLine?.text)
        }
        settings.adjustBy(SyncAdjustment.ADVANCE_FINE.deltaMs())
        assertEquals("Second", preview().phonePreview.currentLine?.text)
        settings.adjustBy(SyncAdjustment.DELAY_COARSE.deltaMs())
        assertEquals("First", preview().phonePreview.currentLine?.text)
        settings.adjustBy(SyncAdjustment.ADVANCE_COARSE.deltaMs())
        assertEquals("Second", preview().phonePreview.currentLine?.text)
        settings.adjustBy(SyncAdjustment.DELAY_FINE.deltaMs())
        assertEquals("First", preview().phonePreview.currentLine?.text)
        settings.resetToDefaults()
        assertEquals(0L, preview().globalOffset.offsetMs)
        assertEquals("First", preview().phonePreview.currentLine?.text)
        assertEquals(4_800L, playback.positionMs)
        assertEquals(5_000L, (document.lines[1] as TimedLyricLine).startMs)
    }

    @Test
    fun `tap panel uses frozen targets while existing fine controls remain available`() {
        val listening = TapSyncState.Listening(listOf(
            TapSyncTarget(0, 10_000L, "First"),
            TapSyncTarget(1, 20_000L, "Second"),
            TapSyncTarget(2, 30_000L, "Third"),
        ), completedTaps = 1)
        val ui = lyrics()
        val tapUi = mapPhoneTapSyncState(
            listening, PlaybackSnapshot(status = PlaybackStatus.PLAYING), LyricsState.Idle,
            ui, LyricsTimingOffset(500L), now = 100_000L,
        )
        val sync = mapGlobalSync(ui, LyricsTimingOffset(500L), tapUi)
        assertEquals(listOf("First", "Second", "Third"), sync.tapSync.lines)
        assertEquals(1, sync.tapSync.completedTaps)
        assertTrue(sync.tapSync.active)
        assertTrue(sync.globalOffset.canAdvance && sync.globalOffset.canDelay)
    }

    @Test
    fun `tap availability explains paused plain and end of song states`() {
        val track = Track(title = "Song")
        val playing = PlaybackSnapshot(track = track, status = PlaybackStatus.PLAYING, positionMs = 500L)
        val document = LyricsDocument(listOf(
            TimedLyricLine("First", 1_000L),
            TimedLyricLine("Second", 2_000L),
            TimedLyricLine("Third", 3_000L),
        ))
        val ready = LyricsState.Ready(
            LyricsLookup(LyricsLookupId(1), track, requireNotNull(playing.trackIdentity)), document,
        )
        fun availability(playback: PlaybackSnapshot = playing, ui: LyricsScreenUiState = lyrics()) =
            mapPhoneTapSyncState(TapSyncState.Idle, playback, ready, ui, LyricsTimingOffset.ZERO, 0).availability

        assertEquals(TapSyncAvailability.READY, availability())
        assertEquals(TapSyncAvailability.NEEDS_PLAYBACK, availability(playing.copy(status = PlaybackStatus.PAUSED)))
        assertEquals(TapSyncAvailability.NEEDS_TIMED_LYRICS, availability(ui = lyrics(sync = LyricsSyncType.PLAIN)))
        assertEquals(TapSyncAvailability.NOT_ENOUGH_LINES, availability(playing.copy(positionMs = 1_500L)))
    }

    @Test
    fun `completed and interrupted tap sessions explain the result`() {
        fun map(state: TapSyncState) = mapPhoneTapSyncState(
            state, PlaybackSnapshot(), LyricsState.Idle, lyrics(), LyricsTimingOffset(-500L), 0L,
        )
        assertEquals(-500L, map(TapSyncState.Completed(-500L)).savedOffsetMs)
        assertEquals(TapSyncMessage.SOURCE_CHANGED, map(TapSyncState.Stopped(TapSyncStopReason.SOURCE_CHANGED)).message)
        assertEquals(TapSyncMessage.OUT_OF_RANGE, map(TapSyncState.Stopped(TapSyncStopReason.OUT_OF_RANGE)).message)
        assertFalse(map(TapSyncState.Completed(-500L)).active)
    }
}
