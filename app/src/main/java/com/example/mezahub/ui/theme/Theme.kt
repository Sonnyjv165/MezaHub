package com.example.mezahub.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun contrastOn(color: Color): Color = if (color.luminance() > 0.45f) InkDark else Color.White

private fun colorSchemeFor(theme: BallTheme, dark: Boolean): ColorScheme {
    val secondary = theme.secondary
    return if (dark) {
        // Nudge primary lighter so it still reads on the near-black background.
        val primary = if (theme.onPrimary == Color.White) lerp(theme.primary, Color.White, 0.2f) else theme.primary
        darkColorScheme(
            primary = primary,
            onPrimary = contrastOn(primary),
            primaryContainer = lerp(theme.primary, Color.Black, 0.55f),
            onPrimaryContainer = lerp(theme.primary, Color.White, 0.8f),
            secondary = secondary,
            onSecondary = contrastOn(secondary),
            secondaryContainer = lerp(secondary, Color.Black, 0.6f),
            onSecondaryContainer = lerp(secondary, Color.White, 0.85f),
            tertiary = PokemonBlueLight,
            background = InkDark,
            onBackground = Color(0xFFE6E6EE),
            surface = SurfaceDark,
            onSurface = Color(0xFFE6E6EE),
            surfaceVariant = SurfaceVariantDark,
            onSurfaceVariant = Color(0xFFC3C6D4),
        )
    } else {
        val paper = lerp(Color.White, theme.primary, 0.03f)
        lightColorScheme(
            primary = theme.primary,
            onPrimary = theme.onPrimary,
            primaryContainer = lerp(theme.primary, Color.White, 0.82f),
            onPrimaryContainer = lerp(theme.primary, Color.Black, 0.7f),
            secondary = secondary,
            onSecondary = contrastOn(secondary),
            secondaryContainer = lerp(secondary, Color.White, 0.72f),
            onSecondaryContainer = lerp(secondary, Color.Black, 0.75f),
            tertiary = PokemonBlue,
            background = paper,
            surface = paper,
            surfaceVariant = lerp(Color.White, theme.primary, 0.10f),
        )
    }
}

/**
 * App theme, colored by the selected [ballTheme]. Dynamic (wallpaper-based) color is deliberately
 * off: it would repaint the app in the user's wallpaper colors and override the chosen ball.
 */
@Composable
fun MezaHubTheme(
    ballTheme: BallTheme = BallTheme.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = remember(ballTheme, darkTheme) { colorSchemeFor(ballTheme, darkTheme) }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Every screen opens with the Pokédex header, so the status bar matches it.
            window.statusBarColor = ballTheme.header.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                ballTheme.header.luminance() > 0.45f
        }
    }

    CompositionLocalProvider(LocalBallTheme provides ballTheme) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
