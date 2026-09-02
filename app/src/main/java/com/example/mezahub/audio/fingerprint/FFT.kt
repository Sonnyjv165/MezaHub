package com.example.mezahub.audio.fingerprint

/** Iterative radix-2 Cooley-Tukey FFT, in place. Array size must be a power of two. */
object FFT {
    fun transform(real: DoubleArray, imag: DoubleArray) {
        val n = real.size
        require(n > 0 && (n and (n - 1)) == 0) { "FFT size must be a power of two" }

        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j or bit
            if (i < j) {
                val tr = real[i]; real[i] = real[j]; real[j] = tr
                val ti = imag[i]; imag[i] = imag[j]; imag[j] = ti
            }
        }

        var len = 2
        while (len <= n) {
            val ang = -2.0 * Math.PI / len
            val wr = Math.cos(ang)
            val wi = Math.sin(ang)
            var i = 0
            while (i < n) {
                var curWr = 1.0
                var curWi = 0.0
                for (k in 0 until len / 2) {
                    val evenR = real[i + k]
                    val evenI = imag[i + k]
                    val oddR = real[i + k + len / 2] * curWr - imag[i + k + len / 2] * curWi
                    val oddI = real[i + k + len / 2] * curWi + imag[i + k + len / 2] * curWr
                    real[i + k] = evenR + oddR
                    imag[i + k] = evenI + oddI
                    real[i + k + len / 2] = evenR - oddR
                    imag[i + k + len / 2] = evenI - oddI
                    val nextWr = curWr * wr - curWi * wi
                    val nextWi = curWr * wi + curWi * wr
                    curWr = nextWr
                    curWi = nextWi
                }
                i += len
            }
            len = len shl 1
        }
    }
}
