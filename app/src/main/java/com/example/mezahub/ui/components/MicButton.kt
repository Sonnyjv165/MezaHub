package com.example.mezahub.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.mezahub.R
import com.example.mezahub.ui.theme.LocalBallTheme

/**
 * Poké Ball mic button shared by Listen screen states. While [isListening], [micLevel]
 * (0f-1f, live input volume) makes the ball bob in size in near-real-time with the sound being
 * picked up, on top of a constant decorative pulsing ring.
 */
@Composable
fun MicButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    micLevel: Float = 0f,
) {
    val bobScale by animateFloatAsState(
        targetValue = 1f + micLevel.coerceIn(0f, 1f) * 0.3f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "mic-bob",
    )

    val description = stringResource(if (isListening) R.string.cd_stop_listening else R.string.cd_start_listening)

    Box(modifier = modifier.size(170.dp), contentAlignment = Alignment.Center) {
        if (isListening) PulsingRing()
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(bobScale)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            Pokeball(size = 120.dp)
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = null,
                tint = Color(0xFF1A1A1A),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun PulsingRing() {
    val transition = rememberInfiniteTransition(label = "mic-pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1200), repeatMode = RepeatMode.Restart),
        label = "mic-pulse-scale",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1200), repeatMode = RepeatMode.Restart),
        label = "mic-pulse-alpha",
    )
    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale)
            .alpha(alpha)
            .background(LocalBallTheme.current.ball.top, CircleShape),
    )
}
