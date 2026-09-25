package com.example.mezahub.data

import com.example.mezahub.model.CryOutcome
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryQueryTest {

    private fun outcome(name: String, tier: StarTier) = CryOutcome("tag-$name-$tier", name, tier, 90, version = 3)

    private val treecko = DetectionRecord("1", listOf(outcome("Treecko", StarTier.TWO)), timestampMillis = 100)
    private val zygarde = DetectionRecord("2", listOf(outcome("Zygarde", StarTier.SUPERSTAR)), timestampMillis = 300)
    private val grimmsnarl = DetectionRecord(
        "3",
        listOf(
            outcome("Grimmsnarl", StarTier.SUPERSTAR),
            outcome("Grimmsnarl", StarTier.STAR),
            outcome("Grimmsnarl", StarTier.FOUR),
        ),
        timestampMillis = 200,
    )
    private val all = listOf(treecko, zygarde, grimmsnarl)

    private fun ids(result: List<DetectionRecord>) = result.map { it.id }

    @Test
    fun noFilters_newestFirstByDefault() {
        assertEquals(listOf("2", "3", "1"), ids(applyHistoryQuery(all, ascending = false, emptySet(), "")))
    }

    @Test
    fun ascending_oldestFirst() {
        assertEquals(listOf("1", "3", "2"), ids(applyHistoryQuery(all, ascending = true, emptySet(), "")))
    }

    @Test
    fun tierFilter_matchesAnyPossibleCard() {
        // Grimmsnarl's detection includes a 4-star possibility, so it passes a 4-star filter.
        assertEquals(listOf("3"), ids(applyHistoryQuery(all, false, setOf(StarTier.FOUR), "")))
    }

    @Test
    fun tierFilter_multipleTiersIsAUnion() {
        assertEquals(listOf("3", "1"), ids(applyHistoryQuery(all, false, setOf(StarTier.TWO, StarTier.STAR), "")))
    }

    @Test
    fun search_isCaseInsensitiveSubstring() {
        assertEquals(listOf("2"), ids(applyHistoryQuery(all, false, emptySet(), "  zyg ")))
    }

    @Test
    fun searchAndFilter_combine() {
        assertEquals(emptyList<String>(), ids(applyHistoryQuery(all, false, setOf(StarTier.TWO), "zygarde")))
        assertEquals(listOf("2"), ids(applyHistoryQuery(all, false, setOf(StarTier.SUPERSTAR), "zygarde")))
    }
}
