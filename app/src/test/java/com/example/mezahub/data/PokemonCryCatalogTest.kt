package com.example.mezahub.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PokemonCryCatalogTest {

    private val all = PokemonCryCatalog.all

    @Test
    fun has73CardsAcross65Species() {
        assertEquals(73, all.size)
        assertEquals(65, all.map { it.speciesName }.toSet().size)
    }

    @Test
    fun tagIdsAreUnique() {
        assertEquals(all.size, all.map { it.tagId }.toSet().size)
    }

    @Test
    fun tierCountsMatchTheCardList() {
        val counts = all.groupingBy { it.tier }.eachCount()
        assertEquals(10, counts[StarTier.SUPERSTAR])
        assertEquals(15, counts[StarTier.STAR])
        assertEquals(17, counts[StarTier.FOUR])
        assertEquals(14, counts[StarTier.THREE])
        assertEquals(14, counts[StarTier.TWO])
        assertEquals(3, counts[StarTier.REGULAR])
    }

    @Test
    fun numberedCardsRunContiguouslyFrom001To070() {
        val numbers = all.map { it.tagId }.filter { it.startsWith("1-3-") }.map { it.removePrefix("1-3-").toInt() }
        assertEquals((1..70).toList(), numbers.sorted())
    }

    @Test
    fun byTagId_resolvesDuplicateSpeciesToDistinctTiers() {
        assertEquals(StarTier.SUPERSTAR, PokemonCryCatalog.byTagId("1-3-009")?.tier)
        assertEquals(StarTier.STAR, PokemonCryCatalog.byTagId("1-3-015")?.tier)
        assertEquals(StarTier.FOUR, PokemonCryCatalog.byTagId("1-3-061")?.tier)
        assertTrue(listOf("1-3-009", "1-3-015", "1-3-061").all { PokemonCryCatalog.byTagId(it)?.speciesName == "Grimmsnarl" })
        assertEquals(StarTier.REGULAR, PokemonCryCatalog.byTagId("R-1-1")?.tier)
    }

    @Test
    fun byTagId_unknownIsNull() {
        assertNull(PokemonCryCatalog.byTagId("9-9-999"))
    }
}
