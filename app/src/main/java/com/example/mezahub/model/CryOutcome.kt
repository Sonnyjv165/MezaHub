package com.example.mezahub.model

import com.example.mezahub.data.StarTier

/**
 * One possible identification for a captured cry. A single capture can produce several
 * outcomes when multiple cards share identical reference audio but differ only by star tier
 * (e.g. Sceptile at both 4-star and 5-star) — the audio alone can't disambiguate which card it
 * actually is, so all tied candidates are surfaced instead of guessing one.
 */
data class CryOutcome(
    val tagId: String,
    val speciesName: String,
    val tier: StarTier,
    val confidencePercent: Int,
)
