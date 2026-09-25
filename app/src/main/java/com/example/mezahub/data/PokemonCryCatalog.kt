package com.example.mezahub.data

import com.example.mezahub.data.catalog.VERSION_1_CARDS
import com.example.mezahub.data.catalog.VERSION_2_CARDS
import com.example.mezahub.data.catalog.VERSION_3_CARDS
import com.example.mezahub.data.catalog.VERSION_4_CARDS

/**
 * Every Mezastar version's card list (one file per version under `data/catalog/`). Lookups are
 * scoped to a version: tag IDs are only guaranteed unique within one (regular tags like "R-1-1"
 * may reappear across versions).
 */
object PokemonCryCatalog {
    private val cardsByVersion: Map<MezastarVersion, List<PokemonCryEntry>> = mapOf(
        MezastarVersion.V1 to VERSION_1_CARDS,
        MezastarVersion.V2 to VERSION_2_CARDS,
        MezastarVersion.V3 to VERSION_3_CARDS,
        MezastarVersion.V4 to VERSION_4_CARDS,
    )

    private val byTagIdByVersion: Map<MezastarVersion, Map<String, PokemonCryEntry>> =
        cardsByVersion.mapValues { (_, cards) -> cards.associateBy { it.tagId } }

    fun cards(version: MezastarVersion): List<PokemonCryEntry> = cardsByVersion.getValue(version)

    fun byTagId(version: MezastarVersion, tagId: String): PokemonCryEntry? = byTagIdByVersion.getValue(version)[tagId]
}
