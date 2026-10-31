package io.github.whoxamxl.aalyrics

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConnectionDiagnosticRecorderTest {
    @Test
    fun `export waits for records and a failed destination does not stop recording`() = runBlocking {
        val directory = Files.createTempDirectory("diagnostic-recorder").toFile()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val recorder = ConnectionDiagnosticRecorder(
                ConnectionDiagnosticStore(directory), scope,
                wallClock = { "2026-10-31T12:00:00Z" }, monotonicClock = { 1234L },
            )
            recorder.record("process_start", mapOf("model" to "test\ninjected\tvalue"))
            recorder.capture(mapOf("listener_connected" to "false"))
            assertFailsWith<IOException> {
                recorder.exportTo(object : OutputStream() {
                    override fun write(value: Int) { throw IOException("destination unavailable") }
                })
            }
            recorder.record("listener_connected")
            val output = ByteArrayOutputStream()
            recorder.exportTo(output)
            val text = output.toString("UTF-8")
            assertTrue(text.contains("process_start"))
            assertTrue(text.contains("connecting_timeout"))
            assertTrue(text.contains("listener_connected"))
            assertTrue(text.contains("uptime_ms=1234"))
            assertFalse(text.contains("\ninjected"))
        } finally {
            scope.cancel()
            directory.deleteRecursively()
        }
    }

    @Test
    fun `storage failures are counted without crashing the event caller`() = runBlocking {
        val directory = Files.createTempFile("diagnostic-invalid-directory", ".txt").toFile()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val recorder = ConnectionDiagnosticRecorder(
                ConnectionDiagnosticStore(directory), scope, monotonicClock = { 1L },
            )
            recorder.record("process_start")
            val output = ByteArrayOutputStream()
            recorder.exportTo(output)
            assertTrue(output.toString().contains("write_failures=1"))
        } finally {
            scope.cancel()
            directory.delete()
        }
    }
}
