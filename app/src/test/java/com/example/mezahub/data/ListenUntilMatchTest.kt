package com.example.mezahub.data

import com.example.mezahub.audio.RecordingBuffer
import com.example.mezahub.audio.WavDecoder
import com.example.mezahub.audio.fingerprint.AudioFingerprinter
import com.example.mezahub.audio.fingerprint.FingerprintIndex
import com.example.mezahub.audio.fingerprint.minMatchVotesFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Random

/**
 * Replays ListenViewModel's listen-until-match loop (check the latest 4 s every 750 ms, up to
 * 10 s, once 1.5 s is in) against the real Version 4 cries, with simulated 44.1 kHz mic audio.
 */
class ListenUntilMatchTest {
    private val rate = 44_100
    private val version = MezastarVersion.V4
    private val cards = PokemonCryCatalog.cards(version)
    private val clips = cards.associate { card ->
        card.tagId to File("src/main/assets/${version.criesDir}/${card.tagId}.wav").inputStream().use { WavDecoder.decode(it) }
    }
    private val index = FingerprintIndex.build(clips.mapValues { (_, pcm) -> AudioFingerprinter.generate(pcm.samples, pcm.sampleRate) })

    /** Background hum at ~5% of full scale, like a noisy arcade floor between cries. */
    private fun noise(seconds: Int, seed: Long): ShortArray {
        val random = Random(seed)
        return ShortArray(rate * seconds) { (random.nextGaussian() * 1600).toInt().coerceIn(-32768, 32767).toShort() }
    }

    /** Mixes [tagId]'s cry (22.05 kHz, upsampled to the mic's 44.1 kHz) into [audio] at [startMs]. */
    private fun withCry(audio: ShortArray, tagId: String, startMs: Int): ShortArray {
        val cry = clips.getValue(tagId).samples
        val out = audio.copyOf()
        val start = rate * startMs / 1000
        for (i in 0 until cry.size * 2) {
            val at = start + i
            if (at >= out.size) break
            out[at] = (out[at] + cry[i / 2] * 0.8).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    /** Returns (time in ms the loop stopped at, matched tag IDs), or null if nothing matched. */
    private fun listen(mic: ShortArray, sensitivity: Float = 0.5f): Pair<Int, Set<String>>? {
        val recording = RecordingBuffer(rate * 10)
        var fed = 0
        for (t in 750..10_000 step 750) {
            val upTo = minOf(mic.size, rate * t / 1000)
            recording.append(mic.copyOfRange(fed, upTo), upTo - fed)
            fed = upTo
            if (recording.size < rate * 1500 / 1000) continue
            val hits = index.match(AudioFingerprinter.generate(recording.latest(rate * 4), rate), minMatchVotesFor(sensitivity))
            if (hits.isNotEmpty()) return t to hits.map { it.tagId }.toSet()
        }
        return null
    }

    @Test
    fun cryStartingLate_isCaughtSoonAfterItPlays() {
        val start = 6_200 // well past the old fixed 4-second capture
        val cry = "1-4-011" // Aggron
        val (stoppedAt, tags) = listen(withCry(noise(10, seed = 1), cry, start)) ?: error("cry was never matched")
        assertEquals(setOf(cry), tags)
        assertTrue("matched at $stoppedAt ms, before the cry began", stoppedAt > start)
        val cryMs = clips.getValue(cry).samples.size * 1000 / 22_050
        assertTrue("matched at $stoppedAt ms, cry ended by ${start + cryMs}", stoppedAt <= start + cryMs + 750)
    }

    @Test
    fun sharedCry_stillReportsEveryCard() {
        val (_, tags) = listen(withCry(noise(10, seed = 2), "1-4-012", startMs = 2_000)) ?: error("cry was never matched")
        assertEquals(setOf("1-4-012", "1-4-030"), tags) // Meganium: Star and 4-star
    }

    @Test
    fun everyCard_isFoundWhenPlayedMidRecording() {
        val misses = cards.filter { card ->
            val expected = cards.filter { it.speciesName == card.speciesName }.map { it.tagId }.toSet()
            listen(withCry(noise(10, seed = card.tagId.hashCode().toLong()), card.tagId, startMs = 3_000))?.second != expected
        }
        assertTrue("wrong or no match for ${misses.map { it.tagId }}", misses.isEmpty())
    }

    /** ~12 checks per listen instead of one, so noise must not get more chances to fake a match. */
    @Test
    fun noiseAlone_neverMatches() {
        for (seed in 1L..20L) assertNull("seed $seed", listen(noise(10, seed)))
    }
}
