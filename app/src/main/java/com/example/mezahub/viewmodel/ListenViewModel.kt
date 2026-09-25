package com.example.mezahub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mezahub.audio.AudioCapture
import com.example.mezahub.audio.MicUnavailableException
import com.example.mezahub.data.AppSettingsRepository
import com.example.mezahub.data.CryFingerprintRepository
import com.example.mezahub.data.DetectionHistoryRepository
import com.example.mezahub.data.SensitivityRepository
import com.example.mezahub.model.CryOutcome
import com.example.mezahub.model.ListenStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val CAPTURE_DURATION_MS = 4000

/** Drives real mic capture and fingerprint matching against [CryFingerprintRepository]. */
class ListenViewModel(application: Application) : AndroidViewModel(application) {

    private val audioCapture = AudioCapture(application)

    private val _status = MutableStateFlow(ListenStatus.IDLE)
    val status: StateFlow<ListenStatus> = _status.asStateFlow()

    private val _result = MutableStateFlow<List<CryOutcome>>(emptyList())
    val result: StateFlow<List<CryOutcome>> = _result.asStateFlow()

    /** Human-readable reason for the last [ListenStatus.MIC_ERROR]. */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** Live mic input level (0f-1f) while listening, for the bobbing mic animation. */
    val amplitude: StateFlow<Float> = audioCapture.amplitude

    /** Whether to show the match confidence percentage — a Settings toggle. */
    val showConfidence: StateFlow<Boolean> = AppSettingsRepository.showConfidence

    /** How many reference cries are loaded — 0 means every capture will come back NO_MATCH. */
    val loadedCryCount: StateFlow<Int> = CryFingerprintRepository.loadedCount

    private var captureJob: Job? = null

    init {
        SensitivityRepository.ensureLoaded(application)
        AppSettingsRepository.ensureLoaded(application)
        viewModelScope.launch { CryFingerprintRepository.ensureLoaded(application) }
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
        _errorMessage.value = null
    }

    private fun startListening() {
        _status.value = ListenStatus.LISTENING
        captureJob = viewModelScope.launch {
            try {
                val samples = withContext(Dispatchers.IO) {
                    audioCapture.captureClip(CAPTURE_DURATION_MS)
                }
                _status.value = ListenStatus.PROCESSING
                // Tapping Listen right after launch could otherwise match against a
                // still-empty index and report a false "no match".
                CryFingerprintRepository.ensureLoaded(getApplication())
                val matches = withContext(Dispatchers.Default) {
                    CryFingerprintRepository.match(samples, AudioCapture.SAMPLE_RATE)
                }
                if (matches.isNotEmpty()) {
                    val outcomes = matches.map { match ->
                        CryOutcome(
                            tagId = match.entry.tagId,
                            speciesName = match.entry.speciesName,
                            tier = match.entry.tier,
                            confidencePercent = match.confidencePercent,
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
                showError(e.message ?: "The microphone isn't available right now.")
            } catch (e: SecurityException) {
                showError("Microphone access was turned off. Allow it again to keep listening.")
            } catch (e: Exception) {
                // Anything unexpected should land on a recoverable screen, never crash the app.
                showError("Something went wrong while identifying the cry. Please try again.")
            }
        }
    }

    private fun showError(message: String) {
        _errorMessage.value = message
        _result.value = emptyList()
        _status.value = ListenStatus.MIC_ERROR
    }

    private fun cancelListening() {
        captureJob?.cancel()
        _status.value = ListenStatus.IDLE
    }
}
