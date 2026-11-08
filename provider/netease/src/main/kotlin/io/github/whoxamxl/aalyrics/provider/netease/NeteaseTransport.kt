package io.github.whoxamxl.aalyrics.provider.netease

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class NeteaseTransport(private val client: OkHttpClient, baseUrl: String) {
    private val base = baseUrl.toHttpUrl()

    suspend fun search(query: String): JsonObject = get("api/cloudsearch/pc", mapOf(
        "s" to query, "type" to "1", "offset" to "0", "total" to "false", "limit" to "10",
    ))

    suspend fun lyrics(id: String): JsonObject = get("api/song/lyric/v1", mapOf(
        "id" to id, "cp" to "false", "lv" to "0", "tv" to "0", "rv" to "0",
        "kv" to "0", "yv" to "0", "ytv" to "0", "yrv" to "0",
    ))

    private suspend fun get(path: String, parameters: Map<String, String>): JsonObject {
        val url = base.newBuilder().addPathSegments(path).apply {
            parameters.forEach { (name, value) -> addQueryParameter(name, value) }
        }.build()
        val body = execute(url)
        val payload = try { JsonParser.parseString(body).asJsonObject } catch (error: Exception) {
            throw IOException("NetEase invalid JSON response", error)
        }
        val code = payload.number("code")
        if (code != 200L) throw IOException("NetEase API code $code")
        // HTTP 200 with a regional/service error is not proof that the song is absent.
        if (path.endsWith("cloudsearch/pc") && payload.obj("result") == null) {
            throw IOException("NetEase search response missing result object")
        }
        return payload
    }

    private suspend fun execute(url: HttpUrl): String = suspendCancellableCoroutine { continuation ->
        val call = client.newCall(Request.Builder().url(url)
            .header("User-Agent", "AALyrics (https://github.com/WanHanxi/AALyrics)")
            .header("Accept", "application/json").build())
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { continuation.resumeWithException(e) }
            override fun onResponse(call: Call, response: Response) {
                try {
                    val text = response.use {
                        if (!it.isSuccessful) throw IOException("NetEase HTTP ${it.code}")
                        val source = it.body?.source() ?: throw IOException("NetEase empty response")
                        if (source.request(MAX_RESPONSE_BYTES + 1)) throw IOException("NetEase response too large")
                        source.readUtf8()
                    }
                    continuation.resume(text)
                } catch (error: Exception) {
                    continuation.resumeWithException(error)
                }
            }
        })
    }

    companion object {
        private const val MAX_RESPONSE_BYTES = 2_000_000L
        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS).readTimeout(8, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS).build()
    }
}

internal fun JsonObject.obj(name: String): JsonObject? = get(name)?.takeIf { it.isJsonObject }?.asJsonObject
internal fun JsonObject.array(name: String): List<JsonElement> =
    get(name)?.takeIf { it.isJsonArray }?.asJsonArray?.toList().orEmpty()
internal fun JsonObject.text(name: String): String? = get(name)?.takeIf {
    it.isJsonPrimitive && it.asJsonPrimitive.isString
}?.asString
internal fun JsonObject.number(name: String): Long? = try {
    get(name)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asBigDecimal?.longValueExact()
} catch (_: ArithmeticException) { null }
