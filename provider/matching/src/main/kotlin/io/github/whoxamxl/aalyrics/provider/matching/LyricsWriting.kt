package io.github.whoxamxl.aalyrics.provider.matching

import java.util.Locale

/** Conservative lexical signal, not language detection. Unknown Latin text stays unknown. */
object LyricsWriting {
    data class Profile(
        val nativeRows: Map<String, Int>, val romanizedRows: Map<String, Int>,
        val usefulRows: Int, val japaneseLikeRows: Int,
    ) {
        val suspectedRomanization: String? get() = romanizedRows.entries.firstOrNull {
            it.value >= 3 && (if (it.key == "ja") japaneseLikeRows * 3 >= usefulRows else it.value * 4 >= usefulRows) &&
                (nativeRows[it.key] ?: 0) == 0
        }?.key
        fun hasOriginal(language: String): Boolean = (nativeRows[language] ?: 0) >= 3
    }

    fun profile(lines: List<String>): Profile {
        val native = mutableMapOf<String, Int>()
        val romanized = mutableMapOf<String, Int>()
        val rowCache = HashMap<String, RowSignals>()
        var useful = 0
        var japaneseLike = 0
        lines.forEach { line ->
            if (line.none(Char::isLetter)) return@forEach
            useful++
            val row = rowCache.getOrPut(line) { signals(line) }
            if (row.japaneseLike) japaneseLike++
            row.native?.let { native[it] = (native[it] ?: 0) + 1 }
            row.romanized?.let { romanized[it] = (romanized[it] ?: 0) + 1 }
        }
        return Profile(native, romanized, useful, japaneseLike)
    }

    fun nativeLanguage(text: String): String? {
        var han = false
        var hangul = false
        var cyrillic = false
        for (char in text) when (char) {
            in '\u3040'..'\u30ff' -> return "ja"
            in '\uac00'..'\ud7af', in '\u1100'..'\u11ff' -> hangul = true
            in '\u0400'..'\u052f' -> cyrillic = true
            in '\u3400'..'\u9fff' -> han = true
        }
        return when { hangul -> "ko"; cyrillic -> "ru"; han -> "zh"; else -> null }
    }

    fun romanizedLanguage(text: String): String? = signals(text).romanized

    private data class RowSignals(val native: String? = null, val romanized: String? = null, val japaneseLike: Boolean = false)

    private fun signals(text: String): RowSignals {
        nativeLanguage(text)?.let { return RowSignals(native = it) }
        if (text.any { it.isLetter() && Character.UnicodeScript.of(it.code) != Character.UnicodeScript.LATIN }) return RowSignals()
        val words = WORD.findAll(text.lowercase(Locale.ROOT)).map { it.value }.toList()
        if (words.size < 3 || words.count { it in ENGLISH_WORDS } >= 2) return RowSignals()
        val syllabic = words.filter { JAPANESE_SYLLABLES.matches(DOUBLED_CONSONANT.replace(it, "$1")) }
        val japaneseLike = syllabic.size.toDouble() / words.size >= 0.70 && syllabic.count { it.length >= 4 } >= 2
        val lexical = VOCABULARY.entries.firstOrNull { (_, vocabulary) ->
            val hits = words.count { it in vocabulary }
            val distinct = words.filter { it in vocabulary }.toSet().size
            hits >= 2 && distinct >= 2 && hits * 3 >= words.size
        }?.key
        val particles = words.filter { it in JAPANESE_PARTICLES }.toSet().size
        val japaneseEvidence = japaneseLike &&
            (particles >= 2 || (particles >= 1 && words.any { it in VOCABULARY.getValue("ja") }))
        return RowSignals(romanized = lexical ?: if (japaneseEvidence) "ja" else null, japaneseLike = japaneseLike || lexical == "ja")
    }

    private val DOUBLED_CONSONANT = Regex("([kstpgbcdnrf])\\1")
    private val JAPANESE_SYLLABLES = Regex("(?:[kstnhmyrwgzdbpfvj]?[aeiou]|(?:sh|ch|ts|[kgshnmyrzdbpfvj]y)[aeiou]|n(?![aeiouy])|'|-)+")
    private val JAPANESE_PARTICLES = setOf("wa", "wo", "ga", "no", "ni", "de", "to", "mo", "e", "o")
    private val ENGLISH_WORDS = setOf("the", "you", "your", "we", "this", "that", "with", "and", "our", "there", "have", "love")

    private val WORD = Regex("[a-z]+")
    private val VOCABULARY = linkedMapOf(
        "ja" to setOf("watashi", "boku", "kimi", "anata", "kokoro", "yume", "sekai", "namida", "kore", "sore", "desu", "shite", "hito", "nani", "itsu", "doko", "suki", "kaze", "sora", "hoshi", "mou", "mada", "zutto", "kitto", "tsugi", "nihongo", "arigatou", "konna", "sonna", "hajimete", "omoide", "hitomi", "koto", "soshite", "dakara", "sayonara"),
        "ko" to setOf("sarang", "saranghae", "saranghaeyo", "neoga", "naega", "naneun", "neoneun", "uri", "eopseo", "isseo", "maeum", "geudae", "eotteoke", "geurae", "molla", "neoui", "naui"),
        "ru" to setOf("tebya", "tebia", "menya", "menia", "lyublyu", "liubliu", "moya", "moia", "tvoya", "tvoia", "zhizn", "noch", "serdtse", "nebo", "lyubov", "liubov", "pochemu", "potomu", "nikogda", "segodnya", "vsegda", "tolko"),
    )
}
