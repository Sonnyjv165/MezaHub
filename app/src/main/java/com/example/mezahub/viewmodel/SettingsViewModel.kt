package com.example.mezahub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mezahub.data.CryFingerprintRepository
import com.example.mezahub.data.SensitivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Cry Database, sensitivity, and version info all reflect real, persisted state. */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    val sensitivity: StateFlow<Float> = SensitivityRepository.sensitivity

    val loadedCryCount: StateFlow<Int> = CryFingerprintRepository.loadedCount

    private val _isUpdatingDatabase = MutableStateFlow(false)
    val isUpdatingDatabase: StateFlow<Boolean> = _isUpdatingDatabase.asStateFlow()

    init {
        SensitivityRepository.ensureLoaded(application)
        viewModelScope.launch { CryFingerprintRepository.ensureLoaded(application) }
    }

    fun onSensitivityChange(value: Float) {
        SensitivityRepository.setSensitivity(getApplication(), value)
    }

    fun updateDatabase() {
        viewModelScope.launch {
            _isUpdatingDatabase.value = true
            CryFingerprintRepository.rebuild(getApplication())
            _isUpdatingDatabase.value = false
        }
    }
}
