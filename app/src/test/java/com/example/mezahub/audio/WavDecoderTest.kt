package com.example.mezahub.audio

import com.example.mezahub.TestAudio
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavDecoderTest {

    @Test
    fun decodesMonoPcm() {
        val samples = shortArrayOf(0, 1000, -1000, Short.MAX_VALUE, Short.MIN_VALUE, 42)
        val pcm = WavDecoder.decode(ByteArrayInputStream(TestAudio.wavBytes(samples, sampleRate = 44100)))
        assertEquals(44100, pcm.sampleRate)
        assertArrayEquals(samples, pcm.samples)
    }

    @Test
    fun downmixesStereoToMono() {
        // Interleaved L/R frames: (100, 300) -> 200, (-50, 50) -> 0, (1000, 0) -> 500
        val interleaved = shortArrayOf(100, 300, -50, 50, 1000, 0)
        val pcm = WavDecoder.decode(ByteArrayInputStream(TestAudio.wavBytes(interleaved, channels = 2)))
        assertArrayEquals(shortArrayOf(200, 0, 500), pcm.samples)
    }

    @Test
    fun toleratesDataChunkSizeLargerThanFile() {
        // Streamed/truncated WAVs often declare a data size bigger than what's actually there.
        val bytes = TestAudio.wavBytes(ShortArray(100) { it.toShort() })
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putInt(40, Int.MAX_VALUE)
        val pcm = WavDecoder.decode(ByteArrayInputStream(bytes))
        assertEquals(100, pcm.samples.size)
    }

    @Test
    fun toleratesNegativeChunkSize() {
        val bytes = TestAudio.wavBytes(ShortArray(100) { it.toShort() })
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putInt(40, -1) // 0xFFFFFFFF unsigned.
        val pcm = WavDecoder.decode(ByteArrayInputStream(bytes))
        assertEquals(100, pcm.samples.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonPcmFormat() {
        val floatWav = TestAudio.wavBytes(ShortArray(100), formatTag = 3) // 3 = IEEE float
        WavDecoder.decode(ByteArrayInputStream(floatWav))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonWavData() {
        WavDecoder.decode(ByteArrayInputStream(ByteArray(100) { 7 }))
    }

    @Test
    fun resample_scalesLengthByRateRatio() {
        val input = ShortArray(44100) { (it % 100).toShort() }
        assertEquals(22050, WavDecoder.resample(input, 44100, 22050).size)
        assertEquals(88200, WavDecoder.resample(input, 44100, 88200).size)
    }

    @Test
    fun resample_sameRateIsIdentity() {
        val input = shortArrayOf(1, 2, 3)
        assertArrayEquals(input, WavDecoder.resample(input, 22050, 22050))
    }
}
