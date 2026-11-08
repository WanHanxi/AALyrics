package io.github.whoxamxl.aalyrics

import io.github.whoxamxl.aalyrics.core.lyrics.CandidateSelectionPreferences
import io.github.whoxamxl.aalyrics.core.model.LyricsSyncType
import io.github.whoxamxl.aalyrics.core.model.Track
import io.github.whoxamxl.aalyrics.provider.api.LyricsRequest
import io.github.whoxamxl.aalyrics.provider.netease.NeteaseProvider
import io.github.whoxamxl.aalyrics.provider.selection.CrossProviderCandidateSelector
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NeteaseTranslationIntegrityTest {
    @Test
    fun `clean translation wins among matching records but cannot rescue a different recording`() = runBlocking {
        val track = Track("Synthetic Song", listOf("Test Artist"), "Test Album", 120_000)
        for (otherTitle in listOf(track.title, "Synthetic Song (Live)")) {
            val requestedLyrics = mutableListOf<String>()
            val client = OkHttpClient.Builder().addInterceptor { chain ->
                val request = chain.request()
                val body = if (request.url.encodedPath.contains("cloudsearch")) {
                    val songs = listOf(1 to otherTitle, 2 to track.title).joinToString(",") { (id, title) ->
                        """{"id":$id,"name":"$title","ar":[{"name":"Test Artist"}],"al":{"name":"Test Album"},"dt":120000}"""
                    }
                    """{"code":200,"result":{"songs":[$songs]}}"""
                } else {
                    val id = requireNotNull(request.url.queryParameter("id"))
                    requestedLyrics += id
                    val secondLine = if (id == "1") "第二测试行" else "损坏\uFFFD\uFFFD译文"
                    """{"code":200,"lrc":{"lyric":"[00:01.00]First original\n[00:50.00]Second original"},"tlyric":{"lyric":"[00:01.00]第一测试行\n[00:50.00]$secondLine"}}"""
                }
                Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("Test")
                    .body(body.toResponseBody("application/json; charset=utf-8".toMediaType())).build()
            }.build()
            try {
                val candidates = NeteaseProvider(client, "https://test.invalid/").search(LyricsRequest(track))
                val selected = assertNotNull(CrossProviderCandidateSelector().select(
                    track, candidates, CandidateSelectionPreferences(LyricsSyncType.WORD, "zh"),
                ))
                if (otherTitle == track.title) {
                    assertEquals(listOf("1", "2"), requestedLyrics)
                    assertEquals("1", selected.lyrics.attribution?.sourceId)
                    assertEquals(listOf("第一测试行", "第二测试行"), selected.lyrics.suppliedTranslation?.lines)
                    assertEquals(listOf("第一测试行", "损坏\uFFFD\uFFFD译文"),
                        candidates.single { it.lyrics.attribution?.sourceId == "2" }.lyrics.suppliedTranslation?.lines)
                } else {
                    assertEquals(listOf("2"), requestedLyrics)
                    assertEquals("2", selected.lyrics.attribution?.sourceId)
                    assertEquals(listOf("第一测试行", "损坏\uFFFD\uFFFD译文"), selected.lyrics.suppliedTranslation?.lines)
                    assertEquals(listOf("First original", "Second original"), selected.lyrics.lines.map { it.text })
                }
            } finally {
                client.dispatcher.executorService.shutdown()
                client.connectionPool.evictAll()
            }
        }
    }
}
