package com.example.mezahub.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlin.math.sqrt

/** Captures short mono PCM clips from the device mic. No fingerprinting happens here. */
class AudioCapture(private val context: Context) {

    companion object {
        const val SAMPLE_RATE = 44100

        // Typical speech RMS sits well below full scale; boost it so the level meter actually
        // moves for normal talking/game-audio volume instead of sitting near 0.
        private const val AMPLITUDE_GAIN = 4f
    }

    private val _amplitude = MutableStateFlow(0f)

    /** Normalized (0f-1f) input level, updated live while [captureClip] is recording. 0 when idle. */
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    /** Records up to [maxDurationMs] of audio, returning early (with what's captured so far)
     *  if the calling coroutine is cancelled — e.g. the user tapped the mic to stop listening. */
    suspend fun captureClip(maxDurationMs: Int): ShortArray {
        require(hasPermission()) { "RECORD_AUDIO permission not granted" }

        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        require(minBufferSize > 0) { "Unable to configure AudioRecord for this device" }

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize * 2,
        )

        val maxSamples = (SAMPLE_RATE.toLong() * maxDurationMs / 1000L).toInt()
        val output = ShortArray(maxSamples)
        var written = 0

        try {
            recorder.startRecording()
            val chunk = ShortArray(minBufferSize)
            while (written < maxSamples && currentCoroutineContext().isActive) {
                val read = recorder.read(chunk, 0, chunk.size)
                if (read <= 0) break
                val toCopy = minOf(read, maxSamples - written)
                System.arraycopy(chunk, 0, output, written, toCopy)
                written += toCopy
                _amplitude.value = rmsLevel(chunk, read)
            }
        } finally {
            recorder.stop()
            recorder.release()
            _amplitude.value = 0f
        }

        return output.copyOf(written)
    }

    private fun rmsLevel(chunk: ShortArray, sampleCount: Int): Float {
        if (sampleCount <= 0) return 0f
        var sumSquares = 0.0
        for (i in 0 until sampleCount) {
            val normalized = chunk[i] / 32768.0
            sumSquares += normalized * normalized
        }
        val rms = sqrt(sumSquares / sampleCount)
        return (rms * AMPLITUDE_GAIN).toFloat().coerceIn(0f, 1f)
    }
}
