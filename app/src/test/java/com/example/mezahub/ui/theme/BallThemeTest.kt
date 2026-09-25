package com.example.mezahub.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class BallThemeTest {

    @Test
    fun generationsOneAndTwoAreAvailable() {
        val byGeneration = BallTheme.entries.groupBy { it.generation }.mapValues { it.value.size }
        assertEquals(mapOf(1 to 5, 2 to 8), byGeneration)
    }

    @Test
    fun everyThemeHasItsOwnName() {
        assertEquals(BallTheme.entries.size, BallTheme.entries.map { it.nameRes }.toSet().size)
    }

    @Test
    fun fromName_roundTripsAndFallsBackToDefault() {
        BallTheme.entries.forEach { assertEquals(it, BallTheme.fromName(it.name)) }
        assertEquals(BallTheme.DEFAULT, BallTheme.fromName(null))
        assertEquals(BallTheme.DEFAULT, BallTheme.fromName("DREAM_BALL")) // unknown / removed theme
        assertEquals(BallTheme.POKE_BALL, BallTheme.DEFAULT)
    }
}
