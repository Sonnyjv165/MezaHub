package com.example.mezahub.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "mezahub_prefs"
private const val KEY_SENSITIVITY = "sensitivity"
const val DEFAULT_SENSITIVITY = 0.5f

/**
 * Shared cry-matching sensitivity (0f = strictest, 1f = most lenient), persisted via
 * SharedPreferences so it survives app restarts. Read directly by
 * [CryFingerprintRepository] at match time.
 */
object SensitivityRepository {
    private val _sensitivity = MutableStateFlow(DEFAULT_SENSITIVITY)
    val sensitivity: StateFlow<Float> = _sensitivity.asStateFlow()

    private var loaded = false

    fun ensureLoaded(context: Context) {
        if (loaded) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _sensitivity.value = prefs.getFloat(KEY_SENSITIVITY, DEFAULT_SENSITIVITY)
        loaded = true
    }

    fun setSensitivity(context: Context, value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        _sensitivity.value = clamped
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_SENSITIVITY, clamped)
            .apply()
    }
}
