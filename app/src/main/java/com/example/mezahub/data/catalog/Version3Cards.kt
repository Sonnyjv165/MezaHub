package com.example.mezahub.data.catalog

import com.example.mezahub.data.StarTier

/**
 * Version 3: 73 cards (70 numbered + 3 regular tags) across 65 species. The 2/3/4-star block
 * (1-3-026 through 1-3-070) is NOT grouped by tier in the real numbering — it's interleaved by
 * evolution line — so tier comes from the card list, not the ID or the evolution chain.
 */
internal val VERSION_3_CARDS = versionCards(3) {
    // Superstar
    card("1-3-001", "Lugia", StarTier.SUPERSTAR)
    card("1-3-002", "Ho-Oh", StarTier.SUPERSTAR)
    card("1-3-003", "Solgaleo", StarTier.SUPERSTAR)
    card("1-3-004", "Lunala", StarTier.SUPERSTAR)
    card("1-3-005", "Greninja", StarTier.SUPERSTAR)
    card("1-3-006", "Zeraora", StarTier.SUPERSTAR)
    card("1-3-007", "Eternatus", StarTier.SUPERSTAR)
    card("1-3-008", "Keldeo", StarTier.SUPERSTAR)
    card("1-3-009", "Grimmsnarl", StarTier.SUPERSTAR)
    card("1-3-010", "Zygarde", StarTier.SUPERSTAR)

    // Star
    card("1-3-011", "Sceptile", StarTier.STAR)
    card("1-3-012", "Blaziken", StarTier.STAR)
    card("1-3-013", "Swampert", StarTier.STAR)
    card("1-3-014", "Chandelure", StarTier.STAR)
    card("1-3-015", "Grimmsnarl", StarTier.STAR)
    card("1-3-016", "Nidoqueen", StarTier.STAR)
    card("1-3-017", "Pidgeot", StarTier.STAR)
    card("1-3-018", "Coalossal", StarTier.STAR)
    card("1-3-019", "Pikachu", StarTier.STAR)
    card("1-3-020", "Alakazam", StarTier.STAR)
    card("1-3-021", "Regirock", StarTier.STAR)
    card("1-3-022", "Regice", StarTier.STAR)
    card("1-3-023", "Registeel", StarTier.STAR)
    card("1-3-024", "Great Tusk", StarTier.STAR)
    card("1-3-025", "Haxorus", StarTier.STAR)

    // 2/3/4-star block: interleaved by evolution line, not grouped by tier.
    card("1-3-026", "Armarouge", StarTier.FOUR)
    card("1-3-027", "Arctibax", StarTier.THREE)
    card("1-3-028", "Pawmo", StarTier.THREE)
    card("1-3-029", "Treecko", StarTier.TWO)
    card("1-3-030", "Grovyle", StarTier.THREE)
    card("1-3-031", "Sceptile", StarTier.FOUR)
    card("1-3-032", "Torchic", StarTier.TWO)
    card("1-3-033", "Combusken", StarTier.THREE)
    card("1-3-034", "Blaziken", StarTier.FOUR)
    card("1-3-035", "Mudkip", StarTier.TWO)
    card("1-3-036", "Marshtomp", StarTier.THREE)
    card("1-3-037", "Swampert", StarTier.FOUR)
    card("1-3-038", "Magby", StarTier.TWO)
    card("1-3-039", "Magmar", StarTier.THREE)
    card("1-3-040", "Magmortar", StarTier.FOUR)
    card("1-3-041", "Koffing", StarTier.TWO)
    card("1-3-042", "Galarian Weezing", StarTier.FOUR)
    card("1-3-043", "Beldum", StarTier.TWO)
    card("1-3-044", "Metang", StarTier.THREE)
    card("1-3-045", "Metagross", StarTier.FOUR)
    card("1-3-046", "Shinx", StarTier.TWO)
    card("1-3-047", "Luxio", StarTier.THREE)
    card("1-3-048", "Luxray", StarTier.FOUR)
    card("1-3-049", "Blipbug", StarTier.TWO)
    card("1-3-050", "Dottler", StarTier.THREE)
    card("1-3-051", "Orbeetle", StarTier.FOUR)
    card("1-3-052", "Silicobra", StarTier.TWO)
    card("1-3-053", "Sandaconda", StarTier.FOUR)
    card("1-3-054", "Rolycoly", StarTier.TWO)
    card("1-3-055", "Carkol", StarTier.THREE)
    card("1-3-056", "Coalossal", StarTier.FOUR)
    card("1-3-057", "Mudbray", StarTier.THREE)
    card("1-3-058", "Mudsdale", StarTier.FOUR)
    card("1-3-059", "Impidimp", StarTier.TWO)
    card("1-3-060", "Morgrem", StarTier.THREE)
    card("1-3-061", "Grimmsnarl", StarTier.FOUR)
    card("1-3-062", "Turtonator", StarTier.FOUR)
    card("1-3-063", "Nickit", StarTier.TWO)
    card("1-3-064", "Thievul", StarTier.FOUR)
    card("1-3-065", "Duskull", StarTier.TWO)
    card("1-3-066", "Dusclops", StarTier.THREE)
    card("1-3-067", "Dusknoir", StarTier.FOUR)
    card("1-3-068", "Axew", StarTier.TWO)
    card("1-3-069", "Fraxure", StarTier.THREE)
    card("1-3-070", "Haxorus", StarTier.FOUR)

    // Regular tags
    card("R-1-1", "Pikachu", StarTier.REGULAR)
    card("R-1-2", "Lucario", StarTier.REGULAR)
    card("R-1-3", "Snorlax", StarTier.REGULAR)
}
