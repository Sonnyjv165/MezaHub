package com.example.mezahub.audio.fingerprint

import com.example.mezahub.TestAudio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class FingerprintIndexTest {

    // Disjoint frequency sets so clips A, B and C share no spectral peaks with each other.
    private val setA = listOf(440.0, 1000.0, 2000.0, 3000.0, 5000.0, 7000.0, 600.0, 1500.0)
    private val setB = listOf(520.0, 1150.0, 2200.0, 3400.0, 4600.0, 8000.0, 800.0, 1700.0)
    private val setC = listOf(360.0, 1250.0, 2400.0, 3700.0, 4200.0, 6500.0, 950.0, 1900.0)

    private fun clip(set: List<Double>, rate: Int = TestAudio.RATE): ShortArray =
        TestAudio.melody((0 until 16).map { i -> listOf(set[i % 8], set[(i * 3 + 2) % 8]) }, rate = rate)

    private fun hashes(samples: ShortArray, rate: Int = TestAudio.RATE) = AudioFingerprinter.generate(samples, rate)

    private val clipA = clip(setA)
    private val clipB = clip(setB)
    private val index = FingerprintIndex.build(mapOf("A" to hashes(clipA), "B" to hashes(clipB)))

    @Test
    fun clipMatchesItself() {
        val result = index.match(hashes(clipA), minMatchVotes = 8)
        assertEquals(listOf("A"), result.map { it.tagId })
    }

    @Test
    fun matchIsTimeShiftInvariant() {
        // Start listening partway through the clip, as happens at the arcade.
        val lateStart = clipA.copyOfRange(512 * 20, clipA.size)
        assertEquals("A", index.match(hashes(lateStart), minMatchVotes = 8).first().tagId)
    }

    @Test
    fun matchSurvivesBackgroundNoise() {
        val random = Random(42)
        val noisy = ShortArray(clipB.size) { i ->
            (clipB[i] + (random.nextInt(1300) - 650)).coerceIn(-32768, 32767).toShort()
        }
        assertEquals("B", index.match(hashes(noisy), minMatchVotes = 8).first().tagId)
    }

    @Test
    fun micRateCapture_matchesReferenceAtFingerprintRate() {
        // Live captures are 44.1kHz; references are fingerprinted at 22.05kHz.
        val captured = clip(setA, rate = 44100)
        assertEquals("A", index.match(hashes(captured, rate = 44100), minMatchVotes = 8).first().tagId)
    }

    @Test
    fun identicalAudioUnderTwoTags_bothReturnedAsTied() {
        // e.g. the same Grimmsnarl cry bundled for its 6-star, 5-star and 4-star cards.
        val dupIndex = FingerprintIndex.build(
            mapOf("A-6star" to hashes(clipA), "A-5star" to hashes(clipA), "B" to hashes(clipB)),
        )
        val result = dupIndex.match(hashes(clipA), minMatchVotes = 8)
        assertEquals(setOf("A-6star", "A-5star"), result.map { it.tagId }.toSet())
        assertEquals(result[0].votes, result[1].votes)
    }

    @Test
    fun unknownClip_noMatch() {
        assertTrue(index.match(hashes(clip(setC)), minMatchVotes = 4).isEmpty())
    }

    @Test
    fun silence_noMatch() {
        assertTrue(hashes(TestAudio.silence(TestAudio.RATE * 2)).isEmpty())
        assertTrue(index.match(emptyList(), minMatchVotes = 4).isEmpty())
    }

    @Test
    fun belowVoteThreshold_noMatch() {
        assertTrue(index.match(hashes(clipA), minMatchVotes = 1_000_000).isEmpty())
    }

    @Test
    fun emptyIndex_noMatch() {
        assertTrue(FingerprintIndex.EMPTY.match(hashes(clipA), minMatchVotes = 1).isEmpty())
        assertEquals(0, FingerprintIndex.EMPTY.size)
    }

    @Test
    fun confidenceIsWithinBounds() {
        index.match(hashes(clipA), minMatchVotes = 8).forEach { assertTrue(it.confidencePercent in 1..99) }
    }

    @Test
    fun clipShorterThanOneFftWindow_hasNoFingerprint() {
        assertTrue(hashes(ShortArray(500)).isEmpty())
    }

    @Test
    fun fingerprintIsDeterministic() {
        assertEquals(hashes(clipA), hashes(clipA))
    }

    @Test
    fun minMatchVotes_scalesWithSensitivityAndClamps() {
        assertEquals(16, minMatchVotesFor(0f))
        assertEquals(10, minMatchVotesFor(0.5f))
        assertEquals(4, minMatchVotesFor(1f))
        assertEquals(16, minMatchVotesFor(-3f))
        assertEquals(4, minMatchVotesFor(7f))
    }
}
