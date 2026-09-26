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
 * Holds fingerprints generated from one Mezastar version's bundled reference clips
 * (assets/versions/v<N>/cries/<tagId>.wav, 16-bit PCM) and matches captured mic audio against
 * them. Only one version is indexed at a time — the active one from [AppSettingsRepository];
 * a version with no clips yet simply has an empty index (always NO_MATCH).
 */
object CryFingerprintRepository {
    private val _loadedCount = MutableStateFlow(0)
    val loadedCount: StateFlow<Int> = _loadedCount.asStateFlow()

    /** An index together with the version it was built from, so the two can never be read out of step. */
    private class LoadedIndex(val version: MezastarVersion, val index: FingerprintIndex)

    // Rebuilds are serialized so Listen and Settings starting up together don't fingerprint
    // every clip twice; `loaded` is swapped in as one object once a rebuild finishes.
    private val mutex = Mutex()
    @Volatile private var loaded: LoadedIndex? = null

    /** Builds [version]'s index if it isn't the one already loaded; suspends until it's ready. */
    suspend fun ensureLoaded(context: Context, version: MezastarVersion) {
        if (loaded?.version == version) return
        mutex.withLock { if (loaded?.version != version) rebuildLocked(context, version) }
    }

    suspend fun rebuild(context: Context, version: MezastarVersion) {
        mutex.withLock { rebuildLocked(context, version) }
    }

    // Fingerprinting ~70 clips is CPU-heavy — keep it off the main thread.
    private suspend fun rebuildLocked(context: Context, version: MezastarVersion) = withContext(Dispatchers.Default) {
        val assetManager = context.assets
        val files = try {
            assetManager.list(version.criesDir).orEmpty()
        } catch (e: IOException) {
            emptyArray()
        }

        val references = HashMap<String, List<FingerprintHash>>()
        for (fileName in files) {
            if (!fileName.endsWith(".wav", ignoreCase = true)) continue
            val entry = PokemonCryCatalog.byTagId(version, fileName.substringBeforeLast(".")) ?: continue
            val pcm = try {
                assetManager.open("${version.criesDir}/$fileName").use { WavDecoder.decode(it) }
            } catch (e: Exception) {
                continue // A single bad clip shouldn't take the whole database down.
            }
            val hashes = AudioFingerprinter.generate(pcm.samples, pcm.sampleRate)
            if (hashes.isNotEmpty()) references[entry.tagId] = hashes
        }

        val built = LoadedIndex(version, FingerprintIndex.build(references))
        loaded = built
        _loadedCount.value = built.index.size
    }

    /**
     * Returns every card whose score is at (or near) the best, not just the single top scorer.
     * Several cards reuse the same cry across star tiers (Sceptile at both 4-star and 5-star,
     * Pikachu as both a regular tag and a 5-star card, etc.) — the audio alone genuinely can't
     * tell them apart, so callers should present all of them as possible outcomes.
     */
    fun match(samples: ShortArray, sampleRate: Int): List<CryMatch> {
        // Read once: a version switch mid-match must not pair one version's index with the other's catalog.
        val current = loaded ?: return emptyList()
        if (current.index.isEmpty()) return emptyList()
        val queryHashes = AudioFingerprinter.generate(samples, sampleRate)
        val minVotes = minMatchVotesFor(SensitivityRepository.sensitivity.value)
        return current.index.match(queryHashes, minVotes).mapNotNull { match ->
            PokemonCryCatalog.byTagId(current.version, match.tagId)?.let { CryMatch(it, match.confidencePercent) }
        }
    }
}
