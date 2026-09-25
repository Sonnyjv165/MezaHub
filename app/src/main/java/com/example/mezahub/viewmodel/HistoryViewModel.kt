package com.example.mezahub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mezahub.data.AppSettingsRepository
import com.example.mezahub.data.DetectionHistoryRepository
import com.example.mezahub.data.DetectionRecord
import com.example.mezahub.data.StarTier
import com.example.mezahub.data.applyHistoryQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Real detection log from [DetectionHistoryRepository], persisted across app restarts, with a
 * session-only (not persisted) sort direction, star-tier filter and text search applied on top —
 * all reset to their defaults (newest-first, no filter, no search) on a fresh app launch.
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    // true = oldest first, false (default) = newest first.
    private val _sortAscending = MutableStateFlow(false)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    // Empty = no filter, show every tier.
    private val _tierFilter = MutableStateFlow<Set<StarTier>>(emptySet())
    val tierFilter: StateFlow<Set<StarTier>> = _tierFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val entries: StateFlow<List<DetectionRecord>> = combine(
        DetectionHistoryRepository.records,
        _sortAscending,
        _tierFilter,
        _searchQuery,
    ) { records, ascending, filter, query -> applyHistoryQuery(records, ascending, filter, query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Unfiltered record count — tells "no catches yet" apart from "nothing matches the filter". */
    val totalCount: StateFlow<Int> = DetectionHistoryRepository.records
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Whether to show the match confidence percentage — a Settings toggle. */
    val showConfidence: StateFlow<Boolean> = AppSettingsRepository.showConfidence

    init {
        AppSettingsRepository.ensureLoaded(application)
        viewModelScope.launch { DetectionHistoryRepository.ensureLoaded(application) }
    }

    fun toggleSortDirection() {
        _sortAscending.value = !_sortAscending.value
    }

    fun toggleTierFilter(tier: StarTier) {
        _tierFilter.value = _tierFilter.value.let { current ->
            if (tier in current) current - tier else current + tier
        }
    }

    fun clearTierFilter() {
        _tierFilter.value = emptySet()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun removeEntry(record: DetectionRecord) {
        viewModelScope.launch { DetectionHistoryRepository.remove(getApplication(), record.id) }
    }

    fun restoreEntry(record: DetectionRecord) {
        viewModelScope.launch { DetectionHistoryRepository.restore(getApplication(), record) }
    }
}
