package io.github.whoxamxl.aalyrics

import io.github.whoxamxl.aalyrics.core.model.PlaybackSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** One application-owned binding; it never owns the Android Auto host's binding. */
internal interface AutomotiveActivationBinding {
    fun bind(listener: Listener): Boolean
    fun unbind()

    interface Listener {
        fun onConnected()
        fun onDisconnected()
        fun onBindingLost(reason: String)
    }
}

/**
 * Main-thread owner of automatic media-session activation for one projection connection.
 *
 * Only real projection and eligible playback may trigger activation. Aggregate lyrics demand
 * cannot be an input: creating the browser service itself adds demand and would form a cycle.
 * Once triggered, pauses and metadata gaps retain the binding until projection disconnects.
 */
internal class AndroidAutoActivation(
    private val scope: CoroutineScope,
    private val createBinding: () -> AutomotiveActivationBinding,
    private val record: (String, Map<String, String>) -> Unit = { _, _ -> },
) {
    private val mutableSessionActivationRequest = MutableStateFlow<Long?>(null)
    /** Replayed to a browser service that may outlive our binding or the projection. */
    val sessionActivationRequest = mutableSessionActivationRequest.asStateFlow()
    private var nextSessionActivationRequest = 0L
    private var projectionConnected = false
    private var eligiblePlayback = false
    private var activationRequested = false
    private var attempts = 0
    private var binding: AutomotiveActivationBinding? = null
    private var retryJob: Job? = null

    fun onProjectionChanged(connected: Boolean) {
        if (projectionConnected == connected) return
        projectionConnected = connected
        if (connected) {
            reconcile()
        } else {
            reset("projection_disconnected")
        }
    }

    fun onPlaybackSnapshot(snapshot: PlaybackSnapshot, sourceEligible: Boolean) {
        eligiblePlayback = sourceEligible && snapshot.isPlaying &&
            snapshot.track != null && snapshot.source != null
        reconcile()
    }

    fun stop() {
        projectionConnected = false
        eligiblePlayback = false
        reset("stopped")
    }

    private fun reconcile() {
        if (!scope.isActive || !projectionConnected || (!activationRequested && !eligiblePlayback)) return
        if (!activationRequested) {
            activationRequested = true
            val request = ++nextSessionActivationRequest
            record("automotive_activation_request", mapOf("request" to request.toString()))
            mutableSessionActivationRequest.value = request
        }
        if (binding != null || retryJob != null || attempts >= MAX_ATTEMPTS) return

        val job = scope.launch(start = CoroutineStart.LAZY) {
            if (attempts > 0) delay(RETRY_DELAY_MS)
            if (isActive && projectionConnected) requestBinding()
        }
        retryJob = job
        job.invokeOnCompletion {
            if (retryJob === job) {
                retryJob = null
                // A failed request gets a bounded retry, even if it failed synchronously.
                reconcile()
            }
        }
        job.start()
    }

    private fun requestBinding() {
        attempts++
        val requested = createBinding()
        binding = requested
        record("automotive_activation_bind_requested", mapOf("attempt" to attempts.toString()))
        val listener = object : AutomotiveActivationBinding.Listener {
            override fun onConnected() {
                if (binding === requested) record("automotive_activation_connected", emptyMap())
            }

            override fun onDisconnected() {
                // Android retains this connection and reconnects it; do not double-bind here.
                if (binding === requested) record("automotive_activation_disconnected", emptyMap())
            }

            override fun onBindingLost(reason: String) {
                if (binding !== requested) return
                record("automotive_activation_binding_lost", mapOf("reason" to reason))
                releaseBinding(reason)
                reconcile()
            }
        }
        try {
            if (requested.bind(listener)) {
                record("automotive_activation_bind_accepted", emptyMap())
            } else if (binding === requested) {
                record("automotive_activation_bind_failed", mapOf("reason" to "rejected"))
                releaseBinding("rejected")
            }
        } catch (failure: RuntimeException) {
            record("automotive_activation_bind_failed", mapOf("exception" to failure.javaClass.simpleName))
            if (binding === requested) releaseBinding("exception")
        }
    }

    private fun reset(reason: String) {
        mutableSessionActivationRequest.value?.let { request ->
            record("automotive_activation_request_cleared", mapOf("request" to request.toString(), "reason" to reason))
        }
        mutableSessionActivationRequest.value = null
        activationRequested = false
        attempts = 0
        val job = retryJob
        retryJob = null
        job?.cancel()
        releaseBinding(reason)
    }

    private fun releaseBinding(reason: String) {
        val released = binding ?: return
        binding = null
        try {
            released.unbind()
        } catch (failure: RuntimeException) {
            record("automotive_activation_unbind_failed", mapOf("exception" to failure.javaClass.simpleName))
        }
        record("automotive_activation_released", mapOf("reason" to reason))
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
        const val RETRY_DELAY_MS = 1_000L
    }
}
