package com.example.mezahub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mezahub.data.DetectionHistoryRepository
import com.example.mezahub.data.DetectionRecord
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Real detection log from [DetectionHistoryRepository], persisted across app restarts. */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    val entries: StateFlow<List<DetectionRecord>> = DetectionHistoryRepository.records

    init {
        viewModelScope.launch { DetectionHistoryRepository.ensureLoaded(application) }
    }

    fun removeEntry(record: DetectionRecord) {
        viewModelScope.launch { DetectionHistoryRepository.remove(getApplication(), record.id) }
    }
}
