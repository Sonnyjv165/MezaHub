package com.example.mezahub.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mezahub.data.PokemonIconRepository
import com.example.mezahub.data.StarTier

/**
 * Card icon for a result outcome. Loads a real image from assets/versions/v<version>/icons/<tagId>
 * when one has been added; otherwise shows a tier-colored placeholder badge with the species' first letter.
 */
@Composable
fun PokemonIcon(
    tagId: String,
    version: Int,
    speciesName: String,
    tier: StarTier,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    desaturated: Boolean = false,
) {
    val context = LocalContext.current
    var bitmap by remember(version, tagId) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(version, tagId) {
        bitmap = PokemonIconRepository.load(context, version, tagId)
    }

    val current = bitmap
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                when {
                    current != null -> Color.Transparent
                    desaturated -> Color(0xFF8A8A8A)
                    else -> tierColor(tier)
                },
            )
            .alpha(if (desaturated) 0.45f else 1f),
        contentAlignment = Alignment.Center,
    ) {
        if (current != null) {
            Image(
                bitmap = current,
                contentDescription = speciesName,
                modifier = Modifier.size(size),
                colorFilter = if (desaturated) GREYSCALE else null,
            )
        } else {
            Text(
                text = speciesName.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

private val GREYSCALE = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

private fun tierColor(tier: StarTier): Color = when (tier) {
    StarTier.SUPERSTAR -> Color(0xFF3B2E6B)
    StarTier.STAR -> Color(0xFFE8A317)
    StarTier.FOUR -> Color(0xFFCFA100)
    StarTier.THREE -> Color(0xFF1E88C7)
    StarTier.TWO -> Color(0xFFD32F2F)
    StarTier.REGULAR -> Color(0xFF8A8A8A)
}
