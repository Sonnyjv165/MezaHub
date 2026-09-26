package com.example.mezahub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mezahub.audio.AudioCapture
import com.example.mezahub.audio.MicErrorReason
import com.example.mezahub.audio.MicUnavailableException
import com.example.mezahub.audio.RecordingBuffer
import com.example.mezahub.data.AppSettingsRepository
import com.example.mezahub.data.CryFingerprintRepository
import com.example.mezahub.data.CryMatch
import com.example.mezahub.data.DetectionHistoryRepository
import com.example.mezahub.data.MezastarVersion
import com.example.mezahub.data.SensitivityRepository
import com.example.mezahub.model.CryOutcome
import com.example.mezahub.model.ListenStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Listen for up to MAX_LISTEN_MS, checking the latest MATCH_WINDOW_MS every MATCH_INTERVAL_MS
// (once at least MIN_MATCH_AUDIO_MS is in), and stop at the first recognized cry.
private const val MAX_LISTEN_MS = 10_000
private const val MATCH_WINDOW_MS = 4000
private const val MATCH_INTERVAL_MS = 750L
private const val MIN_MATCH_AUDIO_MS = 1500

/** Drives real mic capture and fingerprint matching against [CryFingerprintRepository]. */
class ListenViewModel(application: Application) : AndroidViewModel(application) {

    private val audioCapture = AudioCapture(application)

    private val _status = MutableStateFlow(ListenStatus.IDLE)
    val status: StateFlow<ListenStatus> = _status.asStateFlow()

    private val _result = MutableStateFlow<List<CryOutcome>>(emptyList())
    val result: StateFlow<List<CryOutcome>> = _result.asStateFlow()

    /** Why the last capture ended in [ListenStatus.MIC_ERROR]; null means an unexpected failure. */
    private val _errorReason = MutableStateFlow<MicErrorReason?>(null)
    val errorReason: StateFlow<MicErrorReason?> = _errorReason.asStateFlow()

    /** True when the last [ListenStatus.MIC_ERROR] was caused by the permission being revoked. */
    private val _permissionRevoked = MutableStateFlow(false)
    val permissionRevoked: StateFlow<Boolean> = _permissionRevoked.asStateFlow()

    /** Live mic input level (0f-1f) while listening, for the bobbing mic animation. */
    val amplitude: StateFlow<Float> = audioCapture.amplitude

    /** Whether to show the match confidence percentage — a Settings toggle. */
    val showConfidence: StateFlow<Boolean> = AppSettingsRepository.showConfidence

    /** The arcade version whose cards are being listened for — a Settings choice. */
    val activeVersion: StateFlow<MezastarVersion> = AppSettingsRepository.activeVersion

    /** How many reference cries are loaded — 0 means every capture will come back NO_MATCH. */
    val loadedCryCount: StateFlow<Int> = CryFingerprintRepository.loadedCount

    private var captureJob: Job? = null

    init {
        SensitivityRepository.ensureLoaded(application)
        AppSettingsRepository.ensureLoaded(application)
        viewModelScope.launch { CryFingerprintRepository.ensureLoaded(application, activeVersion.value) }
    }

    fun onMicTapped() {
        when (_status.value) {
            ListenStatus.IDLE -> startListening()
            ListenStatus.LISTENING -> cancelListening()
            else -> Unit
        }
    }

    fun reset() {
        captureJob?.cancel()
        _status.value = ListenStatus.IDLE
        _result.value = emptyList()
        _errorReason.value = null
        _permissionRevoked.value = false
    }

    private fun startListening() {
        _status.value = ListenStatus.LISTENING
        captureJob = viewModelScope.launch {
            try {
                val matches = listenUntilMatch()
                if (matches.isNotEmpty()) {
                    val outcomes = matches.map { match ->
                        CryOutcome(
                            tagId = match.entry.tagId,
                            speciesName = match.entry.speciesName,
                            tier = match.entry.tier,
                            confidencePercent = match.confidencePercent,
                            version = match.entry.version,
                        )
                    }.sortedByDescending { it.tier.stars }
                    _result.value = outcomes
                    DetectionHistoryRepository.record(getApplication(), outcomes)
                    _status.value = ListenStatus.RESULT
                } else {
                    _result.value = emptyList()
                    _status.value = ListenStatus.NO_MATCH
                }
            } catch (e: CancellationException) {
                _status.value = ListenStatus.IDLE
                throw e
            } catch (e: MicUnavailableException) {
                showError(reason = e.reason)
            } catch (e: SecurityException) {
                showError(permissionRevoked = true)
            } catch (e: Exception) {
                // Anything unexpected should land on a recoverable screen, never crash the app.
                showError()
            }
        }
    }

    /**
     * Records for up to [MAX_LISTEN_MS], matching the most recent [MATCH_WINDOW_MS] as it goes, and
     * stops as soon as a cry is recognized — so a cry that starts late is still caught, and an
     * early one is identified without waiting out the clock. Mic failures propagate to the caller.
     */
    private suspend fun listenUntilMatch(): List<CryMatch> = coroutineScope {
        val sampleRate = AudioCapture.SAMPLE_RATE
        val recording = RecordingBuffer(sampleRate * MAX_LISTEN_MS / 1000)
        val capture = launch(Dispatchers.IO) {
            audioCapture.captureClip(MAX_LISTEN_MS) { chunk, count -> recording.append(chunk, count) }
        }
        // Loads (or switches) the cry database while the mic is already recording; matching
        // before it's ready would report a false "no match".
        CryFingerprintRepository.ensureLoaded(getApplication(), activeVersion.value)

        val window = sampleRate * MATCH_WINDOW_MS / 1000
        val minAudio = sampleRate * MIN_MATCH_AUDIO_MS / 1000
        suspend fun matchLatest() = withContext(Dispatchers.Default) {
            CryFingerprintRepository.match(recording.latest(window), sampleRate)
        }
        while (capture.isActive) {
            delay(MATCH_INTERVAL_MS)
            if (recording.size < minAudio) continue
            val matches = matchLatest()
            if (matches.isNotEmpty()) {
                capture.cancel() // coroutineScope still waits for it, so the mic is released first.
                return@coroutineScope matches
            }
        }
        // Time's up: one last look covering the audio since the previous check.
        _status.value = ListenStatus.PROCESSING
        matchLatest()
    }

    private fun showError(reason: MicErrorReason? = null, permissionRevoked: Boolean = false) {
        _errorReason.value = reason
        _permissionRevoked.value = permissionRevoked
        _result.value = emptyList()
        _status.value = ListenStatus.MIC_ERROR
    }

    private fun cancelListening() {
        captureJob?.cancel()
        _status.value = ListenStatus.IDLE
    }
}
