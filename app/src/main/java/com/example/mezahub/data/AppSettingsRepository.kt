package com.example.mezahub.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "mezahub_prefs"
private const val KEY_SHOW_CONFIDENCE = "show_confidence"
const val DEFAULT_SHOW_CONFIDENCE = false

/**
 * Small persisted display toggles shared across screens (SharedPreferences-backed, same file
 * as [SensitivityRepository]).
 */
object AppSettingsRepository {
    private val _showConfidence = MutableStateFlow(DEFAULT_SHOW_CONFIDENCE)
    val showConfidence: StateFlow<Boolean> = _showConfidence.asStateFlow()

    private var loaded = false

    fun ensureLoaded(context: Context) {
        if (loaded) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _showConfidence.value = prefs.getBoolean(KEY_SHOW_CONFIDENCE, DEFAULT_SHOW_CONFIDENCE)
        loaded = true
    }

    fun setShowConfidence(context: Context, value: Boolean) {
        _showConfidence.value = value
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOW_CONFIDENCE, value)
            .apply()
    }
}
