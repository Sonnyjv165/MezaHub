package com.example.mezahub.audio.fingerprint

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sin

class FFTTest {

    @Test
    fun impulse_hasFlatUnitSpectrum() {
        val real = DoubleArray(8).also { it[0] = 1.0 }
        val imag = DoubleArray(8)
        FFT.transform(real, imag)
        for (k in 0 until 8) assertEquals(1.0, hypot(real[k], imag[k]), 1e-9)
    }

    @Test
    fun sineOnExactBin_peaksAtThatBin() {
        val n = 256
        val bin = 13
        val real = DoubleArray(n) { i -> sin(2 * PI * bin * i / n) }
        val imag = DoubleArray(n)
        FFT.transform(real, imag)
        val magnitudes = (0 until n / 2).map { hypot(real[it], imag[it]) }
        assertEquals(bin, magnitudes.indices.maxBy { magnitudes[it] })
        assertEquals(n / 2.0, magnitudes[bin], 1e-6) // A unit sine puts N/2 into its bin.
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonPowerOfTwo_isRejected() {
        FFT.transform(DoubleArray(100), DoubleArray(100))
    }
}
