package io.github.whoxamxl.aalyrics

/** One capture per continuous, demanded Connecting episode, using a monotonic clock. */
internal class ConnectionStallMonitor(private val timeoutMs: Long = 30_000L) {
    private var startedAt: Long? = null
    private var captured = false

    fun sample(nowMs: Long, connecting: Boolean, demandActive: Boolean): Boolean {
        if (!connecting || !demandActive) {
            startedAt = null
            captured = false
            return false
        }
        val start = startedAt ?: nowMs.also { startedAt = it }
        if (captured || nowMs - start < timeoutMs) return false
        captured = true
        return true
    }
}
