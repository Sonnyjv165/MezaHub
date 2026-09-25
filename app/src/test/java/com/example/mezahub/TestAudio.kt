package com.example.mezahub

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/** Synthetic audio helpers so fingerprinting can be tested without real cry recordings. */
object TestAudio {
    const val RATE = 22050

    /**
     * A melody: each segment plays its tones (Hz) together for [segmentMs]. Distinct tone
     * sequences give distinct fingerprints, which is all the matcher needs to tell clips apart.
     */
    fun melody(segments: List<List<Double>>, segmentMs: Int = 120, rate: Int = RATE): ShortArray {
        val perSegment = rate * segmentMs / 1000
        val out = ShortArray(perSegment * segments.size)
        segments.forEachIndexed { s, tones ->
            for (i in 0 until perSegment) {
                val t = (s * perSegment + i).toDouble() / rate
                val v = tones.sumOf { f -> sin(2 * PI * f * t) } / tones.size * 0.5
                out[s * perSegment + i] = (v * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return out
    }

    fun silence(samples: Int): ShortArray = ShortArray(samples)

    /** Encodes samples as a 16-bit PCM WAV file (interleaved if [channels] > 1). */
    fun wavBytes(samples: ShortArray, sampleRate: Int = RATE, channels: Int = 1, formatTag: Int = 1): ByteArray {
        val dataSize = samples.size * 2
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(36 + dataSize)
            put("WAVE".toByteArray(Charsets.US_ASCII))
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16)
            putShort(formatTag.toShort())
            putShort(channels.toShort())
            putInt(sampleRate)
            putInt(sampleRate * channels * 2)
            putShort((channels * 2).toShort())
            putShort(16)
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(dataSize)
        }
        val data = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
        samples.forEach { data.putShort(it) }
        return ByteArrayOutputStream().apply {
            write(header.array())
            write(data.array())
        }.toByteArray()
    }
}
