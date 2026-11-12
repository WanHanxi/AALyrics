package io.github.whoxamxl.aalyrics.provider.matching

/** Participant-aware comparison. Provider aliases belong to one named artist, not the whole song. */
object ArtistIdentity {
    fun similarity(
        requested: List<String>,
        candidate: List<String>,
        aliases: Map<String, List<String>> = emptyMap(),
        allowComponents: Boolean = false,
        allowTransliteration: Boolean = false,
    ): Double? {
        if (requested.isEmpty() || candidate.isEmpty()) return null
        if (requested == candidate) return 1.0
        val names = candidate.map { listOf(it) + aliases[it].orEmpty().take(8) }
        val direct = MetadataMatching.stringSimilarity(requested.joinToString(", "), candidate.joinToString(", "))
        val exact = requested.map { name -> names.map { variants -> compare(name, variants) } }
        if (requested.size == candidate.size && covers(exact, 0.999)) return 1.0
        if (!allowComponents) return maxOf(direct, exact.first().first())

        // A flat legacy contributor list is ambiguous; retain its established subset behavior.
        // Never split a band's comma/& spelling merely to match one of its words.
        val legacy = requested.size == 1 && '&' !in requested.single() &&
            requested.single().count { it == ',' || it == ';' } >= 2
        val left = if (legacy) requested.single().split(Regex("\\s*[,;]\\s*")) else requested
        val primaryRecognized = names.any { compare(left.first(), it) >= 0.999 }
        if ((legacy || primaryRecognized) && candidate.size <= left.size && names.all { variants ->
                left.any { compare(it, variants) >= 0.999 }
            }) return maxOf(direct, 0.95)

        // Expand & only if the provider actually lists every participant separately. A direct
        // name/alias anchor plus complete one-to-one coverage is required for transliteration.
        val expanded = left.flatMap { it.split(Regex("\\s+&\\s+")) }
        if (expanded.size != candidate.size) return direct
        val matrix = expanded.map { name -> names.map { compare(name, it) } }
        if (covers(matrix, 0.999)) return maxOf(direct, 0.95)
        if (allowTransliteration && matrix.any { row -> row.any { it >= 0.999 } }) {
            val supported = expanded.map { name -> names.map { variants ->
                maxOf(compare(name, variants), variants.maxOf { variant ->
                    MetadataMatching.stringSimilarity(ComparisonText.foldCyrillic(name), ComparisonText.foldCyrillic(variant))
                })
            } }
            if (covers(supported, 0.80)) return maxOf(direct, 0.95)
        }
        return direct
    }

    private fun compare(name: String, variants: List<String>): Double {
        val best = variants.maxOf { MetadataMatching.stringSimilarity(name, it) }
        if (best >= 0.999 || variants.size < 2) return best
        // A bilingual display name is accepted only when both pieces name this same entity.
        for (a in variants.indices) for (b in variants.indices) {
            if (a != b && MetadataMatching.stringSimilarity(name, "${variants[a]} ${variants[b]}") >= 0.999) return 1.0
        }
        return best
    }

    /** Bipartite matching prevents one shared guest from satisfying several participants. */
    private fun covers(scores: List<List<Double>>, minimum: Double): Boolean {
        val owner = IntArray(scores.firstOrNull()?.size ?: 0) { -1 }
        fun assign(row: Int, visited: BooleanArray): Boolean {
            for (column in owner.indices) {
                if (visited[column] || scores[row][column] < minimum) continue
                visited[column] = true
                if (owner[column] == -1 || assign(owner[column], visited)) {
                    owner[column] = row
                    return true
                }
            }
            return false
        }
        return scores.all { it.size == scores.size } && scores.indices.all { assign(it, BooleanArray(owner.size)) }
    }
}
