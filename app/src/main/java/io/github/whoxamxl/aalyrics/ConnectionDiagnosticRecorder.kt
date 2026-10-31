package io.github.whoxamxl.aalyrics

import java.io.OutputStream
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/** A bounded, ordered IO queue shared by platform events, stall captures and exports. */
internal class ConnectionDiagnosticRecorder(
    private val store: ConnectionDiagnosticStore,
    scope: CoroutineScope,
    private val wallClock: () -> String = { Instant.now().toString() },
    private val monotonicClock: () -> Long,
) {
    private sealed interface Command {
        data class Record(val line: String, val incident: Boolean = false) : Command
        data class Export(val output: OutputStream, val done: CompletableDeferred<Unit>) : Command
    }

    private val commands = Channel<Command>(256)
    private val dropped = AtomicLong()
    private var writeFailures = 0L

    init {
        scope.launch(Dispatchers.IO) {
            for (command in commands) {
                when (command) {
                    is Command.Record -> try {
                        if (command.incident) store.captureIncident(command.line) else store.append(command.line)
                    } catch (_: Exception) {
                        writeFailures++
                    }
                    is Command.Export -> try {
                        command.output.write(
                            "dropped_events=${dropped.get()} write_failures=$writeFailures\n".toByteArray(),
                        )
                        store.exportTo(command.output)
                        command.done.complete(Unit)
                    } catch (failure: Exception) {
                        command.done.completeExceptionally(failure)
                    }
                }
            }
        }
    }

    fun record(event: String, fields: Map<String, String> = emptyMap()) {
        if (!commands.trySend(Command.Record(line(event, fields))).isSuccess) dropped.incrementAndGet()
    }

    suspend fun capture(fields: Map<String, String>) {
        commands.send(Command.Record(line("connecting_timeout", fields), incident = true))
    }

    /** The export is a queue barrier: all earlier accepted records are written first. */
    suspend fun exportTo(output: OutputStream) {
        val done = CompletableDeferred<Unit>()
        commands.send(Command.Export(output, done))
        done.await()
    }

    private fun line(event: String, fields: Map<String, String>): String = buildString {
        append(wallClock()).append(" uptime_ms=").append(monotonicClock())
        append(' ').append(safe(event, 64))
        fields.entries.take(40).forEach { (key, value) ->
            append('\t').append(safe(key, 64)).append('=').append(safe(value, 512))
        }
    }

    private fun safe(value: String, limit: Int) = value.take(limit).map {
        if (it in ' '..'~') it else '?'
    }.joinToString("")
}
