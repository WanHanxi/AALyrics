package io.github.whoxamxl.aalyrics

import android.app.Application
import androidx.car.app.connection.CarConnection
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import io.github.whoxamxl.aalyrics.platform.media.MediaConnectionDiagnostics

/** Android lifecycle adapters for the two process-level lyrics demand sources. */
internal class LyricsDemandLifecycle(
    application: Application,
    private val gate: LyricsDemandGate,
) {
    private val processLifecycle = ProcessLifecycleOwner.get().lifecycle
    private val carConnectionType = CarConnection(application).type
    private val phoneObserver = PhoneProcessDemandObserver(gate)
    private val projectionObserver = Observer<Int> { connectionType ->
        MediaConnectionDiagnostics.record("automotive_projection") {
            mapOf("connection_type" to connectionType.toString())
        }
        gate.setAutomotiveProjectionConnected(
            connectionType == CarConnection.CONNECTION_TYPE_PROJECTION,
        )
    }
    private var started = false

    fun start() {
        if (started) return
        started = true
        processLifecycle.addObserver(phoneObserver)
        carConnectionType.observeForever(projectionObserver)
    }

    fun stop() {
        if (!started) return
        started = false
        processLifecycle.removeObserver(phoneObserver)
        carConnectionType.removeObserver(projectionObserver)
        gate.setPhoneProcessForeground(false)
        gate.setAutomotiveProjectionConnected(false)
    }
}

/** ProcessLifecycleOwner deliberately spans brief Activity recreation gaps. */
internal class PhoneProcessDemandObserver(
    private val gate: LyricsDemandGate,
) : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        MediaConnectionDiagnostics.record("phone_process_foreground")
        gate.setPhoneProcessForeground(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        MediaConnectionDiagnostics.record("phone_process_background")
        gate.setPhoneProcessForeground(false)
    }
}
