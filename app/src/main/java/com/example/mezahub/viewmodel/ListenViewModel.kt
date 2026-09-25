package com.example.mezahub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mezahub.audio.AudioCapture
import com.example.mezahub.audio.MicErrorReason
import com.example.mezahub.audio.MicUnavailableException
import com.example.mezahub.data.AppSettingsRepository
import com.example.mezahub.data.CryFingerprintRepository
import com.example.mezahub.data.DetectionHistoryRepository
import com.example.mezahub.data.MezastarVersion
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
                val samples = withContext(Dispatchers.IO) {
                    audioCapture.captureClip(CAPTURE_DURATION_MS)
                }
                _status.value = ListenStatus.PROCESSING
                // Tapping Listen right after launch (or a version switch) could otherwise match
                // against a still-empty or stale index and report a false "no match".
                CryFingerprintRepository.ensureLoaded(getApplication(), activeVersion.value)
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
