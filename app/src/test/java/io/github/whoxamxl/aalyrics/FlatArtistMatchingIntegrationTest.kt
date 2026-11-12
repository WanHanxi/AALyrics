package io.github.whoxamxl.aalyrics

import io.github.whoxamxl.aalyrics.core.lyrics.CandidateSelectionPreferences
import io.github.whoxamxl.aalyrics.core.model.Track
import io.github.whoxamxl.aalyrics.provider.api.LyricsRequest
import io.github.whoxamxl.aalyrics.provider.lrclib.LrcLibProvider
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

class FlatArtistMatchingIntegrationTest {
    @Test
    fun `phone shaped credits survive provider filtering and select supplied translation`() = runBlocking {
        // Android's MediaSession adapter retains ARTIST as one entry, unlike playlist APIs.
        val track = Track("Northern Light", listOf("Alpha & Endspiel, Rem Vega"), "Album", 120_000)
        val original = """[00:01.00]Synthetic original one\n[00:50.00]Synthetic original two"""
        val requests = mutableListOf<String>()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            requests += request.url.encodedPath
            val body = when (request.url.encodedPath) {
                "/api/get" -> """{
                    "id":1,"trackName":"Northern Light","artistName":"Alpha & Endspiel/Rem Vega",
                    "albumName":"Album","duration":120,"syncedLyrics":"$original","instrumental":false
                }"""
                "/api/cloudsearch/pc" -> """{"code":200,"result":{"songs":[{
                    "id":2,"name":"Northern Light",
                    "ar":[{"name":"Alpha"},{"name":"Эндшпиль"},{"name":"Рем Вега"}],
                    "al":{"name":"Album"},"dt":120100
                }]}}"""
                "/api/song/lyric/v1" -> """{
                    "code":200,"lrc":{"lyric":"$original"},
                    "tlyric":{"lyric":"[00:01.00]第一测试行\n[00:50.00]第二测试行"}
                }"""
                else -> error("Unexpected request: ${request.url.encodedPath}")
            }
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("Test")
                .body(body.toResponseBody("application/json; charset=utf-8".toMediaType())).build()
        }.build()
        try {
            val request = LyricsRequest(track)
            val candidates = LrcLibProvider(client, "https://test.invalid/api/").search(request) +
                NeteaseProvider(client, "https://test.invalid/").search(request)
            assertEquals(listOf("lrclib", "netease"), candidates.map { it.providerId.value })
            assertEquals(listOf("/api/get", "/api/cloudsearch/pc", "/api/song/lyric/v1"), requests)
            val result = CrossProviderCandidateSelector().selectWithDiagnostics(
                track, candidates, CandidateSelectionPreferences(preferredTranslationLanguage = "zh"),
            )
            val selected = assertNotNull(result.candidate)
            assertEquals("netease", selected.providerId.value)
            assertEquals(0.985, result.diagnostics.single { it.providerId == "netease" }.metadataScore)
            assertEquals(listOf("第一测试行", "第二测试行"), selected.lyrics.suppliedTranslation?.lines)
            assertEquals(listOf("Alpha & Endspiel, Rem Vega"), track.artists)
        } finally {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
        }
    }
}
