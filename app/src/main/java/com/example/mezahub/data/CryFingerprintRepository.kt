package com.example.mezahub.data

import android.content.Context
import com.example.mezahub.audio.WavDecoder
import com.example.mezahub.audio.fingerprint.AudioFingerprinter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import kotlin.math.min

data class CryMatch(val entry: PokemonCryEntry, val confidencePercent: Int)

private data class IndexEntry(val tagId: String, val frameIndex: Int)

/**
 * Holds fingerprints generated from bundled reference clips (assets/cries/<tagId>.wav, 16-bit
 * PCM) and matches captured mic audio against them. Empty (always NO_MATCH) until reference
 * .wav files are added under that folder and the database is (re)built.
 */
object CryFingerprintRepository {
    private const val ASSET_DIR = "cries"
    private const val MIN_MATCH_VOTES = 8

    private val _loadedCount = MutableStateFlow(0)
    val loadedCount: StateFlow<Int> = _loadedCount.asStateFlow()

    private var index: Map<Long, List<IndexEntry>> = emptyMap()
    private var referenceHashCounts: Map<String, Int> = emptyMap()
    private var loaded = false

    suspend fun ensureLoaded(context: Context) {
        if (!loaded) rebuild(context)
    }

    suspend fun rebuild(context: Context) {
        val assetManager = context.assets
        val files = try {
            assetManager.list(ASSET_DIR).orEmpty()
        } catch (e: IOException) {
            emptyArray()
        }

        val newIndex = mutableMapOf<Long, MutableList<IndexEntry>>()
        val hashCounts = mutableMapOf<String, Int>()
        var count = 0

        for (fileName in files) {
            if (!fileName.endsWith(".wav", ignoreCase = true)) continue
            val tagId = fileName.substringBeforeLast(".")
            val entry = PokemonCryCatalog.byTagId(tagId) ?: continue
            val pcm = try {
                assetManager.open("$ASSET_DIR/$fileName").use { WavDecoder.decode(it) }
            } catch (e: Exception) {
                continue
            }
            val hashes = AudioFingerprinter.generate(pcm.samples, pcm.sampleRate)
            if (hashes.isEmpty()) continue
            for (h in hashes) {
                newIndex.getOrPut(h.hash) { mutableListOf() } += IndexEntry(entry.tagId, h.frameIndex)
            }
            hashCounts[entry.tagId] = hashes.size
            count++
        }

        index = newIndex
        referenceHashCounts = hashCounts
        _loadedCount.value = count
        loaded = true
    }

    /**
     * Returns every tag whose vote count is at (or near) the best score, not just the single
     * top scorer. Several cards reuse the same cry across star tiers (Sceptile at both 4-star
     * and 5-star, Pikachu as both a regular tag and a 5-star card, etc.) — when the same
     * reference clip is bundled under more than one tagId, those tagIds tie almost exactly, and
     * the audio alone genuinely can't tell them apart, so callers should present all of them as
     * possible outcomes rather than picking one arbitrarily.
     */
    fun match(samples: ShortArray, sampleRate: Int): List<CryMatch> {
        val queryHashes = AudioFingerprinter.generate(samples, sampleRate)
        if (queryHashes.isEmpty() || index.isEmpty()) return emptyList()

        val votes = mutableMapOf<Pair<String, Int>, Int>()
        for (q in queryHashes) {
            val candidates = index[q.hash] ?: continue
            for (c in candidates) {
                val offset = c.frameIndex - q.frameIndex
                val key = c.tagId to offset
                votes[key] = (votes[key] ?: 0) + 1
            }
        }

        val maxVotes = votes.values.maxOrNull() ?: return emptyList()
        if (maxVotes < MIN_MATCH_VOTES) return emptyList()

        val tieThreshold = (maxVotes * 0.9).toInt().coerceAtLeast(MIN_MATCH_VOTES)
        val bestVotesByTag = mutableMapOf<String, Int>()
        for ((key, voteCount) in votes) {
            if (voteCount < tieThreshold) continue
            val tagId = key.first
            if (voteCount > (bestVotesByTag[tagId] ?: 0)) bestVotesByTag[tagId] = voteCount
        }

        return bestVotesByTag.mapNotNull { (tagId, voteCount) ->
            val entry = PokemonCryCatalog.byTagId(tagId) ?: return@mapNotNull null
            val referenceSize = referenceHashCounts[tagId] ?: queryHashes.size
            val denominator = min(queryHashes.size, referenceSize).coerceAtLeast(1)
            val confidence = (voteCount * 100 / denominator).coerceIn(1, 99)
            CryMatch(entry, confidence)
        }.sortedByDescending { it.confidencePercent }
    }
}
