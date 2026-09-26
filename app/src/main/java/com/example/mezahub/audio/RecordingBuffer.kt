package com.example.mezahub.audio

/**
 * Audio recorded so far, filled by the capture thread while the matcher reads the most recent
 * stretch of it. Fixed [capacity]; samples past it are dropped.
 */
class RecordingBuffer(capacity: Int) {
    private val samples = ShortArray(capacity)
    private var filled = 0

    /** Number of samples recorded so far. */
    val size: Int
        @Synchronized get() = filled

    @Synchronized
    fun append(chunk: ShortArray, count: Int) {
        val n = minOf(count, samples.size - filled)
        if (n <= 0) return
        System.arraycopy(chunk, 0, samples, filled, n)
        filled += n
    }

    /** A copy of the last [maxCount] samples (fewer if less has been recorded). */
    @Synchronized
    fun latest(maxCount: Int): ShortArray = samples.copyOfRange(maxOf(0, filled - maxCount), filled)
}
