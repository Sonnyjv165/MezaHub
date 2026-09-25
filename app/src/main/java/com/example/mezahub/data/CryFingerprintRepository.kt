package com.example.mezahub.data

import android.content.Context
import com.example.mezahub.audio.WavDecoder
import com.example.mezahub.audio.fingerprint.AudioFingerprinter
import com.example.mezahub.audio.fingerprint.FingerprintHash
import com.example.mezahub.audio.fingerprint.FingerprintIndex
import com.example.mezahub.audio.fingerprint.minMatchVotesFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException

data class CryMatch(val entry: PokemonCryEntry, val confidencePercent: Int)

/**
 * Holds fingerprints generated from bundled reference clips (assets/cries/<tagId>.wav, 16-bit
 * PCM) and matches captured mic audio against them. Empty (always NO_MATCH) until reference
 * .wav files are added under that folder and the database is (re)built.
 */
object CryFingerprintRepository {
    private const val ASSET_DIR = "cries"

    private val _loadedCount = MutableStateFlow(0)
    val loadedCount: StateFlow<Int> = _loadedCount.asStateFlow()

    // Rebuilds are serialized so Listen and Settings starting up together don't fingerprint
    // every clip twice; `index` is swapped in atomically once a rebuild finishes.
    private val mutex = Mutex()
    @Volatile private var index: FingerprintIndex = FingerprintIndex.EMPTY
    @Volatile private var loaded = false

    /** Builds the index if it hasn't been built yet; suspends until it's ready. */
    suspend fun ensureLoaded(context: Context) {
        if (loaded) return
        mutex.withLock { if (!loaded) rebuildLocked(context) }
    }

    suspend fun rebuild(context: Context) {
        mutex.withLock { rebuildLocked(context) }
    }

    // Fingerprinting ~70 clips is CPU-heavy — keep it off the main thread.
    private suspend fun rebuildLocked(context: Context) = withContext(Dispatchers.Default) {
        val assetManager = context.assets
        val files = try {
            assetManager.list(ASSET_DIR).orEmpty()
        } catch (e: IOException) {
            emptyArray()
        }

        val references = HashMap<String, List<FingerprintHash>>()
        for (fileName in files) {
            if (!fileName.endsWith(".wav", ignoreCase = true)) continue
            val entry = PokemonCryCatalog.byTagId(fileName.substringBeforeLast(".")) ?: continue
            val pcm = try {
                assetManager.open("$ASSET_DIR/$fileName").use { WavDecoder.decode(it) }
            } catch (e: Exception) {
                continue // A single bad clip shouldn't take the whole database down.
            }
            val hashes = AudioFingerprinter.generate(pcm.samples, pcm.sampleRate)
            if (hashes.isNotEmpty()) references[entry.tagId] = hashes
        }

        index = FingerprintIndex.build(references)
        _loadedCount.value = index.size
        loaded = true
    }

    /**
     * Returns every card whose score is at (or near) the best, not just the single top scorer.
     * Several cards reuse the same cry across star tiers (Sceptile at both 4-star and 5-star,
     * Pikachu as both a regular tag and a 5-star card, etc.) — the audio alone genuinely can't
     * tell them apart, so callers should present all of them as possible outcomes.
     */
    fun match(samples: ShortArray, sampleRate: Int): List<CryMatch> {
        val current = index
        if (current.isEmpty()) return emptyList()
        val queryHashes = AudioFingerprinter.generate(samples, sampleRate)
        val minVotes = minMatchVotesFor(SensitivityRepository.sensitivity.value)
        return current.match(queryHashes, minVotes).mapNotNull { match ->
            PokemonCryCatalog.byTagId(match.tagId)?.let { CryMatch(it, match.confidencePercent) }
        }
    }
}
