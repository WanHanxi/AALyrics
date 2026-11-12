package io.github.whoxamxl.aalyrics.provider.lrclib

import com.google.gson.Gson
import io.github.whoxamxl.aalyrics.core.model.Track
import io.github.whoxamxl.aalyrics.provider.api.LyricsRequest
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import java.util.concurrent.TimeUnit

/** Fixed 80 ms response delay isolates request-budget changes from service noise. */
object LookupBenchmark {
    @JvmStatic fun main(args: Array<String>) = runBlocking {
        val client = OkHttpClient()
        try {
            for (scenario in listOf("ordinary", "romanized", "fallback", "miss")) {
                val durations = mutableListOf<Double>()
                val requests = mutableListOf<Int>()
                val counts = mutableListOf<Int>()
                MockWebServer().use { server ->
                    val text = if (scenario == "romanized") "kimi no kokoro ni yume ga aru" else "We follow the evening light"
                    fun row(id: Int, native: Boolean = false) = mapOf(
                        "id" to id, "trackName" to "Evening Light", "artistName" to "Example Artist",
                        "albumName" to "Album", "duration" to 240, "instrumental" to false,
                        "syncedLyrics" to (1..12).joinToString("\n") { "[00:${it.toString().padStart(2, '0')}.00]${if (native) "星の道を歩いている" else text}" },
                    )
                    server.dispatcher = object : Dispatcher() {
                        override fun dispatch(request: RecordedRequest): MockResponse {
                            val exact = request.requestUrl!!.encodedPath.endsWith("/get")
                            val body = if (exact) row(1) else if (scenario == "miss") emptyList<Any>() else listOf(row(2, scenario == "romanized"))
                            return MockResponse().setHeadersDelay(80, TimeUnit.MILLISECONDS)
                                .setHeader("Content-Type", "application/json")
                                .setHeader("Connection", "close")
                                .setResponseCode(if (exact && scenario in listOf("fallback", "miss")) 404 else 200)
                                .setBody(Gson().toJson(body))
                        }
                    }
                    server.start()
                    val provider = LrcLibProvider(client, server.url("/api/").toString())
                    val request = LyricsRequest(Track("Evening Light", listOf("Example Artist"), "Album", 240_000))
                    repeat(9) { iteration ->
                        val before = server.requestCount
                        val start = System.nanoTime()
                        val result = provider.search(request)
                        val elapsed = (System.nanoTime() - start) / 1_000_000.0
                        if (iteration > 0) {
                            durations += elapsed
                            requests += server.requestCount - before
                            counts += result.size
                        }
                    }
                }
                durations.sort()
                println("LOOKUP case=$scenario delay_ms=80 p50_ms=${durations[4]} p95_ms=${durations[7]} requests=${requests.toSet()} emitted=${counts.toSet()}")
            }
        } finally {
            client.connectionPool.evictAll()
            client.dispatcher.executorService.shutdown()
        }
    }
}
