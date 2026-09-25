package com.example.mezahub.ui.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** e.g. "Sep 2, 11:29 PM", in the device's current locale (read per call so a language change applies). */
fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(millis))
