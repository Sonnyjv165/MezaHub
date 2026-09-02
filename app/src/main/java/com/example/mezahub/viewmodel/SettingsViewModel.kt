package com.example.mezahub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mezahub.data.CryFingerprintRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Sensitivity is still UI-only; Cry Database and version info reflect real repository state. */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _sensitivity = MutableStateFlow(0.5f)
    val sensitivity: StateFlow<Float> = _sensitivity.asStateFlow()

    val loadedCryCount: StateFlow<Int> = CryFingerprintRepository.loadedCount

    private val _isUpdatingDatabase = MutableStateFlow(false)
    val isUpdatingDatabase: StateFlow<Boolean> = _isUpdatingDatabase.asStateFlow()

    init {
        viewModelScope.launch { CryFingerprintRepository.ensureLoaded(getApplication()) }
    }

    fun onSensitivityChange(value: Float) {
        _sensitivity.value = value
    }

    fun updateDatabase() {
        viewModelScope.launch {
            _isUpdatingDatabase.value = true
            CryFingerprintRepository.rebuild(getApplication())
            _isUpdatingDatabase.value = false
        }
    }
}
