package com.example.mezahub.data.catalog

import com.example.mezahub.data.PokemonCryEntry
import com.example.mezahub.data.StarTier

internal class CatalogBuilder(private val version: Int) {
    private val cards = mutableListOf<PokemonCryEntry>()

    fun card(tagId: String, speciesName: String, tier: StarTier) {
        cards += PokemonCryEntry(tagId = tagId, speciesName = speciesName, tier = tier, version = version)
    }

    fun build(): List<PokemonCryEntry> = cards.toList()
}

/** Declares one version's card list: `versionCards(4) { card("1-4-001", "Name", StarTier.SUPERSTAR) }`. */
internal fun versionCards(version: Int, block: CatalogBuilder.() -> Unit): List<PokemonCryEntry> =
    CatalogBuilder(version).apply(block).build()
