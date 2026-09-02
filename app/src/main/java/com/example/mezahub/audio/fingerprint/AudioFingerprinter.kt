package com.example.mezahub.audio.fingerprint

import com.example.mezahub.audio.WavDecoder
import kotlin.math.min

data class FingerprintHash(val hash: Long, val frameIndex: Int)

/**
 * Lightweight Shazam-style constellation fingerprinting: STFT -> strongest peak per frequency
 * band per frame -> pairwise (anchor, target) hashing. Tuned for short (1-4s), tonal cry clips
 * rather than music, so bands and window sizes are narrower than a typical Shazam clone.
 *
 * Unvalidated until real reference cry audio is loaded — see CryFingerprintRepository.
 */
object AudioFingerprinter {
    const val TARGET_SAMPLE_RATE = 22050
    private const val FFT_SIZE = 1024
    private const val HOP_SIZE = 512
    private val BAND_EDGES_HZ = intArrayOf(300, 700, 1400, 2500, 4000, 6000, 9000)
    private const val FAN_OUT = 5
    private const val MIN_TARGET_DELTA_FRAMES = 1
    private const val MAX_TARGET_DELTA_FRAMES = 64
    private const val MIN_MAGNITUDE = 2.0

    private data class Peak(val frame: Int, val bin: Int)

    fun generate(samples: ShortArray, sampleRate: Int): List<FingerprintHash> {
        val resampled = WavDecoder.resample(samples, sampleRate, TARGET_SAMPLE_RATE)
        if (resampled.size < FFT_SIZE) return emptyList()

        val window = hannWindow(FFT_SIZE)
        val bandBins = BAND_EDGES_HZ.map { hzToBin(it) }
        val peaks = mutableListOf<Peak>()

        val real = DoubleArray(FFT_SIZE)
        val imag = DoubleArray(FFT_SIZE)
        var frameIndex = 0
        var offset = 0
        while (offset + FFT_SIZE <= resampled.size) {
            for (i in 0 until FFT_SIZE) {
                real[i] = resampled[offset + i] / 32768.0 * window[i]
                imag[i] = 0.0
            }
            FFT.transform(real, imag)

            val magnitudes = DoubleArray(FFT_SIZE / 2)
            for (i in magnitudes.indices) magnitudes[i] = Math.hypot(real[i], imag[i])

            for (b in 0 until bandBins.size - 1) {
                val lo = bandBins[b]
                val hi = min(bandBins[b + 1], magnitudes.size - 1)
                if (hi <= lo) continue
                var bestBin = -1
                var bestMag = MIN_MAGNITUDE
                for (bin in lo until hi) {
                    if (magnitudes[bin] > bestMag) {
                        bestMag = magnitudes[bin]
                        bestBin = bin
                    }
                }
                if (bestBin >= 0) peaks += Peak(frameIndex, bestBin)
            }

            frameIndex++
            offset += HOP_SIZE
        }

        val hashes = mutableListOf<FingerprintHash>()
        for (anchorIdx in peaks.indices) {
            val anchor = peaks[anchorIdx]
            var fanned = 0
            for (targetIdx in anchorIdx + 1 until peaks.size) {
                if (fanned >= FAN_OUT) break
                val target = peaks[targetIdx]
                val delta = target.frame - anchor.frame
                if (delta < MIN_TARGET_DELTA_FRAMES) continue
                if (delta > MAX_TARGET_DELTA_FRAMES) break
                val hash = (anchor.bin.toLong() shl 20) or (target.bin.toLong() shl 10) or delta.toLong()
                hashes += FingerprintHash(hash, anchor.frame)
                fanned++
            }
        }
        return hashes
    }

    private fun hzToBin(hz: Int): Int =
        (hz.toDouble() * FFT_SIZE / TARGET_SAMPLE_RATE).toInt().coerceIn(0, FFT_SIZE / 2 - 1)

    private fun hannWindow(size: Int): DoubleArray = DoubleArray(size) { i ->
        0.5 - 0.5 * Math.cos(2.0 * Math.PI * i / (size - 1))
    }
}
