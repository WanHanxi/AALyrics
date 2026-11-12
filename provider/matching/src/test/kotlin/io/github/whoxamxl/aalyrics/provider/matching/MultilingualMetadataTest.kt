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
}
