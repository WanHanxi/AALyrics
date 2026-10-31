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

    internal fun <Token> sessions(controllers: List<RuntimeMediaController<Token>>) {
        if (sink == null) return
        sessionQueries.incrementAndGet()
        changed("sessions_observed") {
            mapOf(
                "count" to controllers.size.toString(),
                "packages_playing" to controllers.take(16).joinToString(",") {
                    "${it.packageName}:${it.isPlaying}"
                },
            )
        }
    }

    fun snapshot(): Map<String, String> = mapOf(
        "service_created" to serviceCreated.toString(),
        "listener_connected" to listenerConnected.toString(),
        "session_observations" to sessionQueries.get().toString(),
        "notification_callbacks" to notifications.get().toString(),
    )
}
