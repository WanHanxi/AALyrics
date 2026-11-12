package io.github.whoxamxl.aalyrics.provider.matching

/** Small ICU-derived comparison tables; no runtime ICU dependency or display rewriting. */
internal object ComparisonText {
    private fun table(name: String): Map<Char, String> =
        requireNotNull(javaClass.getResourceAsStream(name)).bufferedReader().useLines { rows ->
            rows.filter { !it.startsWith('#') && it.isNotEmpty() }.associate { row ->
                row.substringBefore('\t').toInt(16).toChar() to row.substringAfter('\t')
            }
        }

    private val han by lazy { table("han-folds.tsv") }
    private val cyrillic by lazy { table("cyrillic-folds.tsv") }
    private fun isHan(char: Char) = char in '\u3400'..'\u9fff' || char in '\uf900'..'\ufaff'

    fun foldHan(value: String): String = if (value.none(::isHan)) value else buildString {
        value.forEach { append(if (isHan(it)) han[it] ?: it.toString() else it.toString()) }
    }

    fun foldCyrillic(value: String): String = if (value.none { it in '\u0400'..'\u052f' }) value else buildString {
        value.forEach { append(cyrillic[it] ?: it.toString()) }
    }
}
