package io.github.whoxamxl.aalyrics.provider.matching

import kotlin.test.*

class MultilingualMetadataTest {
    @Test fun traditionalFormsAndFeatureCreditsCompareWithoutChangingDisplay() {
        assertEquals(1.0, MetadataMatching.titleSimilarity("夜裡的風", "夜里的风"))
        assertEquals(1.0, MetadataMatching.stringSimilarity("樂隊", "乐队"))
        assertEquals(1.0, MetadataMatching.titleSimilarity("Echo (feat. Guest)", "Echo"))
        assertFalse(MetadataMatching.versionsCompatible("Echo (feat. Guest) - Live", "Echo"))
    }

    @Test fun languageVersionSpellingsAreEquivalentButDifferentPerformancesAreNot() {
        assertEquals(1.0, MetadataMatching.titleSimilarity("Blue - English Version", "Blue (English ver.)"))
        assertTrue(MetadataMatching.versionsCompatible("Blue - English Version", "Blue (English ver.)"))
        assertFalse(MetadataMatching.versionsCompatible("Blue (English ver.)", "Blue (Japanese Version)"))
        assertFalse(MetadataMatching.versionsCompatible("Blue (English ver.)", "Blue"))
        assertFalse(MetadataMatching.versionsCompatible("Blue (English ver.) (Live)", "Blue - English Version"))
        assertTrue(MetadataMatching.versionsCompatible("English Rose", "English Rose"))
    }

    @Test fun bilingualDisplayNamesNeedAliasesOfTheSameEntity() {
        assertEquals(1.0, ArtistIdentity.similarity(listOf("樂隊 Example Band"), listOf("乐队"),
            mapOf("乐队" to listOf("Example Band")), allowComponents = true))
        assertEquals(1.0, ArtistIdentity.similarity(listOf("Example Band"), listOf("乐队"),
            mapOf("乐队" to listOf("Example Band")), allowComponents = true))
        assertTrue(ArtistIdentity.similarity(listOf("Different Band"), listOf("乐队"),
            mapOf("乐队" to listOf("Example Band")), allowComponents = true)!! < 0.8)
    }

    @Test fun collaborationExpansionNeedsEveryParticipantAndDirectIdentityAnchor() {
        assertEquals(0.95, ArtistIdentity.similarity(listOf("Svet & Mir", "Novy"),
            listOf("Svet", "Мир", "Новый"), allowComponents = true, allowTransliteration = true))
        assertEquals(0.95, ArtistIdentity.similarity(listOf("Alpha & Endspiel", "Rem Vega"),
            listOf("Alpha", "Эндшпиль", "Рем Вега"), allowComponents = true, allowTransliteration = true))
        assertTrue(ArtistIdentity.similarity(listOf("Svet & Mir", "Novy"),
            listOf("Svet", "Unrelated", "Other"), allowComponents = true, allowTransliteration = true)!! < 0.7)
        assertTrue(ArtistIdentity.similarity(listOf("Mir"), listOf("Мир"),
            allowComponents = true, allowTransliteration = true)!! < 0.4)
        assertTrue(ArtistIdentity.similarity(listOf("Earth, Wind & Fire"), listOf("Earth"),
            allowComponents = true)!! < 0.95)
        assertEquals(1.0, ArtistIdentity.similarity(listOf("Simon & Garfunkel"), listOf("Simon & Garfunkel")))
    }

    @Test fun exactTitleAndDurationDoNotExcuseUnrelatedArtist() {
        var reason: MetadataMatchScore.Rejection? = null
        assertNull(MetadataMatchScore.score("Echo", "Echo", "Singer", "别的歌手",
            requestedDurationMs = 240_000, candidateDurationMs = 240_100, onRejected = { reason = it }))
        assertEquals(MetadataMatchScore.Rejection.ARTIST, reason)
    }

    @Test fun phoneStyleCollaborationCreditsMatchTheSameCompleteParticipants() {
        val candidate = listOf("Alpha", "Эндшпиль", "Рем Вега")
        for (requested in listOf(
            listOf("Alpha & Endspiel", "Rem Vega"),
            listOf("Alpha & Endspiel, Rem Vega"),
            listOf("Alpha & Endspiel; Rem Vega"),
        )) {
            assertEquals(0.985, MetadataMatchScore.score(
                "Northern Light", "Northern Light", requested.joinToString(", "), candidate.joinToString(", "),
                "Album", "Album", 240_000, 240_100,
                requestedArtists = requested, candidateArtists = candidate,
            ))
        }
        assertEquals(0.95, ArtistIdentity.similarity(listOf("Alpha, Mir"), listOf("Alpha", "Мир"),
            allowComponents = true, allowTransliteration = true))
    }

    @Test fun commasWithinAlreadyMatchedParticipantsRemainIntact() {
        assertEquals(0.95, ArtistIdentity.similarity(listOf("Alpha, Beta & Mir"),
            listOf("Alpha, Beta", "Мир"), allowComponents = true, allowTransliteration = true))
    }

    @Test fun flatCreditExpansionCannotUseASubsetOrReuseAnArtist() {
        val requested = listOf("Alpha & Endspiel, Rem Vega")
        for (candidate in listOf(
            listOf("Alpha"),
            listOf("Alpha", "Рем Вега"),
            listOf("Alpha", "Рем Вега", "Рем Вега"),
            listOf("Alpha", "Unrelated", "Рем Вега"),
        )) {
            assertTrue(ArtistIdentity.similarity(requested, candidate, allowComponents = true,
                allowTransliteration = true, allowSecondaryCreditMismatch = true)!! < 0.95)
        }
    }

    @Test fun flatCrossScriptCreditsKeepVersionAndDurationRequirements() {
        fun score(title: String = "Northern Light", duration: Long = 240_100) = MetadataMatchScore.score(
            "Northern Light", title, "Alpha & Endspiel, Rem Vega", "Alpha, Эндшпиль, Рем Вега",
            "Album", "Album", 240_000, duration,
            candidateArtists = listOf("Alpha", "Эндшпиль", "Рем Вега"),
        )
        assertNull(score(title = "Northern Light (Live)"))
        assertNull(score(duration = 256_000))
        assertNull(score(duration = 246_000)) // Too weak to corroborate transliteration.
        assertTrue(ArtistIdentity.similarity(listOf("Alpha & Endspiel, Rem Vega"),
            listOf("Alpha", "Эндшпиль", "Рем Вега"), allowComponents = false,
            allowTransliteration = true)!! < 0.4)
    }

    @Test fun secondaryCatalogCreditsNeedSamePrimaryAlbumAndDuration() {
        val score = MetadataMatchScore.score("Echo", "Echo", "Lead, Pianist", "Lead, Label", "Album", "Album", 240_000, 240_000,
            requestedArtists = listOf("Lead", "Pianist"), candidateArtists = listOf("Lead", "Label"))
        assertEquals(0.985, score)
        assertTrue((ArtistIdentity.similarity(listOf("Lead", "Guest"), listOf("Stranger", "Guest"),
            allowComponents = true, allowSecondaryCreditMismatch = true) ?: 0.0) < 0.95)
    }
}
