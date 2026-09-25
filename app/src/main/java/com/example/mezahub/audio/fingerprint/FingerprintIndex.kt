package com.example.mezahub.audio.fingerprint

import kotlin.math.min
import kotlin.math.roundToInt

// Vote-count threshold to count as a match, scaled by the user's sensitivity setting:
// strictest (sensitivity 0) needs a much cleaner match than most lenient (sensitivity 1).
private const val MIN_VOTES_STRICT = 16
private const val MIN_VOTES_LENIENT = 4

// Candidates within this fraction of the top vote count are treated as tied — this is what
// surfaces every card that shares an identical cry (e.g. Grimmsnarl at 6/5/4-star).
private const val TIE_TOLERANCE = 0.9

/** Minimum vote count for a match at the given sensitivity (0f = strict ... 1f = lenient). */
fun minMatchVotesFor(sensitivity: Float): Int {
    val s = sensitivity.coerceIn(0f, 1f)
    return (MIN_VOTES_STRICT - s * (MIN_VOTES_STRICT - MIN_VOTES_LENIENT)).roundToInt()
}

data class IndexMatch(val tagId: String, val votes: Int, val confidencePercent: Int)

/**
 * Inverted index (hash -> where it occurs in which reference clip) plus offset-histogram voting.
 * Pure Kotlin with no Android dependencies so the matching behaviour can be unit tested.
 */
class FingerprintIndex private constructor(
    private val postings: Map<Long, List<Posting>>,
    private val referenceHashCounts: Map<String, Int>,
) {
    private class Posting(val tagId: String, val frameIndex: Int)

    /** Number of reference clips in the index. */
    val size: Int get() = referenceHashCounts.size

    fun isEmpty(): Boolean = postings.isEmpty()

    /**
     * Every reference whose best (tag, time-offset) vote count is within [TIE_TOLERANCE] of the
     * overall best, strongest first. A true match piles votes onto one consistent offset because
     * the whole clip lines up; noise scatters a few votes across many offsets. Empty if even the
     * best candidate is under [minMatchVotes].
     */
    fun match(queryHashes: List<FingerprintHash>, minMatchVotes: Int): List<IndexMatch> {
        if (queryHashes.isEmpty() || postings.isEmpty()) return emptyList()

        val votes = HashMap<Pair<String, Int>, Int>()
        for (q in queryHashes) {
            val hits = postings[q.hash] ?: continue
            for (p in hits) {
                val key = p.tagId to (p.frameIndex - q.frameIndex)
                votes[key] = (votes[key] ?: 0) + 1
            }
        }

        val maxVotes = votes.values.maxOrNull() ?: return emptyList()
        if (maxVotes < minMatchVotes) return emptyList()

        val tieThreshold = (maxVotes * TIE_TOLERANCE).toInt().coerceAtLeast(minMatchVotes)
        val bestVotesByTag = HashMap<String, Int>()
        for ((key, count) in votes) {
            if (count < tieThreshold) continue
            if (count > (bestVotesByTag[key.first] ?: 0)) bestVotesByTag[key.first] = count
        }

        return bestVotesByTag.map { (tagId, count) ->
            val referenceSize = referenceHashCounts[tagId] ?: queryHashes.size
            val denominator = min(queryHashes.size, referenceSize).coerceAtLeast(1)
            IndexMatch(tagId, count, (count * 100 / denominator).coerceIn(1, 99))
        }.sortedWith(compareByDescending<IndexMatch> { it.votes }.thenBy { it.tagId })
    }

    companion object {
        val EMPTY = FingerprintIndex(emptyMap(), emptyMap())

        fun build(references: Map<String, List<FingerprintHash>>): FingerprintIndex {
            val postings = HashMap<Long, MutableList<Posting>>()
            val counts = HashMap<String, Int>()
            for ((tagId, hashes) in references) {
                if (hashes.isEmpty()) continue
                for (h in hashes) postings.getOrPut(h.hash) { mutableListOf() } += Posting(tagId, h.frameIndex)
                counts[tagId] = hashes.size
            }
            return FingerprintIndex(postings, counts)
        }
    }
}
