package com.example.mezahub.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PokedexRedLight,
    onPrimary = Color.White,
    primaryContainer = PokedexRedDark,
    onPrimaryContainer = Color(0xFFFFDADC),
    secondary = PokemonYellow,
    onSecondary = InkDark,
    secondaryContainer = Color(0xFF5C4A00),
    onSecondaryContainer = Color(0xFFFFE9A6),
    tertiary = PokemonBlueLight,
    onTertiary = InkDark,
    background = InkDark,
    onBackground = Color(0xFFE6E6EE),
    surface = SurfaceDark,
    onSurface = Color(0xFFE6E6EE),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC3C6D4),
)

private val LightColorScheme = lightColorScheme(
    primary = PokedexRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDADC),
    onPrimaryContainer = Color(0xFF40000A),
    secondary = PokemonYellowDeep,
    onSecondary = InkDark,
    secondaryContainer = Color(0xFFFFE9A6),
    onSecondaryContainer = Color(0xFF3A2E00),
    tertiary = PokemonBlue,
    onTertiary = Color.White,
    background = PaperLight,
    surface = PaperLight,
    surfaceVariant = Color(0xFFF3E4E4),
)

/**
 * App theme. Dynamic (wallpaper-based) color is deliberately off: it would repaint the app in the
 * user's wallpaper colors and wash out the Pokédex red/yellow branding.
 */
@Composable
fun MezaHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Every screen opens with the red Pokédex header, so the status bar matches it.
            window.statusBarColor = PokedexRed.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
