package io.github.whoxamxl.aalyrics.platform.media

import java.util.concurrent.atomic.AtomicLong

/** Optional application-owned diagnostics. Never accepts notifications or track metadata. */
fun interface MediaConnectionDiagnosticSink {
    fun record(event: String, fields: Map<String, String>)
}

object MediaConnectionDiagnostics {
    @Volatile private var sink: MediaConnectionDiagnosticSink? = null
    @Volatile private var serviceCreated = false
    @Volatile private var listenerConnected = false
    private val sessionQueries = AtomicLong()
    private val notifications = AtomicLong()
    private val lastValues = mutableMapOf<String, Map<String, String>>()

    fun attach(sink: MediaConnectionDiagnosticSink) {
        this.sink = sink
    }

    fun detach() {
        sink = null
        synchronized(lastValues) { lastValues.clear() }
    }

    /** Lazy and best-effort: diagnostics cannot introduce a new media-runtime failure. */
    fun record(event: String, fields: () -> Map<String, String> = { emptyMap() }) {
        val target = sink ?: return
        try {
            target.record(event, fields())
        } catch (_: RuntimeException) {
            // Observability must not affect session selection or playback.
        }
    }

    internal fun changed(event: String, fields: () -> Map<String, String>) {
        if (sink == null) return
        try {
            val values = fields()
            val changed = synchronized(lastValues) { lastValues.put(event, values) != values }
            if (changed) record(event) { values }
        } catch (_: RuntimeException) {
            // A diagnostic read must not introduce another Binder failure path.
        }
    }

    internal fun lifecycle(event: String) {
        if (sink == null) return
        when (event) {
            "listener_created" -> serviceCreated = true
            "listener_connected" -> listenerConnected = true
            "listener_disconnected" -> listenerConnected = false
            "listener_destroyed" -> {
                serviceCreated = false
                listenerConnected = false
            }
        }
        record(event)
    }

    internal fun notificationPosted() {
        if (sink != null) notifications.incrementAndGet()
    }

    /** Original system order, before AALyrics' own source-selection policy runs. */
    internal fun <Token> sessions(controllers: List<RuntimeMediaController<Token>>, reason: String) {
        if (sink == null) return
        val sequence = sessionQueries.incrementAndGet()
        // Identical callbacks matter: a quick active-state flip may leave the list unchanged.
        record("sessions_observed") {
            buildMap {
                put("reason", reason)
                put("sequence", sequence.toString())
                put("count", controllers.size.toString())
                put("omitted", (controllers.size - 16).coerceAtLeast(0).toString())
                controllers.take(16).forEachIndexed { rank, controller ->
                    put("session_$rank", controllerFields(controller).entries.joinToString(" ") { (key, value) ->
                        "$key=$value"
                    })
                }
            }
        }
    }

    internal fun <Token> controllerEvent(
        event: String,
        controller: RuntimeMediaController<Token>,
        deduplicate: Boolean = false,
    ) {
        if (deduplicate) changed(event) { controllerFields(controller) }
        else record(event) { controllerFields(controller) }
    }

    /** Correlates the automotive session with framework controllers without storing a raw token. */
    fun sessionEvent(
        event: String,
        token: Any,
        fields: Map<String, String>,
        deduplicate: Boolean = false,
    ) {
        val values = { fields + ("token_hash" to tokenFingerprint(token)) }
        if (deduplicate) changed(event, values) else record(event, values)
    }

    internal fun tokenFingerprint(token: Any?): String =
        token?.hashCode()?.toUInt()?.toString(16) ?: "none"

    private fun <Token> controllerFields(controller: RuntimeMediaController<Token>): Map<String, String> = buildMap {
        try {
            put("package", controller.packageName)
            put("token_hash", tokenFingerprint(controller.token))
            putAll(controller.diagnosticPlayback())
        } catch (failure: RuntimeException) {
            // A vanished controller must not hide the remaining entries or affect selection.
            put("read_error", failure.javaClass.simpleName)
        }
    }

    fun snapshot(): Map<String, String> = mapOf(
        "service_created" to serviceCreated.toString(),
        "listener_connected" to listenerConnected.toString(),
        "session_observations" to sessionQueries.get().toString(),
        "notification_callbacks" to notifications.get().toString(),
    )
}
