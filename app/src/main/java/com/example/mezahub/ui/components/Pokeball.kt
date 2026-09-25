package com.example.mezahub.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.example.mezahub.ui.theme.PokedexRed

private val BallWhite = Color(0xFFF5F5F5)
private val BallInk = Color(0xFF1A1A1A)

/** A flat Poké Ball drawn with Canvas, so it scales cleanly and needs no image asset. */
@Composable
fun Pokeball(size: Dp, modifier: Modifier = Modifier, topColor: Color = PokedexRed) {
    Canvas(modifier = modifier.size(size)) {
        val r = this.size.minDimension / 2f
        val c = center
        val outline = r * 0.08f
        val box = Size(r * 2, r * 2)
        val topLeft = Offset(c.x - r, c.y - r)

        drawArc(color = topColor, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = topLeft, size = box)
        drawArc(color = BallWhite, startAngle = 0f, sweepAngle = 180f, useCenter = true, topLeft = topLeft, size = box)
        drawRect(color = BallInk, topLeft = Offset(c.x - r, c.y - outline / 2), size = Size(r * 2, outline))
        drawCircle(color = BallInk, radius = r - outline / 2, center = c, style = Stroke(width = outline))
        drawCircle(color = BallInk, radius = r * 0.30f, center = c)
        drawCircle(color = BallWhite, radius = r * 0.20f, center = c)
        drawCircle(color = BallInk, radius = r * 0.20f, center = c, style = Stroke(width = outline * 0.5f))
        // Glossy highlight on the red half.
        drawCircle(color = Color.White.copy(alpha = 0.35f), radius = r * 0.14f, center = Offset(c.x - r * 0.45f, c.y - r * 0.5f))
    }
}

/** A faint Poké Ball outline for use as a decorative background watermark. */
@Composable
fun PokeballWatermark(size: Dp, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val r = this.size.minDimension / 2f
        val c = center
        val stroke = r * 0.10f
        drawCircle(color = color, radius = r - stroke / 2, center = c, style = Stroke(width = stroke))
        // Band stops at the center button instead of running through it.
        drawRect(color = color, topLeft = Offset(c.x - r, c.y - stroke / 2), size = Size(r * 0.66f, stroke))
        drawRect(color = color, topLeft = Offset(c.x + r * 0.34f, c.y - stroke / 2), size = Size(r * 0.66f, stroke))
        drawCircle(color = color, radius = r * 0.26f, center = c, style = Stroke(width = stroke))
    }
}
