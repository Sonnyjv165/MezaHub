package com.example.mezahub.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val POKEBALL_RED = Color(0xFFE3350D)
private val POKEBALL_WHITE = Color(0xFFF5F5F5)

/**
 * Pokéball-styled mic button shared by Listen screen states. While [isListening], [micLevel]
 * (0f-1f, live input volume) makes the button bob in size in near-real-time with the sound
 * being picked up, on top of the constant decorative pulsing ring.
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

    Box(modifier = modifier.size(140.dp), contentAlignment = Alignment.Center) {
        if (isListening) {
            PulsingRing()
        }
        Box(
            modifier = Modifier
                .size(96.dp)
                .scale(bobScale)
                .clip(CircleShape)
                .background(POKEBALL_WHITE)
                .border(2.dp, Color.Black.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            // Red top half of the ball.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.TopCenter)
                    .background(POKEBALL_RED, RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp)),
            )
            // Black center band.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .align(Alignment.Center)
                    .background(Color.Black),
            )
            // Center knob.
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(POKEBALL_WHITE)
                    .border(3.dp, Color.Black, CircleShape),
            )

            IconButton(onClick = onClick, modifier = Modifier.size(96.dp)) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "Mic button",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun PulsingRing() {
    val transition = rememberInfiniteTransition(label = "mic-pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart,
        ),
        label = "mic-pulse-scale",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart,
        ),
        label = "mic-pulse-alpha",
    )
    Box(
        modifier = Modifier
            .size(96.dp)
            .scale(scale)
            .alpha(alpha)
            .background(POKEBALL_RED, CircleShape),
    )
}
