package com.example.mezahub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mezahub.data.AppLanguage
import com.example.mezahub.data.AppSettingsRepository
import com.example.mezahub.data.CryFingerprintRepository
import com.example.mezahub.data.MezastarVersion
import com.example.mezahub.data.PokemonCryCatalog
import com.example.mezahub.data.SensitivityRepository
import com.example.mezahub.ui.theme.BallTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Cry Database, sensitivity, confidence display, and version info all reflect real, persisted state. */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    val sensitivity: StateFlow<Float> = SensitivityRepository.sensitivity

    val showConfidence: StateFlow<Boolean> = AppSettingsRepository.showConfidence

    val loadedCryCount: StateFlow<Int> = CryFingerprintRepository.loadedCount

    private val _isUpdatingDatabase = MutableStateFlow(false)
    val isUpdatingDatabase: StateFlow<Boolean> = _isUpdatingDatabase.asStateFlow()

    init {
        SensitivityRepository.ensureLoaded(application)
        AppSettingsRepository.ensureLoaded(application)
        viewModelScope.launch { CryFingerprintRepository.ensureLoaded(application, activeVersion.value) }
    }

    fun onSensitivityChange(value: Float) {
        SensitivityRepository.setSensitivity(getApplication(), value)
    }

    fun onShowConfidenceChange(value: Boolean) {
        AppSettingsRepository.setShowConfidence(getApplication(), value)
    }

    val ballTheme: StateFlow<BallTheme> = AppSettingsRepository.ballTheme

    fun onBallThemeChange(theme: BallTheme) {
        AppSettingsRepository.setBallTheme(getApplication(), theme)
    }

    val activeVersion: StateFlow<MezastarVersion> = AppSettingsRepository.activeVersion

    /** Cards in each version's catalog; 0 means that version is still a placeholder. */
    fun cardCount(version: MezastarVersion): Int = PokemonCryCatalog.cards(version).size

    /** Switches the arcade version and re-fingerprints that version's cries (spinner shows meanwhile). */
    fun onActiveVersionChange(version: MezastarVersion) {
        if (version == activeVersion.value) return
        AppSettingsRepository.setActiveVersion(getApplication(), version)
        updateDatabase()
    }

    fun currentLanguage(): AppLanguage = AppLanguage.current()

    /** Switches the app language; AppCompat recreates the activity to apply it. */
    fun onLanguageChange(language: AppLanguage) {
        if (language != AppLanguage.current()) AppLanguage.apply(language)
    }

    fun updateDatabase() {
        viewModelScope.launch {
            _isUpdatingDatabase.value = true
            try {
                CryFingerprintRepository.rebuild(getApplication(), activeVersion.value)
            } finally {
                _isUpdatingDatabase.value = false
            }
        }
    }
}
