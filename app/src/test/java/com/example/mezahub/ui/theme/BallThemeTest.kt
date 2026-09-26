package com.example.mezahub.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BallThemeTest {

    /** Every catching ball from the main series, grouped by the generation that introduced it. */
    @Test
    fun everyGenerationsBallsAreAvailable() {
        val byGeneration = BallTheme.entries.groupBy { it.generation }.mapValues { it.value.size }
        // Gens VI and IX added no new balls; Gen VIII's are Legends: Arceus (Hisui).
        assertEquals(mapOf(1 to 5, 2 to 8, 3 to 7, 4 to 5, 5 to 1, 7 to 1, 8 to 11), byGeneration)
    }

    @Test
    fun everyThemeHasItsOwnName() {
        assertEquals(BallTheme.entries.size, BallTheme.entries.map { it.nameRes }.toSet().size)
    }

    @Test
    fun fromName_roundTripsAndFallsBackToDefault() {
        BallTheme.entries.forEach { assertEquals(it, BallTheme.fromName(it.name)) }
        assertEquals(BallTheme.DEFAULT, BallTheme.fromName(null))
        assertEquals(BallTheme.DEFAULT, BallTheme.fromName("GS_BALL")) // unknown / removed theme
        assertEquals(BallTheme.POKE_BALL, BallTheme.DEFAULT)
    }

    /**
     * Theme colors carry text: the header title, button labels, and primary-colored headings and
     * links on the page. 3:1 is the WCAG minimum for bold/large text and UI components.
     */
    @Test
    fun themeColorsStayReadableInLightAndDarkMode() {
        val problems = mutableListOf<String>()
        fun check(theme: BallTheme, what: String, fg: Color, bg: Color) {
            val ratio = contrast(fg, bg)
            if (ratio < 3f) problems += "${theme.name} $what: %.1f:1".format(ratio)
        }
        for (theme in BallTheme.entries) {
            check(theme, "header text", theme.onHeader, theme.header)
            for (dark in listOf(false, true)) {
                val scheme = colorSchemeFor(theme, dark)
                val mode = if (dark) "dark" else "light"
                check(theme, "$mode button text", scheme.onPrimary, scheme.primary)
                check(theme, "$mode primary text", scheme.primary, scheme.surface)
            }
        }
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    private fun contrast(a: Color, b: Color): Float {
        val (light, dark) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (light + 0.05f) / (dark + 0.05f)
    }
}
