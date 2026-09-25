package com.example.mezahub.data

import com.example.mezahub.model.CryOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PokedexStatsTest {

    private fun card(tagId: String): CryOutcome {
        val entry = PokemonCryCatalog.byTagId(MezastarVersion.V3, tagId)!!
        return CryOutcome(entry.tagId, entry.speciesName, entry.tier, 90, entry.version)
    }

    private fun record(time: Long, vararg tagIds: String) =
        DetectionRecord(id = "r$time", outcomes = tagIds.map { card(it) }, timestampMillis = time)

    @Test
    fun emptyHistory_nothingHeard() {
        val stats = computePokedexStats(emptyList())
        assertEquals(73, stats.totalCards)
        assertEquals(0, stats.heardCount)
        assertEquals(0, stats.totalDetections)
        assertTrue(stats.mostHeard.isEmpty())
        assertEquals(listOf(10, 15, 17, 14, 14, 3), stats.tiers.map { it.total })
    }

    @Test
    fun tiedDetection_marksEveryPossibleCardHeard() {
        // One Grimmsnarl cry tied across its 6/5/4-star cards.
        val stats = computePokedexStats(listOf(record(1, "1-3-009", "1-3-015", "1-3-061")))
        assertEquals(3, stats.heardCount)
        assertEquals(1, stats.totalDetections)
        assertEquals(1, stats.tiers.first { it.tier == StarTier.SUPERSTAR }.heard)
        assertEquals(1, stats.tiers.first { it.tier == StarTier.FOUR }.heard)
    }

    @Test
    fun timesHeardAndLastHeard_trackPerCard() {
        val stats = computePokedexStats(listOf(record(100, "1-3-029"), record(500, "1-3-029"), record(300, "1-3-001")))
        val treecko = stats.cards.first { it.entry.tagId == "1-3-029" }
        assertEquals(2, treecko.timesHeard)
        assertEquals(500L, treecko.lastHeardMillis)
        assertFalse(stats.cards.first { it.entry.tagId == "1-3-002" }.heard)
    }

    @Test
    fun mostHeard_countsSpeciesOncePerDetectionAndUsesRarestCardIcon() {
        val stats = computePokedexStats(
            listOf(
                record(1, "1-3-009", "1-3-015", "1-3-061"), // Grimmsnarl x1 (tied across 3 cards)
                record(2, "1-3-061"), // Grimmsnarl x2
                record(3, "1-3-029"), // Treecko x1
            ),
        )
        val top = stats.mostHeard.first()
        assertEquals("Grimmsnarl", top.speciesName)
        assertEquals(2, top.count)
        assertEquals("1-3-009", top.iconTagId) // Superstar card, the rarest it was heard as.
        assertEquals(listOf("Grimmsnarl", "Treecko"), stats.mostHeard.map { it.speciesName })
    }

    @Test
    fun otherVersionsDetectionsAreNotCounted() {
        val other = DetectionRecord(
            id = "v4",
            outcomes = listOf(CryOutcome("1-4-001", "Somebody", StarTier.SUPERSTAR, 90, version = 4)),
            timestampMillis = 1,
        )
        val stats = computePokedexStats(listOf(other, record(2, "1-3-001")), MezastarVersion.V3)
        assertEquals(1, stats.totalDetections)
        assertEquals(1, stats.heardCount)
    }

    @Test
    fun placeholderVersion_hasNoCards() {
        val stats = computePokedexStats(emptyList(), MezastarVersion.V4)
        assertEquals(4, stats.version)
        assertEquals(0, stats.totalCards)
        assertTrue(stats.tiers.isEmpty())
    }
}
