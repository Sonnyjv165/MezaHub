package com.example.mezahub.data

/**
 * Filters and sorts the History list. Pure (no Android types) so it's unit-testable.
 *
 * A record passes the tier filter if ANY of its possible cards is in [tierFilter] (empty = all
 * tiers), and passes the search if any possible card's species name contains [searchQuery]
 * (case-insensitive; blank = everything).
 */
fun applyHistoryQuery(
    records: List<DetectionRecord>,
    ascending: Boolean,
    tierFilter: Set<StarTier>,
    searchQuery: String,
): List<DetectionRecord> {
    val query = searchQuery.trim()
    val filtered = records.filter { record ->
        val tierOk = tierFilter.isEmpty() || record.outcomes.any { it.tier in tierFilter }
        val searchOk = query.isEmpty() || record.outcomes.any { it.speciesName.contains(query, ignoreCase = true) }
        tierOk && searchOk
    }
    return if (ascending) {
        filtered.sortedBy { it.timestampMillis }
    } else {
        filtered.sortedByDescending { it.timestampMillis }
    }
}
