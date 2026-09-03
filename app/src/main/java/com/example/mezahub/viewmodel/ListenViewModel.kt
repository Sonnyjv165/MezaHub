package com.example.mezahub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mezahub.audio.AudioCapture
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

    /** Live mic input level (0f-1f) while listening, for the bobbing mic animation. */
    val amplitude: StateFlow<Float> = audioCapture.amplitude

    /** Whether to show the match confidence percentage — a Settings toggle. */
    val showConfidence: StateFlow<Boolean> = AppSettingsRepository.showConfidence

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
    }

    private fun startListening() {
        _status.value = ListenStatus.LISTENING
        captureJob = viewModelScope.launch {
            try {
                val samples = withContext(Dispatchers.IO) {
                    audioCapture.captureClip(CAPTURE_DURATION_MS)
                }
                _status.value = ListenStatus.PROCESSING
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
                    }
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
            }
        }
    }

    private fun cancelListening() {
        captureJob?.cancel()
        _status.value = ListenStatus.IDLE
    }
}
