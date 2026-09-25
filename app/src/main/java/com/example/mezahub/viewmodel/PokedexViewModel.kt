package com.example.mezahub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mezahub.data.AppSettingsRepository
import com.example.mezahub.data.DetectionHistoryRepository
import com.example.mezahub.data.PokedexStats
import com.example.mezahub.data.computePokedexStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn

/** Pokédex completion for the active Mezastar version, derived live from the detection history. */
class PokedexViewModel(application: Application) : AndroidViewModel(application) {

    val stats: StateFlow<PokedexStats> = combine(
        DetectionHistoryRepository.records,
        AppSettingsRepository.activeVersion,
    ) { records, version -> computePokedexStats(records, version) }
        .flowOn(Dispatchers.Default)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            computePokedexStats(emptyList(), AppSettingsRepository.activeVersion.value),
        )

    init {
        AppSettingsRepository.ensureLoaded(application)
        viewModelScope.launch { DetectionHistoryRepository.ensureLoaded(application) }
    }
}
