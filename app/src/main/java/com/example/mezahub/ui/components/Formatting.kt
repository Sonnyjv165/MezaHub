package com.example.mezahub.ui.components

import android.text.format.DateFormat
import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Short date + time (e.g. "Sep 2, 11:29 PM", "9月2日 23:29") in the app's current locale — read from
 * the Compose configuration so an in-app language change applies without a restart.
 */
@Composable
fun formatTimestamp(millis: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    val pattern = DateFormat.getBestDateTimePattern(locale, "MMMdjmm")
    return SimpleDateFormat(pattern, locale).format(Date(millis))
}

/** Plural string lookup (Compose's pluralStringResource is still experimental in this version). */
@Composable
fun quantityString(@PluralsRes id: Int, count: Int, vararg args: Any): String =
    LocalContext.current.resources.getQuantityString(id, count, *args)
