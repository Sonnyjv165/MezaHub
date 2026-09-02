package com.example.mezahub.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mezahub.R
import com.example.mezahub.data.StarTier
import com.example.mezahub.model.CryOutcome

/** Shared per-tier background look used on both the Listen result card and History rows. */
data class RarityStyle(
    val gradient: Brush? = null,
    @DrawableRes val backgroundImageRes: Int? = null,
    val borderColor: Color,
    val textColor: Color,
    val label: String,
    val iconSize: Dp,
)

val TWO_STYLE = RarityStyle(
    gradient = Brush.linearGradient(listOf(Color(0xFFFF8A80), Color(0xFFE53935))),
    borderColor = Color(0xFFFFB3AB),
    textColor = Color.White,
    label = "2-STAR",
    iconSize = 90.dp,
)

val THREE_STYLE = RarityStyle(
    gradient = Brush.linearGradient(listOf(Color(0xFFB3E5FC), Color(0xFF29B6F6))),
    borderColor = Color(0xFFE1F5FE),
    textColor = Color.Black,
    label = "3-STAR",
    iconSize = 94.dp,
)

val FOUR_STYLE = RarityStyle(
    gradient = Brush.linearGradient(listOf(Color(0xFFFFF59D), Color(0xFFFBC02D))),
    borderColor = Color(0xFFFFF9C4),
    textColor = Color.Black,
    label = "4-STAR",
    iconSize = 98.dp,
)

val FIVE_STYLE = RarityStyle(
    gradient = Brush.linearGradient(listOf(Color(0xFFFFF3C4), Color(0xFFFFD34E), Color(0xFFE8A317))),
    borderColor = Color(0xFFFFE28A),
    textColor = Color.Black,
    label = "5-STAR",
    iconSize = 104.dp,
)

val SIX_STYLE = RarityStyle(
    backgroundImageRes = R.drawable.bg_superstar_galaxy,
    borderColor = Color(0xFFB39DDB),
    textColor = Color.White,
    label = "SUPERSTAR",
    iconSize = 112.dp,
)

/** Highest-tier style present among the outcomes (6 > 5 > 4 > 3 > 2); null for Regular-only. */
fun rarityStyleFor(outcomes: List<CryOutcome>): RarityStyle? {
    val tiers = outcomes.map { it.tier }.toSet()
    return when {
        StarTier.SUPERSTAR in tiers -> SIX_STYLE
        StarTier.STAR in tiers -> FIVE_STYLE
        StarTier.FOUR in tiers -> FOUR_STYLE
        StarTier.THREE in tiers -> THREE_STYLE
        StarTier.TWO in tiers -> TWO_STYLE
        else -> null
    }
}

/**
 * Fills its bounds with [style]'s background (a gradient, or the galaxy image + a dark scrim
 * for legibility for [SIX_STYLE]) before drawing [content] on top. No-op background when
 * [style] is null — caller supplies its own default look in that case.
 */
@Composable
fun RarityBackground(style: RarityStyle?, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = modifier) {
        if (style?.backgroundImageRes != null) {
            Image(
                painter = painterResource(style.backgroundImageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)))
        } else if (style?.gradient != null) {
            Box(modifier = Modifier.matchParentSize().background(style.gradient))
        }
        content()
    }
}
