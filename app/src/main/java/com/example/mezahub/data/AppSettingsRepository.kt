package com.example.mezahub.data

import android.content.Context
import com.example.mezahub.ui.theme.BallTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "mezahub_prefs"
private const val KEY_SHOW_CONFIDENCE = "show_confidence"
private const val KEY_BALL_THEME = "ball_theme"
private const val KEY_ACTIVE_VERSION = "active_version"
const val DEFAULT_SHOW_CONFIDENCE = false

/**
 * Small persisted display settings shared across screens (SharedPreferences-backed, same file
 * as [SensitivityRepository]).
 */
object AppSettingsRepository {
    private val _showConfidence = MutableStateFlow(DEFAULT_SHOW_CONFIDENCE)
    val showConfidence: StateFlow<Boolean> = _showConfidence.asStateFlow()

    private val _ballTheme = MutableStateFlow(BallTheme.DEFAULT)
    val ballTheme: StateFlow<BallTheme> = _ballTheme.asStateFlow()

    /** The Mezastar version the user's arcade runs; only its cards are matched and shown. */
    private val _activeVersion = MutableStateFlow(MezastarVersion.DEFAULT)
    val activeVersion: StateFlow<MezastarVersion> = _activeVersion.asStateFlow()

    @Volatile private var loaded = false

    fun ensureLoaded(context: Context) {
        if (loaded) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _showConfidence.value = prefs.getBoolean(KEY_SHOW_CONFIDENCE, DEFAULT_SHOW_CONFIDENCE)
        _ballTheme.value = BallTheme.fromName(prefs.getString(KEY_BALL_THEME, null))
        _activeVersion.value = MezastarVersion.fromNumber(prefs.getInt(KEY_ACTIVE_VERSION, MezastarVersion.DEFAULT.number))
        loaded = true
    }

    fun setShowConfidence(context: Context, value: Boolean) {
        _showConfidence.value = value
        prefs(context).edit().putBoolean(KEY_SHOW_CONFIDENCE, value).apply()
    }

    fun setBallTheme(context: Context, theme: BallTheme) {
        _ballTheme.value = theme
        prefs(context).edit().putString(KEY_BALL_THEME, theme.name).apply()
    }

    fun setActiveVersion(context: Context, version: MezastarVersion) {
        _activeVersion.value = version
        prefs(context).edit().putInt(KEY_ACTIVE_VERSION, version.number).apply()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
