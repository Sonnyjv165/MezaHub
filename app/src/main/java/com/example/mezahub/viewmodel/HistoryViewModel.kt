package com.example.mezahub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mezahub.data.AppSettingsRepository
import com.example.mezahub.data.DetectionHistoryRepository
import com.example.mezahub.data.DetectionRecord
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Real detection log from [DetectionHistoryRepository], persisted across app restarts. */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    val entries: StateFlow<List<DetectionRecord>> = DetectionHistoryRepository.records

    /** Whether to show the match confidence percentage — a Settings toggle. */
    val showConfidence: StateFlow<Boolean> = AppSettingsRepository.showConfidence

    init {
        AppSettingsRepository.ensureLoaded(application)
        viewModelScope.launch { DetectionHistoryRepository.ensureLoaded(application) }
    }

    fun removeEntry(record: DetectionRecord) {
        viewModelScope.launch { DetectionHistoryRepository.remove(getApplication(), record.id) }
    }
}
