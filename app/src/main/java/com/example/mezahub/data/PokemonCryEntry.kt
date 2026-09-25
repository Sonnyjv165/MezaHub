package com.example.mezahub.data

/**
 * One catchable cry "card" in MezaStar. [tagId] is the real game tag ID (e.g. "1-3-011",
 * "R-1-1") and is also the expected filename (minus extension) under
 * `assets/versions/v<version>/cries/` and `.../icons/`. Several species appear as distinct cards
 * under multiple tiers (e.g. Sceptile at 1-3-011 [5-star] and 1-3-031 [4-star]) — tagId, not
 * speciesName, is the unique key within a [version].
 */
data class PokemonCryEntry(
    val tagId: String,
    val speciesName: String,
    val tier: StarTier,
    val version: Int,
)
