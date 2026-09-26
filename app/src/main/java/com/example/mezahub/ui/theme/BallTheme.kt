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
    NET, // Net Ball mesh
    WAVES, // Dive Ball water
    RINGS, // rings around the button: Nest, Repeat, Cherish, Origin Ball
    RAYS, // lines fanning out from the button: Quick, Beast Ball
    WINGS, // Feather, Wing, Jet Ball
}

private val BallWhite = Color(0xFFF5F5F5)
private val BallInk = Color(0xFF1A1A1A)

data class BallStyle(
    val top: Color,
    val bottom: Color = BallWhite,
    val accent: Color = Color.Transparent,
    val accent2: Color = Color.Transparent,
    val pattern: BallPattern = BallPattern.PLAIN,
    /** The seam band and button ring: red on a Premier Ball, a leather strap on Hisuian balls. */
    val band: Color = BallInk,
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
    // Generation I
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
        primary = Color(0xFF8F6A00), onPrimary = Color.White, secondary = Color(0xFFF7C600),
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
    // Generation II
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
    // Generation III
    NET_BALL(
        R.string.ball_net, 3,
        BallStyle(top = Color(0xFF2BA6B8), accent = Color(0xFF1A1A1A), pattern = BallPattern.NET),
        header = Color(0xFF147A87), headerLip = Color(0xFF0B4A52), onHeader = Color.White,
        primary = Color(0xFF147A87), onPrimary = Color.White, secondary = Color(0xFF7FD6E0),
    ),
    DIVE_BALL(
        R.string.ball_dive, 3,
        BallStyle(top = Color(0xFF1E6FD9), accent = Color(0xFF8FD8FA), pattern = BallPattern.WAVES),
        header = Color(0xFF1B5FBF), headerLip = Color(0xFF0E3673), onHeader = Color.White,
        primary = Color(0xFF1B5FBF), onPrimary = Color.White, secondary = Color(0xFF8FD8FA),
    ),
    NEST_BALL(
        R.string.ball_nest, 3,
        BallStyle(top = Color(0xFF7CB342), accent = Color(0xFFE6D86A), pattern = BallPattern.RINGS),
        header = Color(0xFF4C7F24), headerLip = Color(0xFF2C4D12), onHeader = Color.White,
        primary = Color(0xFF4C7F24), onPrimary = Color.White, secondary = Color(0xFFE6D86A),
    ),
    REPEAT_BALL(
        R.string.ball_repeat, 3,
        BallStyle(top = Color(0xFFF26B1D), accent = Color(0xFFFFD23F), pattern = BallPattern.RINGS),
        header = Color(0xFFC44A0C), headerLip = Color(0xFF7A2C04), onHeader = Color.White,
        primary = Color(0xFFC44A0C), onPrimary = Color.White, secondary = Color(0xFFFFD23F),
    ),
    TIMER_BALL(
        R.string.ball_timer, 3,
        BallStyle(top = Color(0xFFF5F5F5), accent = Color(0xFFE53935), accent2 = Color(0xFF1A1A1A), pattern = BallPattern.TWO_BANDS),
        header = Color(0xFFC62828), headerLip = Color(0xFF7F1414), onHeader = Color.White,
        primary = Color(0xFFC62828), onPrimary = Color.White, secondary = Color(0xFF9E9E9E),
    ),
    LUXURY_BALL(
        R.string.ball_luxury, 3,
        BallStyle(top = Color(0xFF222222), bottom = Color(0xFF2E2E2E), accent = Color(0xFFF2C230), accent2 = Color(0xFFD32F2F), pattern = BallPattern.TWO_BANDS),
        header = Color(0xFF222222), headerLip = Color(0xFFF2C230), onHeader = Color.White,
        primary = Color(0xFF8F6A00), onPrimary = Color.White, secondary = Color(0xFFF2C230),
    ),
    PREMIER_BALL(
        R.string.ball_premier, 3,
        BallStyle(top = Color(0xFFF5F5F5), band = Color(0xFFD32F2F)),
        header = Color(0xFFF5F5F5), headerLip = Color(0xFFD32F2F), onHeader = Color(0xFF1A1A1A),
        primary = Color(0xFFC62828), onPrimary = Color.White, secondary = Color(0xFF9E9E9E),
    ),
    // Generation IV
    DUSK_BALL(
        R.string.ball_dusk, 4,
        BallStyle(top = Color(0xFF2F6B3C), bottom = Color(0xFF2B2B2B), accent = Color(0xFF1A1A1A), accent2 = Color(0xFFE57B2E), pattern = BallPattern.TWO_BANDS),
        header = Color(0xFF24532E), headerLip = Color(0xFFE57B2E), onHeader = Color.White,
        primary = Color(0xFF2F7A43), onPrimary = Color.White, secondary = Color(0xFFE57B2E),
    ),
    HEAL_BALL(
        R.string.ball_heal, 4,
        BallStyle(top = Color(0xFFF48FB1), accent = Color.White, pattern = BallPattern.CENTER_STRIPE),
        header = Color(0xFFC2185B), headerLip = Color(0xFF7A0E39), onHeader = Color.White,
        primary = Color(0xFFC2185B), onPrimary = Color.White, secondary = Color(0xFFF8BBD0),
    ),
    QUICK_BALL(
        R.string.ball_quick, 4,
        BallStyle(top = Color(0xFF2F6FD6), accent = Color(0xFFFFD000), pattern = BallPattern.RAYS),
        header = Color(0xFF2A5DB8), headerLip = Color(0xFF163470), onHeader = Color.White,
        primary = Color(0xFF2A5DB8), onPrimary = Color.White, secondary = Color(0xFFFFD000),
    ),
    CHERISH_BALL(
        R.string.ball_cherish, 4,
        BallStyle(top = Color(0xFFD32F2F), bottom = Color(0xFFB71C1C), accent = Color(0xFFFF8A80), pattern = BallPattern.RINGS),
        header = Color(0xFFB71C1C), headerLip = Color(0xFF6E0F0F), onHeader = Color.White,
        primary = Color(0xFFB71C1C), onPrimary = Color.White, secondary = Color(0xFFFF8A80),
    ),
    PARK_BALL(
        R.string.ball_park, 4,
        BallStyle(top = Color(0xFFF2B705), accent = Color(0xFF3E9B4F), pattern = BallPattern.CENTER_STRIPE),
        header = Color(0xFFF2B705), headerLip = Color(0xFF3E9B4F), onHeader = Color(0xFF1A1A1A),
        primary = Color(0xFF2E7D32), onPrimary = Color.White, secondary = Color(0xFFF2B705),
    ),
    // Generation V
    DREAM_BALL(
        R.string.ball_dream, 5,
        BallStyle(top = Color(0xFFF48FB1), bottom = Color(0xFFFBE0EC), accent = Color(0xFF9C4DCC), pattern = BallPattern.SIDE_PATCHES),
        header = Color(0xFFB0306E), headerLip = Color(0xFF6B1A42), onHeader = Color.White,
        primary = Color(0xFFB0306E), onPrimary = Color.White, secondary = Color(0xFFC39BE0),
    ),
    // Generation VII
    BEAST_BALL(
        R.string.ball_beast, 7,
        BallStyle(top = Color(0xFF2A3F9E), bottom = Color(0xFF1E2E75), accent = Color(0xFFF5D33B), pattern = BallPattern.RAYS),
        header = Color(0xFF22347F), headerLip = Color(0xFFF5D33B), onHeader = Color.White,
        primary = Color(0xFF3450C0), onPrimary = Color.White, secondary = Color(0xFFF5D33B),
    ),
    // Generation VIII (Legends: Arceus)
    STRANGE_BALL(
        R.string.ball_strange, 8,
        BallStyle(top = Color(0xFF4A4E69), accent = Color(0xFF9AD1D4), pattern = BallPattern.SPOTS),
        header = Color(0xFF4A4E69), headerLip = Color(0xFF2A2C3D), onHeader = Color.White,
        primary = Color(0xFF5A5F80), onPrimary = Color.White, secondary = Color(0xFF9AD1D4),
    ),
    FEATHER_BALL(
        R.string.ball_feather, 8,
        BallStyle(top = Color(0xFF7EC8E3), bottom = Color(0xFFB07D4F), accent = Color.White, band = Color(0xFF4A2C17), pattern = BallPattern.WINGS),
        header = Color(0xFF2F7FA3), headerLip = Color(0xFF4A2C17), onHeader = Color.White,
        primary = Color(0xFF2F7FA3), onPrimary = Color.White, secondary = Color(0xFFC9A27A),
    ),
    WING_BALL(
        R.string.ball_wing, 8,
        BallStyle(top = Color(0xFF2F8FD0), bottom = Color(0xFFB07D4F), accent = Color.White, band = Color(0xFF4A2C17), pattern = BallPattern.WINGS),
        header = Color(0xFF2273AD), headerLip = Color(0xFF4A2C17), onHeader = Color.White,
        primary = Color(0xFF2273AD), onPrimary = Color.White, secondary = Color(0xFFC9A27A),
    ),
    JET_BALL(
        R.string.ball_jet, 8,
        BallStyle(top = Color(0xFF1D3B6F), bottom = Color(0xFFB07D4F), accent = Color(0xFFF2C230), band = Color(0xFF4A2C17), pattern = BallPattern.WINGS),
        header = Color(0xFF1D3B6F), headerLip = Color(0xFF4A2C17), onHeader = Color.White,
        primary = Color(0xFF2D5599), onPrimary = Color.White, secondary = Color(0xFFF2C230),
    ),
    LEADEN_BALL(
        R.string.ball_leaden, 8,
        BallStyle(top = Color(0xFF7A7F85), bottom = Color(0xFFB07D4F), accent = Color(0xFF4A4F55), accent2 = Color(0xFF4A4F55), band = Color(0xFF4A2C17), pattern = BallPattern.TWO_BANDS),
        header = Color(0xFF5E6369), headerLip = Color(0xFF4A2C17), onHeader = Color.White,
        primary = Color(0xFF5E6369), onPrimary = Color.White, secondary = Color(0xFFB0B5BA),
    ),
    GIGATON_BALL(
        R.string.ball_gigaton, 8,
        BallStyle(top = Color(0xFF3B3F45), bottom = Color(0xFFB07D4F), accent = Color(0xFF1E2023), accent2 = Color(0xFF1E2023), band = Color(0xFF4A2C17), pattern = BallPattern.TWO_BANDS),
        header = Color(0xFF3B3F45), headerLip = Color(0xFF4A2C17), onHeader = Color.White,
        primary = Color(0xFF565C64), onPrimary = Color.White, secondary = Color(0xFFC9A27A),
    ),
    ORIGIN_BALL(
        R.string.ball_origin, 8,
        BallStyle(top = Color(0xFFEDE6CC), bottom = Color(0xFFEDE6CC), accent = Color(0xFFC9A227), band = Color(0xFF8C6D1F), pattern = BallPattern.RINGS),
        header = Color(0xFF8C6D1F), headerLip = Color(0xFF4F3D0F), onHeader = Color.White,
        primary = Color(0xFF8C6D1F), onPrimary = Color.White, secondary = Color(0xFFEDE6CC),
    ),
    HISUI_POKE_BALL(
        R.string.ball_hisui_poke, 8,
        BallStyle(top = Color(0xFFB5452B), bottom = Color(0xFFB07D4F), band = Color(0xFF4A2C17)),
        header = Color(0xFF9C3A22), headerLip = Color(0xFF4A2C17), onHeader = Color.White,
        primary = Color(0xFF9C3A22), onPrimary = Color.White, secondary = Color(0xFFC9A27A),
    ),
    HISUI_GREAT_BALL(
        R.string.ball_hisui_great, 8,
        BallStyle(top = Color(0xFF3B6FB6), bottom = Color(0xFFB07D4F), accent = Color(0xFFB5452B), band = Color(0xFF4A2C17), pattern = BallPattern.SIDE_PATCHES),
        header = Color(0xFF2F5C99), headerLip = Color(0xFF4A2C17), onHeader = Color.White,
        primary = Color(0xFF2F5C99), onPrimary = Color.White, secondary = Color(0xFFC9A27A),
    ),
    HISUI_ULTRA_BALL(
        R.string.ball_hisui_ultra, 8,
        BallStyle(top = Color(0xFF3A3A3A), bottom = Color(0xFFB07D4F), accent = Color(0xFFE0B53A), band = Color(0xFF4A2C17), pattern = BallPattern.SIDE_PATCHES),
        header = Color(0xFF3A3A3A), headerLip = Color(0xFFE0B53A), onHeader = Color.White,
        primary = Color(0xFF8F6A00), onPrimary = Color.White, secondary = Color(0xFFE0B53A),
    ),
    HISUI_HEAVY_BALL(
        R.string.ball_hisui_heavy, 8,
        BallStyle(top = Color(0xFF6B7C8D), bottom = Color(0xFFB07D4F), accent = Color(0xFF34404C), accent2 = Color(0xFF34404C), band = Color(0xFF4A2C17), pattern = BallPattern.TWO_BANDS),
        header = Color(0xFF55667A), headerLip = Color(0xFF4A2C17), onHeader = Color.White,
        primary = Color(0xFF55667A), onPrimary = Color.White, secondary = Color(0xFF9FB3C8),
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
