package com.example.mezahub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mezahub.data.DetectionHistoryRepository
import com.example.mezahub.data.PokedexStats
import com.example.mezahub.data.computePokedexStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn

/** Pokédex completion derived live from the detection history. */
class PokedexViewModel(application: Application) : AndroidViewModel(application) {

    val stats: StateFlow<PokedexStats> = DetectionHistoryRepository.records
        .map { computePokedexStats(it) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), computePokedexStats(emptyList()))

    init {
        viewModelScope.launch { DetectionHistoryRepository.ensureLoaded(application) }
    }
}
