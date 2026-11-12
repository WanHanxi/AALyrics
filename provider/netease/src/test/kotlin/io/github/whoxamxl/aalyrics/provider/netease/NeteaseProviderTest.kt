package io.github.whoxamxl.aalyrics.provider.netease

import com.google.gson.Gson
import io.github.whoxamxl.aalyrics.core.model.*
import io.github.whoxamxl.aalyrics.provider.api.LyricsRequest
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.mockwebserver.*
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.*

class NeteaseProviderTest {
    private val track = Track("Synthetic Song", listOf("Test Artist"), "Test Album", 120_000)
    private val request = LyricsRequest(track)

    @Test fun `catalog artist aliases survive local filtering and candidate handoff`() = withServer { server, provider ->
        val aliased = song() + mapOf("ar" to listOf(mapOf("name" to "乐队", "alias" to listOf("Example Band"))))
        server.json(search(aliased))
        server.json(lyrics("[00:01.00]Original test", "[00:01.00]测试译文"))
        val result = provider.search(LyricsRequest(track.copy(artists = listOf("樂隊 Example Band")))).single()
        assertEquals(listOf("乐队"), result.matchedTrack.artists)
        assertEquals(mapOf("乐队" to listOf("Example Band")), result.evidence.artistAliases)
        assertEquals(2, server.requestCount)
    }

    @Test fun `same response supplies original translation and contributor attribution`() = withServer { server, provider ->
        server.json(search(song()))
        server.json(lyrics("[00:01.00]First test line\n[00:02.50]Second test line", "[00:01.00]第一测试行\n[00:02.50]第二测试行"))
        val candidate = provider.search(request).single()
        assertEquals(track, candidate.matchedTrack)
        assertEquals(listOf(1000L, 2500L), candidate.lyrics.lines.map { (it as TimedLyricLine).startMs })
        val translation = assertNotNull(candidate.lyrics.suppliedTranslation)
        assertEquals(listOf("第一测试行", "第二测试行"), translation.lines)
        assertEquals("zh", translation.languageTag)
        assertEquals("1", translation.attribution.sourceId)
        assertEquals(listOf("Test contributor"), translation.attribution.contributors)
        assertEquals(listOf("[by:Test credit]"), translation.attribution.notices)
        assertEquals("Synthetic Song Test Artist", server.takeRequest().requestUrl!!.queryParameter("s"))
        val lyricRequest = server.takeRequest()
        assertEquals("/api/song/lyric/v1", lyricRequest.requestUrl!!.encodedPath)
        assertNull(lyricRequest.getHeader("Cookie"))
    }

    @Test fun `unmatched timed translation rights notice is retained in attribution`() = withServer { server, provider ->
        server.json(search(song()))
        server.json(lyrics("[00:01.00]Original test", "[00:01.00]测试译文\n[00:00.00]Copyright: synthetic rights holder"))
        val notices = provider.search(request).single().lyrics.suppliedTranslation!!.attribution.notices
        assertTrue("Copyright: synthetic rights holder" in notices)
    }

    @Test fun `timestamp gaps and ambiguous duplicates never shift translation onto another row`() = withServer { server, provider ->
        server.json(search(song()))
        server.json(lyrics(
            "[00:01.00]One\n[00:02.00]Two\n[00:03.00]Three\n[00:04.00]Four\n[00:04.00]Also four",
            "[00:01.00]一\n[00:02.10]错误时间\n[00:03.00]三甲\n[00:03.00]三乙\n[00:04.00]四",
        ))
        assertEquals(listOf("一", null, null, null, null), provider.search(request).single().lyrics.suppliedTranslation?.lines)
    }

    @Test fun `missing or malformed translated lyrics preserve valid original`() = withServer { server, provider ->
        server.json(search(song()))
        server.json(lyrics("[00:01.00]Original test", "no timing"))
        val document = provider.search(request).single().lyrics
        assertEquals("Original test", document.lines.single().text)
        assertNull(document.suppliedTranslation)
    }

    @Test fun `literal or escaped replacement characters do not discard usable supplied translations`() {
        for (escaped in listOf(false, true)) withServer { server, provider ->
            server.json(search(song()))
            val json = Gson().toJson(lyrics(
                "[00:01.00]First original\n[00:02.00]Second original",
                "[00:01.00]正常译文\n[00:02.00]损坏\uFFFD\uFFFD译文",
            ))
            server.enqueue(MockResponse().setBody(if (escaped) json.replace("\uFFFD", "\\ufffd") else json))
            val document = provider.search(request).single().lyrics
            assertEquals(listOf("First original", "Second original"), document.lines.map { it.text })
            assertEquals(listOf("正常译文", "损坏\uFFFD\uFFFD译文"), document.suppliedTranslation?.lines)
        }
    }

    @Test fun `valid non ASCII text punctuation and supplementary Unicode remain unchanged`() = withServer { server, provider ->
        server.json(search(song()))
        val translated = "测试—𠮷 🎵 café"
        server.json(lyrics("[00:01.00]Unicode test", "[00:01.00]$translated"))
        assertEquals(listOf(translated), provider.search(request).single().lyrics.suppliedTranslation?.lines)
    }

    @Test fun `version mismatch is rejected before fetching lyrics and title search is bounded`() = withServer { server, provider ->
        server.json(search(song(title = "Synthetic Song (Live)")))
        server.json(search(song()))
        server.json(lyrics("[00:01.00]Original", "[00:01.00]测试"))
        assertEquals("1", provider.search(request).single().lyrics.attribution?.sourceId)
        server.takeRequest()
        assertEquals("Synthetic Song", server.takeRequest().requestUrl!!.queryParameter("s"))
        assertEquals(3, server.requestCount)
    }

    @Test fun `metadata ordering and request cap do not blindly use search position`() = withServer { server, provider ->
        server.json(search(song(9, "Unrelated"), song(8, "Synthetic Song（伴奏）"), song(3), song(1), song(2), song(4)))
        repeat(3) { server.json(lyrics("[00:01.00]Original", "")) }
        val results = provider.search(request)
        assertEquals(listOf("1", "2", "3"), results.map { it.lyrics.attribution?.sourceId })
        assertEquals(4, server.requestCount)
    }

    @Test fun `API errors and invalid regional responses are operational failures`() {
        for (body in listOf("{\"code\":301}", "{\"code\":200,\"result\":\"restricted\",\"abroad\":true}", "<html>error</html>")) {
            withServer { server, provider ->
                server.enqueue(MockResponse().setBody(body))
                assertFailsWith<IOException> { provider.search(request) }
                assertEquals(1, server.requestCount)
            }
        }
    }

    @Test fun `HTTP error is not silently treated as no lyrics`() = withServer { server, provider ->
        server.enqueue(MockResponse().setResponseCode(503))
        assertFailsWith<IOException> { provider.search(request) }
    }

    @Test fun `no lyrics and malformed sibling metadata do not create a candidate`() = withServer { server, provider ->
        server.json(mapOf("code" to 200, "result" to mapOf("songs" to listOf(mapOf("id" to "bad"), song()))))
        server.json(mapOf("code" to 200, "nolyric" to true, "lrc" to mapOf("lyric" to "[00:00.00]Instrumental placeholder")))
        assertTrue(provider.search(request).isEmpty())
    }

    @Test fun `one failed lyric fetch retains a successful sibling`() = withServer { server, provider ->
        server.json(search(song(1), song(2)))
        server.enqueue(MockResponse().setResponseCode(503))
        server.json(lyrics("[00:01.00]Original", ""))
        assertEquals("2", provider.search(request).single().lyrics.attribution?.sourceId)
    }

    @Test fun `cancellation cancels underlying HTTP call`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val cancelled = CountDownLatch(1)
            val client = OkHttpClient.Builder().eventListener(object : EventListener() {
                override fun canceled(call: Call) { cancelled.countDown() }
            }).build()
            val provider = NeteaseProvider(client, server.url("/").toString())
            val job = launch(Dispatchers.Default) { provider.search(request) }
            assertNotNull(withContext(Dispatchers.IO) { server.takeRequest(3, TimeUnit.SECONDS) })
            job.cancelAndJoin()
            assertTrue(cancelled.await(3, TimeUnit.SECONDS))
        }
    }

    private fun song(id: Int = 1, title: String = track.title) = mapOf(
        "id" to id, "name" to title, "ar" to listOf(mapOf("name" to "Test Artist")),
        "al" to mapOf("name" to "Test Album"), "dt" to 120_000,
    )
    private fun search(vararg songs: Map<String, Any>) = mapOf("code" to 200, "result" to mapOf("songs" to songs))
    private fun lyrics(original: String, translation: String) = mapOf(
        "code" to 200, "lrc" to mapOf("lyric" to original),
        "tlyric" to mapOf("lyric" to "[by:Test credit]\n$translation"),
        "transUser" to mapOf("nickname" to "Test contributor"),
    )
    private fun MockWebServer.json(value: Any) { enqueue(MockResponse().setBody(Gson().toJson(value))) }
    private fun withServer(block: suspend (MockWebServer, NeteaseProvider) -> Unit) = runBlocking {
        MockWebServer().use { server -> block(server, NeteaseProvider(baseUrl = server.url("/").toString())) }
    }
}
