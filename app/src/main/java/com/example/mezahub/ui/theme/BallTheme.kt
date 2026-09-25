package com.example.mezahub.ui.theme

import androidx.annotation.StringRes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.mezahub.R

/** Decoration drawn on a ball's top half. Simplified, code-drawn takes on each ball's design. */
enum class BallPattern {
    PLAIN,
    SIDE_PATCHES, // Great Ball's red patches, Ultra Ball's yellow "H", Friend Ball's leaves
    SPOTS, // Safari Ball camo
    MASTER, // pink bumps + "M"
    HEART, // Love Ball
    CRESCENT, // Moon Ball
    CENTER_STRIPE, // one band across the top half
    TWO_BANDS, // two bands across the top half
}

private val BallWhite = Color(0xFFF5F5F5)

data class BallStyle(
    val top: Color,
    val bottom: Color = BallWhite,
    val accent: Color = Color.Transparent,
    val accent2: Color = Color.Transparent,
    val pattern: BallPattern = BallPattern.PLAIN,
) {
    companion object {
        /** Neutral grey ball for empty / "nothing here" states, independent of the chosen theme. */
        val GREYED = BallStyle(top = Color(0xFF9E9E9E))
    }
}

/**
 * A selectable app theme based on a Poké Ball. [header] paints the Pokédex header and status
 * bar; [primary]/[secondary] seed the Material color scheme; [ball] is how every Poké Ball in the
 * app (mic button, loaders, empty states) is drawn.
 */
enum class BallTheme(
    @StringRes val nameRes: Int,
    val generation: Int,
    val ball: BallStyle,
    val header: Color,
    val headerLip: Color,
    val onHeader: Color,
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
) {
    POKE_BALL(
        R.string.ball_poke, 1,
        BallStyle(top = Color(0xFFDC0A2D)),
        header = Color(0xFFDC0A2D), headerLip = Color(0xFF8B0000), onHeader = Color.White,
        primary = Color(0xFFDC0A2D), onPrimary = Color.White, secondary = Color(0xFFFFCB05),
    ),
    GREAT_BALL(
        R.string.ball_great, 1,
        BallStyle(top = Color(0xFF2A6ACF), accent = Color(0xFFE53935), pattern = BallPattern.SIDE_PATCHES),
        header = Color(0xFF2A6ACF), headerLip = Color(0xFF163F80), onHeader = Color.White,
        primary = Color(0xFF2A6ACF), onPrimary = Color.White, secondary = Color(0xFFE53935),
    ),
    ULTRA_BALL(
        R.string.ball_ultra, 1,
        BallStyle(top = Color(0xFF2B2B2B), accent = Color(0xFFF7C600), pattern = BallPattern.SIDE_PATCHES),
        header = Color(0xFF2B2B2B), headerLip = Color(0xFFF7C600), onHeader = Color.White,
        primary = Color(0xFFF7C600), onPrimary = Color(0xFF1A1A1A), secondary = Color(0xFF757575),
    ),
    SAFARI_BALL(
        R.string.ball_safari, 1,
        BallStyle(top = Color(0xFF4E9A45), bottom = Color(0xFFF2E7C9), accent = Color(0xFF2E6B2A), pattern = BallPattern.SPOTS),
        header = Color(0xFF3E8E41), headerLip = Color(0xFF1F5E23), onHeader = Color.White,
        primary = Color(0xFF3E8E41), onPrimary = Color.White, secondary = Color(0xFFC9A227),
    ),
    MASTER_BALL(
        R.string.ball_master, 1,
        BallStyle(top = Color(0xFF7B3FB5), accent = Color(0xFFE75EA8), accent2 = Color.White, pattern = BallPattern.MASTER),
        header = Color(0xFF6A2FA5), headerLip = Color(0xFF3D1466), onHeader = Color.White,
        primary = Color(0xFF7B3FB5), onPrimary = Color.White, secondary = Color(0xFFE75EA8),
    ),
    FAST_BALL(
        R.string.ball_fast, 2,
        BallStyle(top = Color(0xFFF6B400), accent = Color(0xFFE53935), pattern = BallPattern.SIDE_PATCHES),
        header = Color(0xFFF6B400), headerLip = Color(0xFFC62828), onHeader = Color(0xFF1A1A1A),
        primary = Color(0xFFE53935), onPrimary = Color.White, secondary = Color(0xFFF6B400),
    ),
    LEVEL_BALL(
        R.string.ball_level, 2,
        BallStyle(top = Color(0xFF3A3A3A), accent = Color(0xFFE53935), accent2 = Color(0xFFF7C600), pattern = BallPattern.TWO_BANDS),
        header = Color(0xFF3A3A3A), headerLip = Color(0xFFE53935), onHeader = Color.White,
        primary = Color(0xFFE53935), onPrimary = Color.White, secondary = Color(0xFFF7C600),
    ),
    LURE_BALL(
        R.string.ball_lure, 2,
        BallStyle(top = Color(0xFF1E88C8), accent = Color(0xFFE53935), pattern = BallPattern.CENTER_STRIPE),
        header = Color(0xFF1E7DB8), headerLip = Color(0xFF0D4C75), onHeader = Color.White,
        primary = Color(0xFF1E7DB8), onPrimary = Color.White, secondary = Color(0xFFE53935),
    ),
    HEAVY_BALL(
        R.string.ball_heavy, 2,
        BallStyle(top = Color(0xFF6B7C8D), accent = Color(0xFF34404C), accent2 = Color(0xFF34404C), pattern = BallPattern.TWO_BANDS),
        header = Color(0xFF55667A), headerLip = Color(0xFF2B3642), onHeader = Color.White,
        primary = Color(0xFF55667A), onPrimary = Color.White, secondary = Color(0xFF9FB3C8),
    ),
    LOVE_BALL(
        R.string.ball_love, 2,
        BallStyle(top = Color(0xFFF07BB6), accent = Color(0xFFD81B60), pattern = BallPattern.HEART),
        header = Color(0xFFD6408E), headerLip = Color(0xFF8E1552), onHeader = Color.White,
        primary = Color(0xFFD6408E), onPrimary = Color.White, secondary = Color(0xFFF7A8CF),
    ),
    FRIEND_BALL(
        R.string.ball_friend, 2,
        BallStyle(top = Color(0xFF43A047), accent = Color(0xFFE53935), pattern = BallPattern.SIDE_PATCHES),
        header = Color(0xFF388E3C), headerLip = Color(0xFF1B5E20), onHeader = Color.White,
        primary = Color(0xFF388E3C), onPrimary = Color.White, secondary = Color(0xFFE53935),
    ),
    MOON_BALL(
        R.string.ball_moon, 2,
        BallStyle(top = Color(0xFF2C3E73), accent = Color(0xFFF7D23A), pattern = BallPattern.CRESCENT),
        header = Color(0xFF2C3E73), headerLip = Color(0xFF141D3A), onHeader = Color.White,
        primary = Color(0xFF4A63B0), onPrimary = Color.White, secondary = Color(0xFFF7D23A),
    ),
    SPORT_BALL(
        R.string.ball_sport, 2,
        BallStyle(top = Color(0xFFEF6C1A), accent = Color(0xFF6D3B12), accent2 = Color(0xFF6D3B12), pattern = BallPattern.TWO_BANDS),
        header = Color(0xFFE0621A), headerLip = Color(0xFF7A3208), onHeader = Color.White,
        primary = Color(0xFFE0621A), onPrimary = Color.White, secondary = Color(0xFF8D5524),
    ),
    ;

    companion object {
        val DEFAULT = POKE_BALL

        /** Falls back to the default for unknown/removed names (e.g. a stale saved preference). */
        fun fromName(name: String?): BallTheme = entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

/** The active ball theme, for components that draw with it (Poké Balls, the Pokédex header). */
val LocalBallTheme = staticCompositionLocalOf { BallTheme.DEFAULT }
