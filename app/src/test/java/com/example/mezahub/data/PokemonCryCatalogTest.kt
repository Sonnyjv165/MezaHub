package com.example.mezahub.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PokemonCryCatalogTest {

    private val all = PokemonCryCatalog.cards(MezastarVersion.V3)

    private fun v3(tagId: String) = PokemonCryCatalog.byTagId(MezastarVersion.V3, tagId)

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
        assertEquals(StarTier.SUPERSTAR, v3("1-3-009")?.tier)
        assertEquals(StarTier.STAR, v3("1-3-015")?.tier)
        assertEquals(StarTier.FOUR, v3("1-3-061")?.tier)
        assertTrue(listOf("1-3-009", "1-3-015", "1-3-061").all { v3(it)?.speciesName == "Grimmsnarl" })
        assertEquals(StarTier.REGULAR, v3("R-1-1")?.tier)
    }

    @Test
    fun byTagId_unknownIsNull() {
        assertNull(v3("9-9-999"))
    }

    @Test
    fun everyVersionUsesItsOwnTagPatternAndUniqueIds() {
        for (version in MezastarVersion.entries) {
            val cards = PokemonCryCatalog.cards(version)
            assertTrue(cards.all { it.version == version.number })
            assertEquals(cards.size, cards.map { it.tagId }.toSet().size)
            val pattern = Regex("""1-${version.number}-\d{3}|R-\d+-\d+""")
            cards.forEach { assertTrue("${it.tagId} in $version", pattern.matches(it.tagId)) }
        }
    }

    @Test
    fun lookupsAreScopedToTheirVersion() {
        assertNull(PokemonCryCatalog.byTagId(MezastarVersion.V4, "1-3-001"))
        assertEquals("Lugia", v3("1-3-001")?.speciesName)
    }

    @Test
    fun fromNumber_fallsBackToDefault() {
        assertEquals(MezastarVersion.V4, MezastarVersion.fromNumber(4))
        assertEquals(MezastarVersion.DEFAULT, MezastarVersion.fromNumber(99))
    }

    /** Every bundled cry/icon must belong to a card in its version's catalog, or it's silently ignored. */
    @Test
    fun bundledAssetsMatchTheirVersionCatalog() {
        for (version in MezastarVersion.entries) {
            val tagIds = PokemonCryCatalog.cards(version).map { it.tagId }.toSet()
            for (dir in listOf(version.criesDir, version.iconsDir)) {
                val folder = File("src/main/assets/$dir")
                assertTrue("missing folder $folder", folder.isDirectory)
                val orphans = folder.listFiles().orEmpty()
                    .filter { it.name != "README.md" }
                    .map { it.nameWithoutExtension }
                    .filterNot { it in tagIds }
                assertTrue("$dir has files with no catalog card: $orphans", orphans.isEmpty())
            }
        }
    }
}
