package io.github.whoxamxl.aalyrics

import java.io.File
import java.io.IOException
import java.io.OutputStream

/** Single-writer store. Only the diagnostics IO worker accesses these fixed file names. */
internal class ConnectionDiagnosticStore(
    private val directory: File,
    private val chunkBytes: Int = 256 * 1024,
    private val eventFileCount: Int = 4,
    private val incidentBytes: Int = 128 * 1024,
    private val incidentFileCount: Int = 3,
) {
    init {
        require(chunkBytes > 0 && incidentBytes > 0)
        require(eventFileCount > 0 && incidentFileCount > 0)
    }

    fun append(line: String) {
        prepareDirectory()
        val bytes = boundedLine(line, minOf(8 * 1024, chunkBytes))
        val current = file("events", 0)
        if (current.length() + bytes.size > chunkBytes) rotate("events", eventFileCount)
        current.appendBytes(bytes)
    }

    /** Freeze the recent event tail separately so normal use cannot immediately overwrite it. */
    fun captureIncident(line: String) {
        append(line)
        rotate("incident", incidentFileCount)
        var remaining = incidentBytes
        val tails = mutableListOf<ByteArray>()
        for (index in 0 until eventFileCount) {
            val source = file("events", index)
            if (!source.exists() || remaining == 0) continue
            val data = source.readBytes()
            val tail = data.takeLast(minOf(remaining, data.size)).toByteArray()
            tails.add(0, tail)
            remaining -= tail.size
        }
        file("incident", 0).outputStream().use { output -> tails.forEach(output::write) }
    }

    fun exportTo(output: OutputStream) {
        output.write("AALyrics connection diagnostics v1\nLocal Debug records; timestamps are UTC.\n\n".toByteArray())
        for ((prefix, count) in listOf("events" to eventFileCount, "incident" to incidentFileCount)) {
            for (index in count - 1 downTo 0) {
                val source = file(prefix, index)
                if (!source.exists()) continue
                output.write("\n--- ${source.name} ---\n".toByteArray())
                source.inputStream().use { it.copyTo(output) }
            }
        }
        output.flush()
    }

    private fun prepareDirectory() {
        if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Diagnostics storage unavailable")
    }

    private fun file(prefix: String, index: Int) = directory.resolve("$prefix-$index.log")

    private fun rotate(prefix: String, count: Int) {
        val oldest = file(prefix, count - 1)
        if (oldest.exists() && !oldest.delete()) throw IOException("Diagnostics rotation failed")
        for (index in count - 2 downTo 0) {
            val source = file(prefix, index)
            if (source.exists() && !source.renameTo(file(prefix, index + 1))) {
                throw IOException("Diagnostics rotation failed")
            }
        }
    }

    private fun boundedLine(line: String, limit: Int): ByteArray {
        // An ASCII diagnostic format keeps truncation valid and forbids multiline field injection.
        val safe = line.map { if (it in ' '..'~' || it == '\t') it else '?' }
            .joinToString("").take(limit - 1)
        return "$safe\n".toByteArray(Charsets.US_ASCII)
    }
}
