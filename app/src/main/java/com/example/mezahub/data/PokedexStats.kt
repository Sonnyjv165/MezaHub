package com.example.mezahub.data

data class CardProgress(val entry: PokemonCryEntry, val timesHeard: Int, val lastHeardMillis: Long?) {
    val heard: Boolean get() = timesHeard > 0
}

data class TierProgress(val tier: StarTier, val heard: Int, val total: Int)

data class SpeciesCount(val speciesName: String, val count: Int, val iconTagId: String, val tier: StarTier)

data class PokedexStats(
    val cards: List<CardProgress>,
    val heardCount: Int,
    val totalDetections: Int,
    val tiers: List<TierProgress>,
    val mostHeard: List<SpeciesCount>,
) {
    val totalCards: Int get() = cards.size
}

/**
 * Derives Pokédex completion from detection history. Pure (no Android types) so it's testable.
 *
 * A detection that tied several cards (one cry shared across tiers) counts as "heard" for every
 * one of them — the cry was genuinely heard, the audio just can't say which card it was. For
 * [PokedexStats.mostHeard] that same detection counts once per species, not once per card.
 */
fun computePokedexStats(
    records: List<DetectionRecord>,
    catalog: List<PokemonCryEntry> = PokemonCryCatalog.all,
    topSpecies: Int = 3,
): PokedexStats {
    val timesHeard = HashMap<String, Int>()
    val lastHeard = HashMap<String, Long>()
    val speciesCounts = HashMap<String, Int>()
    val speciesIcon = HashMap<String, CryIconChoice>()

    for (record in records) {
        for (tagId in record.outcomes.map { it.tagId }.toSet()) {
            timesHeard[tagId] = (timesHeard[tagId] ?: 0) + 1
            lastHeard[tagId] = maxOf(lastHeard[tagId] ?: Long.MIN_VALUE, record.timestampMillis)
        }
        for ((species, outcomes) in record.outcomes.groupBy { it.speciesName }) {
            speciesCounts[species] = (speciesCounts[species] ?: 0) + 1
            // Represent each species by the rarest card it was ever heard as.
            val best = outcomes.maxBy { it.tier.stars }
            val current = speciesIcon[species]
            if (current == null || best.tier.stars > current.tier.stars) {
                speciesIcon[species] = CryIconChoice(best.tagId, best.tier)
            }
        }
    }

    val cards = catalog.map { CardProgress(it, timesHeard[it.tagId] ?: 0, lastHeard[it.tagId]) }
    val tiers = StarTier.entries.map { tier ->
        val inTier = cards.filter { it.entry.tier == tier }
        TierProgress(tier, heard = inTier.count { it.heard }, total = inTier.size)
    }.filter { it.total > 0 }
    val mostHeard = speciesCounts.entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        .take(topSpecies)
        .map { (species, count) -> speciesIcon.getValue(species).let { SpeciesCount(species, count, it.tagId, it.tier) } }

    return PokedexStats(
        cards = cards,
        heardCount = cards.count { it.heard },
        totalDetections = records.size,
        tiers = tiers,
        mostHeard = mostHeard,
    )
}

private data class CryIconChoice(val tagId: String, val tier: StarTier)
