package io.github.whoxamxl.aalyrics.ui.phone.lyrics

private val performancePhrases = Regex("[^ \\t\\u3000\\r\\n\\u2028\\u2029]+")

/**
 * Keep space-delimited phrases together when they fit. Compose still wraps an oversized phrase.
 * Only replace a separator, never insert/remove characters: WORD offsets remain canonical UTF-16
 * offsets for both layers of the shared Karaoke renderer. Original provider text is untouched.
 */
internal fun performanceWrappedText(text: String, fitsOnLine: (String) -> Boolean): String {
    if (text.none { it == ' ' || it == '\t' || it == '\u3000' } || fitsOnLine(text)) return text

    val display = StringBuilder(text)
    var lineStart = 0
    var previousEnd = 0
    for (phrase in performancePhrases.findAll(text)) {
        val start = phrase.range.first
        val end = phrase.range.last + 1
        for (index in previousEnd until start) {
            when (text[index]) {
                '\r', '\n', '\u2028', '\u2029' -> lineStart = index + 1
            }
        }
        if (previousEnd > lineStart && !fitsOnLine(text.substring(lineStart, end))) {
            // Use the last separator so repeated spaces do not indent the next visual line.
            display[start - 1] = '\n'
            lineStart = start
        }
        previousEnd = end
    }
    return display.toString()
}
