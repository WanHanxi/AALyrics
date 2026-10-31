package io.github.whoxamxl.aalyrics

import java.io.ByteArrayOutputStream
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConnectionDiagnosticStoreTest {
    @Test
    fun `rotation is bounded and a new process can export earlier events and incidents`() {
        val directory = Files.createTempDirectory("connection-diagnostics").toFile()
        try {
            val store = ConnectionDiagnosticStore(directory, 64, 3, 96, 2)
            store.append("listener_connected")
            store.captureIncident("connecting_timeout")
            repeat(100) { store.append("later-$it " + "x".repeat(20)) }
            val exported = ByteArrayOutputStream()
            ConnectionDiagnosticStore(directory, 64, 3, 96, 2).exportTo(exported)
            val text = exported.toString("UTF-8")
            assertTrue(text.contains("listener_connected"), "Incident must survive event rotation")
            assertTrue(text.contains("connecting_timeout"))
            assertTrue(text.contains("later-99"))
            assertFalse(text.contains("later-0 "))
            assertTrue(directory.listFiles()!!.sumOf { it.length() } <= 3 * 64 + 2 * 96)
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun `oversize records and repeated incidents cannot exceed disk budget`() {
        val directory = Files.createTempDirectory("connection-diagnostics").toFile()
        try {
            val store = ConnectionDiagnosticStore(directory, 64, 2, 80, 2)
            repeat(20) { store.captureIncident("episode-$it " + "音\n".repeat(200)) }
            assertEquals(4, directory.listFiles()!!.size)
            assertTrue(directory.listFiles()!!.sumOf { it.length() } <= 2 * 64 + 2 * 80)
            assertTrue(directory.resolve("events-0.log").readText().lines().size <= 2)
            val exported = ByteArrayOutputStream()
            store.exportTo(exported)
            assertTrue(exported.toString().contains("episode-19"))
            assertFalse(exported.toString().contains("episode-0 "))
        } finally {
            directory.deleteRecursively()
        }
    }
}
