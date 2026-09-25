package com.example.mezahub.data
 
/**
 * Static catalog of all 73 MezaStar cry cards (70 numbered + 3 regular tags) across 65 species,
 * keyed by the real game tag IDs (e.g. "1-3-001", "R-1-1"). The 2/3/4-star block (1-3-026 through 1-3-070) is NOT grouped
 * by tier in the real numbering — it's interleaved by evolution line (base -> middle -> final
 * each get their own consecutive IDs), so tier here is taken from the card list directly rather
 * than derived from the ID or the Pokedex evolution chain. Several species recur as distinct
 * cards across tiers (e.g. Sceptile at 1-3-011 [5-star] and 1-3-031 [4-star], Grimmsnarl at
 * three different tiers) — tagId, not speciesName, is the unique key for fingerprint lookup.
 */
object PokemonCryCatalog {
    val all: List<PokemonCryEntry> = buildList {
        addAll(
            listOf(
                entry("1-3-001", "Lugia", StarTier.SUPERSTAR),
                entry("1-3-002", "Ho-Oh", StarTier.SUPERSTAR),
                entry("1-3-003", "Solgaleo", StarTier.SUPERSTAR),
                entry("1-3-004", "Lunala", StarTier.SUPERSTAR),
                entry("1-3-005", "Greninja", StarTier.SUPERSTAR),
                entry("1-3-006", "Zeraora", StarTier.SUPERSTAR),
                entry("1-3-007", "Eternatus", StarTier.SUPERSTAR),
                entry("1-3-008", "Keldeo", StarTier.SUPERSTAR),
                entry("1-3-009", "Grimmsnarl", StarTier.SUPERSTAR),
                entry("1-3-010", "Zygarde", StarTier.SUPERSTAR),
            ),
        )
        addAll(
            listOf(
                entry("1-3-011", "Sceptile", StarTier.STAR),
                entry("1-3-012", "Blaziken", StarTier.STAR),
                entry("1-3-013", "Swampert", StarTier.STAR),
                entry("1-3-014", "Chandelure", StarTier.STAR),
                entry("1-3-015", "Grimmsnarl", StarTier.STAR),
                entry("1-3-016", "Nidoqueen", StarTier.STAR),
                entry("1-3-017", "Pidgeot", StarTier.STAR),
                entry("1-3-018", "Coalossal", StarTier.STAR),
                entry("1-3-019", "Pikachu", StarTier.STAR),
                entry("1-3-020", "Alakazam", StarTier.STAR),
                entry("1-3-021", "Regirock", StarTier.STAR),
                entry("1-3-022", "Regice", StarTier.STAR),
                entry("1-3-023", "Registeel", StarTier.STAR),
                entry("1-3-024", "Great Tusk", StarTier.STAR),
                entry("1-3-025", "Haxorus", StarTier.STAR),
            ),
        )
        // 2/3/4-star block: interleaved by evolution line, not grouped by tier.
        addAll(
            listOf(
                entry("1-3-026", "Armarouge", StarTier.FOUR),
                entry("1-3-027", "Arctibax", StarTier.THREE),
                entry("1-3-028", "Pawmo", StarTier.THREE),
                entry("1-3-029", "Treecko", StarTier.TWO),
                entry("1-3-030", "Grovyle", StarTier.THREE),
                entry("1-3-031", "Sceptile", StarTier.FOUR),
                entry("1-3-032", "Torchic", StarTier.TWO),
                entry("1-3-033", "Combusken", StarTier.THREE),
                entry("1-3-034", "Blaziken", StarTier.FOUR),
                entry("1-3-035", "Mudkip", StarTier.TWO),
                entry("1-3-036", "Marshtomp", StarTier.THREE),
                entry("1-3-037", "Swampert", StarTier.FOUR),
                entry("1-3-038", "Magby", StarTier.TWO),
                entry("1-3-039", "Magmar", StarTier.THREE),
                entry("1-3-040", "Magmortar", StarTier.FOUR),
                entry("1-3-041", "Koffing", StarTier.TWO),
                entry("1-3-042", "Galarian Weezing", StarTier.FOUR),
                entry("1-3-043", "Beldum", StarTier.TWO),
                entry("1-3-044", "Metang", StarTier.THREE),
                entry("1-3-045", "Metagross", StarTier.FOUR),
                entry("1-3-046", "Shinx", StarTier.TWO),
                entry("1-3-047", "Luxio", StarTier.THREE),
                entry("1-3-048", "Luxray", StarTier.FOUR),
                entry("1-3-049", "Blipbug", StarTier.TWO),
                entry("1-3-050", "Dottler", StarTier.THREE),
                entry("1-3-051", "Orbeetle", StarTier.FOUR),
                entry("1-3-052", "Silicobra", StarTier.TWO),
                entry("1-3-053", "Sandaconda", StarTier.FOUR),
                entry("1-3-054", "Rolycoly", StarTier.TWO),
                entry("1-3-055", "Carkol", StarTier.THREE),
                entry("1-3-056", "Coalossal", StarTier.FOUR),
                entry("1-3-057", "Mudbray", StarTier.THREE),
                entry("1-3-058", "Mudsdale", StarTier.FOUR),
                entry("1-3-059", "Impidimp", StarTier.TWO),
                entry("1-3-060", "Morgrem", StarTier.THREE),
                entry("1-3-061", "Grimmsnarl", StarTier.FOUR),
                entry("1-3-062", "Turtonator", StarTier.FOUR),
                entry("1-3-063", "Nickit", StarTier.TWO),
                entry("1-3-064", "Thievul", StarTier.FOUR),
                entry("1-3-065", "Duskull", StarTier.TWO),
                entry("1-3-066", "Dusclops", StarTier.THREE),
                entry("1-3-067", "Dusknoir", StarTier.FOUR),
                entry("1-3-068", "Axew", StarTier.TWO),
                entry("1-3-069", "Fraxure", StarTier.THREE),
                entry("1-3-070", "Haxorus", StarTier.FOUR),
            ),
        )
        addAll(
            listOf(
                entry("R-1-1", "Pikachu", StarTier.REGULAR),
                entry("R-1-2", "Lucario", StarTier.REGULAR),
                entry("R-1-3", "Snorlax", StarTier.REGULAR),
            ),
        )
    }

    private val byTagIdMap: Map<String, PokemonCryEntry> = all.associateBy { it.tagId }

    fun byTagId(tagId: String): PokemonCryEntry? = byTagIdMap[tagId]

    private fun entry(tagId: String, speciesName: String, tier: StarTier) =
        PokemonCryEntry(tagId = tagId, speciesName = speciesName, tier = tier)
}
