package com.example.mezahub.audio

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class PcmAudio(val sampleRate: Int, val samples: ShortArray)

/** Minimal 16-bit PCM WAV reader, for decoding bundled reference cry clips. */
object WavDecoder {
    private const val FORMAT_PCM = 1
    private const val FORMAT_EXTENSIBLE = 0xFFFE

    fun decode(input: InputStream): PcmAudio {
        val bytes = input.readBytes()
        require(bytes.size > 44) { "File too small to be a valid WAV" }
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        require(String(bytes, 0, 4, Charsets.US_ASCII) == "RIFF") { "Not a RIFF file" }
        require(String(bytes, 8, 4, Charsets.US_ASCII) == "WAVE") { "Not a WAVE file" }

        var pos = 12
        var audioFormat = FORMAT_PCM
        var sampleRate = 0
        var channels = 1
        var bitsPerSample = 16
        var dataOffset = -1
        var dataSize = 0

        while (pos + 8 <= bytes.size) {
            val chunkId = String(bytes, pos, 4, Charsets.US_ASCII)
            val chunkDataStart = pos + 8
            // Clamp to what's actually in the file: streamed/truncated WAVs often declare a
            // bogus (even negative, i.e. > 2GB unsigned) size, which would otherwise read out of
            // bounds or walk `pos` backwards forever.
            val declaredSize = buffer.getInt(pos + 4)
            val chunkSize = if (declaredSize < 0) {
                bytes.size - chunkDataStart
            } else {
                minOf(declaredSize, bytes.size - chunkDataStart)
            }
            when (chunkId) {
                "fmt " -> if (chunkSize >= 16) {
                    audioFormat = buffer.getShort(chunkDataStart).toInt() and 0xFFFF
                    channels = buffer.getShort(chunkDataStart + 2).toInt()
                    sampleRate = buffer.getInt(chunkDataStart + 4)
                    bitsPerSample = buffer.getShort(chunkDataStart + 14).toInt()
                }
                "data" -> {
                    dataOffset = chunkDataStart
                    dataSize = chunkSize
                }
            }
            pos = chunkDataStart + chunkSize + (chunkSize and 1)
        }

        require(dataOffset >= 0) { "No data chunk found" }
        require(audioFormat == FORMAT_PCM || audioFormat == FORMAT_EXTENSIBLE) {
            "Only uncompressed PCM WAV files are supported (format tag $audioFormat)"
        }
        require(bitsPerSample == 16) { "Only 16-bit PCM WAV files are supported" }
        require(sampleRate > 0) { "Invalid sample rate $sampleRate" }
        require(channels >= 1) { "Invalid channel count $channels" }

        val sampleCount = dataSize / 2
        val allSamples = ShortArray(sampleCount)
        for (i in 0 until sampleCount) {
            allSamples[i] = buffer.getShort(dataOffset + i * 2)
        }

        if (channels <= 1) return PcmAudio(sampleRate, allSamples)

        val frames = sampleCount / channels
        val mono = ShortArray(frames)
        for (f in 0 until frames) {
            var sum = 0
            for (c in 0 until channels) sum += allSamples[f * channels + c]
            mono[f] = (sum / channels).toShort()
        }
        return PcmAudio(sampleRate, mono)
    }

    /** Linear-interpolation resample. Good enough for fingerprinting, not for playback quality. */
    fun resample(samples: ShortArray, fromRate: Int, toRate: Int): ShortArray {
        if (fromRate == toRate || samples.isEmpty()) return samples
        val ratio = toRate.toDouble() / fromRate.toDouble()
        val outLength = (samples.size * ratio).toInt().coerceAtLeast(1)
        val output = ShortArray(outLength)
        for (i in 0 until outLength) {
            val srcPos = i / ratio
            val srcIndex = srcPos.toInt().coerceIn(0, samples.size - 1)
            val nextIndex = (srcIndex + 1).coerceAtMost(samples.size - 1)
            val frac = srcPos - srcIndex
            val interpolated = samples[srcIndex] + (samples[nextIndex] - samples[srcIndex]) * frac
            output[i] = interpolated.toInt().toShort()
        }
        return output
    }
}
