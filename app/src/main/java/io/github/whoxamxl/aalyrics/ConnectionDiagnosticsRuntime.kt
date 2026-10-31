package io.github.whoxamxl.aalyrics

import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.os.SystemClock
import io.github.whoxamxl.aalyrics.core.model.PlaybackSnapshot
import io.github.whoxamxl.aalyrics.platform.media.MediaConnectionDiagnosticSink
import io.github.whoxamxl.aalyrics.platform.media.MediaConnectionDiagnostics
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class ConnectionDiagnosticState(
    val connecting: Boolean,
    val demandActive: Boolean,
    val fields: Map<String, String>,
)

/** Instantiated only by Debug builds; observes existing state without refreshing or rebinding it. */
internal class ConnectionDiagnosticsRuntime(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    private val recorder = ConnectionDiagnosticRecorder(
        store = ConnectionDiagnosticStore(context.noBackupFilesDir.resolve("connection-diagnostics")),
        scope = scope,
        monotonicClock = SystemClock::elapsedRealtime,
    )
    private val processFacts = mapOf(
        "package" to context.packageName,
        "version" to BuildConfig.VERSION_NAME,
        "version_code" to BuildConfig.VERSION_CODE.toString(),
        "pid" to Process.myPid().toString(),
        "manufacturer" to Build.MANUFACTURER,
        "model" to Build.MODEL,
        "android" to Build.VERSION.RELEASE,
        "sdk" to Build.VERSION.SDK_INT.toString(),
        "os_build" to Build.DISPLAY,
    )
    private var lastPlayback: Map<String, String>? = null

    init {
        recorder.record("process_start", processFacts)
        MediaConnectionDiagnostics.attach(MediaConnectionDiagnosticSink(recorder::record))
    }

    @Synchronized
    fun onPlayback(snapshot: PlaybackSnapshot) {
        val fields = mapOf(
            "source" to (snapshot.source?.id ?: "none"),
            "has_track" to (snapshot.track != null).toString(),
            "playback" to snapshot.status.name,
        )
        if (lastPlayback != fields) {
            lastPlayback = fields
            recorder.record("playback_forwarded", fields)
        }
    }

    fun onPhoneResume() {
        recorder.record("phone_resumed", systemFacts())
    }

    fun start(readState: () -> ConnectionDiagnosticState) {
        scope.launch(Dispatchers.Main.immediate) {
            val monitor = ConnectionStallMonitor()
            var lastState: ConnectionDiagnosticState? = null
            var lastHeartbeat = 0L
            while (isActive) {
                val state = readState()
                val now = SystemClock.elapsedRealtime()
                if (lastState != state) {
                    recorder.record("runtime_state", state.fields)
                    lastState = state
                }
                if (monitor.sample(now, state.connecting, state.demandActive)) {
                    recorder.capture(processFacts + state.fields + systemFacts() + MediaConnectionDiagnostics.snapshot())
                }
                if (state.demandActive && now - lastHeartbeat >= 60_000L) {
                    lastHeartbeat = now
                    recorder.record("connection_heartbeat", state.fields + systemFacts() + MediaConnectionDiagnostics.snapshot())
                }
                delay(5_000L)
            }
        }
    }

    fun export(uri: Uri, onComplete: (Boolean) -> Unit) {
        recorder.record("export_requested", systemFacts() + MediaConnectionDiagnostics.snapshot())
        scope.launch(Dispatchers.IO) {
            val succeeded = try {
                val output = context.contentResolver.openOutputStream(uri, "wt")
                    ?: error("No export destination")
                output.use { recorder.exportTo(it) }
                true
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                recorder.record("export_failed", mapOf("exception" to failure.javaClass.simpleName))
                false
            }
            withContext(Dispatchers.Main) { onComplete(succeeded) }
        }
    }

    private fun systemFacts(): Map<String, String> = mapOf(
        "notification_access" to fact { isNotificationAccessGranted(context) },
        "battery_optimization_exempt" to fact {
            context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName)
        },
        "background_restricted" to if (Build.VERSION.SDK_INT >= 28) {
            fact { context.getSystemService(ActivityManager::class.java).isBackgroundRestricted }
        } else "unsupported",
    )

    private fun fact(read: () -> Boolean): String = try {
        read().toString()
    } catch (failure: RuntimeException) {
        "unknown:${failure.javaClass.simpleName}"
    }
}
