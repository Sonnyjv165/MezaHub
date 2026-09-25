package com.example.mezahub.audio

import android.Manifest
import android.annotation.SuppressLint
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

/** Why the mic couldn't be used — mapped to user-facing (translated) text by the UI. */
enum class MicErrorReason { UNSUPPORTED_FORMAT, OPEN_FAILED, BUSY, STOPPED_RESPONDING }

/** The mic exists and permission is granted, but it can't be used right now (busy, broken, etc.). */
class MicUnavailableException(val reason: MicErrorReason) : Exception(reason.name)

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

    /**
     * Records up to [maxDurationMs] of audio, returning early (with what's captured so far) if the
     * calling coroutine is cancelled — e.g. the user tapped the mic to stop listening.
     *
     * @throws SecurityException if the RECORD_AUDIO permission isn't (or is no longer) granted.
     * @throws MicUnavailableException if the mic can't be opened or stops delivering audio —
     *   most often because another app (a call, voice assistant, screen recorder) holds it.
     */
    @SuppressLint("MissingPermission") // Checked explicitly on the first line.
    suspend fun captureClip(maxDurationMs: Int): ShortArray {
        if (!hasPermission()) throw SecurityException("RECORD_AUDIO permission not granted")

        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minBufferSize <= 0) {
            throw MicUnavailableException(MicErrorReason.UNSUPPORTED_FORMAT)
        }

        val recorder = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize * 2,
            )
        } catch (e: IllegalArgumentException) {
            throw MicUnavailableException(MicErrorReason.OPEN_FAILED)
        }
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            throw MicUnavailableException(MicErrorReason.OPEN_FAILED)
        }

        val maxSamples = (SAMPLE_RATE.toLong() * maxDurationMs / 1000L).toInt()
        val output = ShortArray(maxSamples)
        var written = 0

        try {
            try {
                recorder.startRecording()
            } catch (e: IllegalStateException) {
                throw MicUnavailableException(MicErrorReason.BUSY)
            }
            if (recorder.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                throw MicUnavailableException(MicErrorReason.BUSY)
            }
            val chunk = ShortArray(minBufferSize)
            while (written < maxSamples && currentCoroutineContext().isActive) {
                val read = recorder.read(chunk, 0, chunk.size)
                if (read < 0) throw MicUnavailableException(MicErrorReason.STOPPED_RESPONDING)
                if (read == 0) break
                val toCopy = minOf(read, maxSamples - written)
                System.arraycopy(chunk, 0, output, written, toCopy)
                written += toCopy
                _amplitude.value = rmsLevel(chunk, read)
            }
        } finally {
            if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) recorder.stop()
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
