package com.example.mezahub.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mezahub.ui.theme.PokedexLensBlue
import com.example.mezahub.ui.theme.PokedexRed
import com.example.mezahub.ui.theme.PokedexRedDark

/**
 * Screen header styled after the classic Pokédex: a red band with the big blue lens and three
 * indicator lights. [actions] sit on the right, tinted for the red background (use Color.White).
 */
@Composable
fun PokedexHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PokedexRed)
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PokedexLens()
            Spacer(modifier = Modifier.width(10.dp))
            IndicatorLights(modifier = Modifier.align(Alignment.Top).padding(top = 2.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }
            actions()
        }
        // Darker lip along the bottom edge, like the device's hinge seam.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(PokedexRedDark),
        )
    }
}

@Composable
private fun PokedexLens() {
    Canvas(modifier = Modifier.size(44.dp)) {
        val r = size.minDimension / 2f
        drawCircle(color = Color.White, radius = r)
        drawCircle(color = Color(0xFF1A1A1A), radius = r, style = Stroke(width = r * 0.08f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFB8E6FF), PokedexLensBlue, Color(0xFF0B5FA8)),
                center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                radius = r * 0.9f,
            ),
            radius = r * 0.72f,
        )
        drawCircle(color = Color.White.copy(alpha = 0.7f), radius = r * 0.16f, center = Offset(center.x - r * 0.28f, center.y - r * 0.28f))
    }
}

@Composable
private fun IndicatorLights(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        listOf(Color(0xFFFF3B3B), Color(0xFFFFD83B), Color(0xFF3BD86B)).forEach { color ->
            Canvas(modifier = Modifier.size(11.dp)) {
                val r = size.minDimension / 2f
                drawCircle(color = color, radius = r)
                drawCircle(color = Color(0xFF1A1A1A), radius = r, style = Stroke(width = r * 0.22f))
                drawCircle(color = Color.White.copy(alpha = 0.5f), radius = r * 0.3f, center = Offset(center.x - r * 0.3f, center.y - r * 0.3f))
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}
