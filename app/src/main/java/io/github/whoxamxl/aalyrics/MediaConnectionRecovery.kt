package io.github.whoxamxl.aalyrics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal enum class MediaConnectionRecoveryState {
    IDLE,
    CONNECTING,
    CONNECTED,
    ACCESS_REQUIRED,
    TIMED_OUT,
}

/** Main-thread owner of binding recovery. Playback/session availability is deliberately unrelated. */
internal class MediaConnectionRecovery(
    private val scope: CoroutineScope,
    private val hasAccess: () -> Boolean,
    private val requestRebind: (resetBinding: Boolean) -> Unit,
    private val record: (String, Map<String, String>) -> Unit = { _, _ -> },
) {
    private val mutableState = MutableStateFlow(MediaConnectionRecoveryState.IDLE)
    val state = mutableState.asStateFlow()
    private var demandActive = false
    private var listenerConnected = false
    private var recoveryJob: Job? = null

    fun setDemandActive(active: Boolean) {
        if (demandActive == active) return
        demandActive = active
        if (!active) {
            cancelRecovery()
            publish(if (listenerConnected) MediaConnectionRecoveryState.CONNECTED else MediaConnectionRecoveryState.IDLE)
        } else {
            reconcile()
        }
    }

    /** Only actual Android service callbacks can report a connected binding. */
    fun onConnectionChanged(connected: Boolean) {
        listenerConnected = connected
        if (connected) {
            cancelRecovery()
            publish(MediaConnectionRecoveryState.CONNECTED)
        } else {
            // A real disconnect permits a new bounded episode after a previous connection.
            if (state.value == MediaConnectionRecoveryState.CONNECTED) {
                publish(MediaConnectionRecoveryState.IDLE)
            }
            reconcile()
        }
    }

    /** Returning from system Settings must re-read access even if process demand stayed active. */
    fun refreshAccess() = reconcile()

    fun reconnect() {
        if (!demandActive || listenerConnected || recoveryJob?.isActive == true ||
            state.value != MediaConnectionRecoveryState.TIMED_OUT
        ) return
        record("recovery_manual", emptyMap())
        publish(MediaConnectionRecoveryState.IDLE)
        reconcile(resetBinding = true)
    }

    fun stop() {
        demandActive = false
        cancelRecovery()
    }

    private fun reconcile(resetBinding: Boolean = false) {
        if (!demandActive) return
        if (!hasAccess()) {
            cancelRecovery()
            publish(MediaConnectionRecoveryState.ACCESS_REQUIRED)
            return
        }
        if (listenerConnected) {
            cancelRecovery()
            publish(MediaConnectionRecoveryState.CONNECTED)
            return
        }
        if (recoveryJob?.isActive == true || state.value == MediaConnectionRecoveryState.TIMED_OUT) return

        publish(MediaConnectionRecoveryState.CONNECTING)
        // Assign before starting: a synchronous connection callback must be able to cancel this job.
        val job = scope.launch(start = CoroutineStart.LAZY) {
            val waits = longArrayOf(5_000L, 10_000L, 15_000L)
            for ((index, waitMs) in waits.withIndex()) {
                if (!isActive || !demandActive || listenerConnected) return@launch
                if (!hasAccess()) {
                    publish(MediaConnectionRecoveryState.ACCESS_REQUIRED)
                    return@launch
                }
                record("recovery_attempt", mapOf("attempt" to (index + 1).toString()))
                // Only a manual retry after timeout may reset the system binding, once per episode.
                requestRebind(resetBinding && index == 0)
                delay(waitMs)
            }
            if (isActive && demandActive && !listenerConnected) {
                publish(
                    if (hasAccess()) MediaConnectionRecoveryState.TIMED_OUT
                    else MediaConnectionRecoveryState.ACCESS_REQUIRED,
                )
            }
        }
        recoveryJob = job
        job.start()
    }

    private fun cancelRecovery() {
        recoveryJob?.cancel()
        recoveryJob = null
    }

    private fun publish(value: MediaConnectionRecoveryState) {
        if (mutableState.value == value) return
        mutableState.value = value
        record("recovery_state", mapOf("state" to value.name))
    }
}
