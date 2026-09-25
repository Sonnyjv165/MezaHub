package com.example.mezahub.data.catalog

import com.example.mezahub.data.StarTier

/**
 * Version 4: 70 numbered cards across 63 species (no regular tags). Like Version 3, the
 * 2/3/4-star block (1-4-026 through 1-4-070) is interleaved by evolution line, not grouped by tier.
 *
 * Seven species appear at two star levels with the same cry, so their clips are the same file:
 * Calyrex (005/006), Kyurem (008/009), Meganium (012/030), Typhlosion (013/033),
 * Feraligatr (014/036), Machamp (018/057), and Talonflame (025/068).
 */
internal val VERSION_4_CARDS = versionCards(4) {
    // Superstar
    card("1-4-001", "Dialga", StarTier.SUPERSTAR)
    card("1-4-002", "Palkia", StarTier.SUPERSTAR)
    card("1-4-003", "Xerneas", StarTier.SUPERSTAR)
    card("1-4-004", "Yveltal", StarTier.SUPERSTAR)
    card("1-4-005", "Calyrex", StarTier.SUPERSTAR)
    card("1-4-006", "Calyrex", StarTier.SUPERSTAR)
    card("1-4-007", "Mimikyu", StarTier.SUPERSTAR)
    card("1-4-008", "Kyurem", StarTier.SUPERSTAR)
    card("1-4-009", "Kyurem", StarTier.SUPERSTAR)
    card("1-4-010", "Rayquaza", StarTier.SUPERSTAR)

    // Star
    card("1-4-011", "Aggron", StarTier.STAR)
    card("1-4-012", "Meganium", StarTier.STAR)
    card("1-4-013", "Typhlosion", StarTier.STAR)
    card("1-4-014", "Feraligatr", StarTier.STAR)
    card("1-4-015", "Goodra", StarTier.STAR)
    card("1-4-016", "Corviknight", StarTier.STAR)
    card("1-4-017", "Runerigus", StarTier.STAR)
    card("1-4-018", "Machamp", StarTier.STAR)
    card("1-4-019", "Gardevoir", StarTier.STAR)
    card("1-4-020", "Obstagoon", StarTier.STAR)
    card("1-4-021", "Regieleki", StarTier.STAR)
    card("1-4-022", "Regidrago", StarTier.STAR)
    card("1-4-023", "Iron Treads", StarTier.STAR)
    card("1-4-024", "Copperajah", StarTier.STAR)
    card("1-4-025", "Talonflame", StarTier.STAR)

    // 2/3/4-star block: interleaved by evolution line, not grouped by tier.
    card("1-4-026", "Ceruledge", StarTier.FOUR)
    card("1-4-027", "Baxcalibur", StarTier.FOUR)
    card("1-4-028", "Chikorita", StarTier.TWO)
    card("1-4-029", "Bayleef", StarTier.THREE)
    card("1-4-030", "Meganium", StarTier.FOUR)
    card("1-4-031", "Cyndaquil", StarTier.TWO)
    card("1-4-032", "Quilava", StarTier.THREE)
    card("1-4-033", "Typhlosion", StarTier.FOUR)
    card("1-4-034", "Totodile", StarTier.TWO)
    card("1-4-035", "Croconaw", StarTier.THREE)
    card("1-4-036", "Feraligatr", StarTier.FOUR)
    card("1-4-037", "Dreepy", StarTier.TWO)
    card("1-4-038", "Drakloak", StarTier.THREE)
    card("1-4-039", "Dragapult", StarTier.FOUR)
    card("1-4-040", "Sinistea", StarTier.TWO)
    card("1-4-041", "Polteageist", StarTier.FOUR)
    card("1-4-042", "Drampa", StarTier.THREE)
    card("1-4-043", "Elekid", StarTier.TWO)
    card("1-4-044", "Electabuzz", StarTier.THREE)
    card("1-4-045", "Electivire", StarTier.FOUR)
    card("1-4-046", "Alolan Vulpix", StarTier.THREE)
    card("1-4-047", "Alolan Ninetales", StarTier.FOUR)
    card("1-4-048", "Wooloo", StarTier.TWO)
    card("1-4-049", "Dubwool", StarTier.FOUR)
    card("1-4-050", "Deino", StarTier.TWO)
    card("1-4-051", "Zweilous", StarTier.THREE)
    card("1-4-052", "Hydreigon", StarTier.FOUR)
    card("1-4-053", "Woobat", StarTier.TWO)
    card("1-4-054", "Swoobat", StarTier.THREE)
    card("1-4-055", "Machop", StarTier.TWO)
    card("1-4-056", "Machoke", StarTier.THREE)
    card("1-4-057", "Machamp", StarTier.FOUR)
    card("1-4-058", "Murkrow", StarTier.THREE)
    card("1-4-059", "Honchkrow", StarTier.FOUR)
    card("1-4-060", "Hatenna", StarTier.TWO)
    card("1-4-061", "Hattrem", StarTier.THREE)
    card("1-4-062", "Hatterene", StarTier.FOUR)
    card("1-4-063", "Swinub", StarTier.TWO)
    card("1-4-064", "Piloswine", StarTier.THREE)
    card("1-4-065", "Mamoswine", StarTier.FOUR)
    card("1-4-066", "Fletchling", StarTier.TWO)
    card("1-4-067", "Fletchinder", StarTier.THREE)
    card("1-4-068", "Talonflame", StarTier.FOUR)
    card("1-4-069", "Noibat", StarTier.TWO)
    card("1-4-070", "Noivern", StarTier.FOUR)
}
