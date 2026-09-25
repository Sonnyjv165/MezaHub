package com.example.mezahub.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import com.example.mezahub.ui.theme.BallPattern
import com.example.mezahub.ui.theme.BallStyle
import com.example.mezahub.ui.theme.LocalBallTheme

private val BallWhite = Color(0xFFF5F5F5)
private val BallInk = Color(0xFF1A1A1A)

/** A flat, code-drawn ball (defaults to the active theme's ball) — scales cleanly, no assets. */
@Composable
fun Pokeball(size: Dp, modifier: Modifier = Modifier, style: BallStyle = LocalBallTheme.current.ball) {
    Canvas(modifier = modifier.size(size)) { drawBall(style) }
}

private fun DrawScope.drawBall(style: BallStyle) {
    val r = size.minDimension / 2f
    val c = center
    val outline = r * 0.08f
    val box = Size(r * 2, r * 2)
    val topLeft = Offset(c.x - r, c.y - r)

    drawArc(color = style.top, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = topLeft, size = box)
    drawArc(color = style.bottom, startAngle = 0f, sweepAngle = 180f, useCenter = true, topLeft = topLeft, size = box)

    val topHalf = Path().apply {
        arcTo(Rect(topLeft, box), 180f, 180f, forceMoveTo = true)
        close()
    }
    clipPath(topHalf) { drawPattern(style, r, c) }

    drawRect(color = BallInk, topLeft = Offset(c.x - r, c.y - outline / 2), size = Size(r * 2, outline))
    drawCircle(color = BallInk, radius = r - outline / 2, center = c, style = Stroke(width = outline))
    drawCircle(color = BallInk, radius = r * 0.30f, center = c)
    drawCircle(color = BallWhite, radius = r * 0.20f, center = c)
    drawCircle(color = BallInk, radius = r * 0.20f, center = c, style = Stroke(width = outline * 0.5f))
    // Glossy highlight on the top half.
    drawCircle(color = Color.White.copy(alpha = 0.35f), radius = r * 0.14f, center = Offset(c.x - r * 0.45f, c.y - r * 0.5f))
}

private fun DrawScope.drawPattern(style: BallStyle, r: Float, c: Offset) {
    when (style.pattern) {
        BallPattern.PLAIN -> Unit
        BallPattern.SIDE_PATCHES -> {
            drawCircle(color = style.accent, radius = r * 0.52f, center = Offset(c.x - r * 0.95f, c.y - r * 0.30f))
            drawCircle(color = style.accent, radius = r * 0.52f, center = Offset(c.x + r * 0.95f, c.y - r * 0.30f))
        }
        BallPattern.SPOTS -> listOf(
            -0.55f to -0.55f, 0.1f to -0.8f, 0.6f to -0.45f, -0.15f to -0.35f, 0.35f to -0.2f, -0.75f to -0.15f,
        ).forEach { (dx, dy) ->
            drawCircle(color = style.accent, radius = r * 0.16f, center = Offset(c.x + r * dx, c.y + r * dy))
        }
        BallPattern.MASTER -> {
            drawCircle(color = style.accent, radius = r * 0.20f, center = Offset(c.x - r * 0.52f, c.y - r * 0.50f))
            drawCircle(color = style.accent, radius = r * 0.20f, center = Offset(c.x + r * 0.52f, c.y - r * 0.50f))
            val m = Path().apply {
                moveTo(c.x - r * 0.22f, c.y - r * 0.40f)
                lineTo(c.x - r * 0.22f, c.y - r * 0.78f)
                lineTo(c.x, c.y - r * 0.55f)
                lineTo(c.x + r * 0.22f, c.y - r * 0.78f)
                lineTo(c.x + r * 0.22f, c.y - r * 0.40f)
            }
            drawPath(m, color = style.accent2, style = Stroke(width = r * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        BallPattern.HEART -> {
            val s = r * 0.30f
            val hx = c.x
            val hy = c.y - r * 0.52f
            val heart = Path().apply {
                moveTo(hx, hy + s * 0.9f)
                cubicTo(hx - s * 1.7f, hy - s * 0.1f, hx - s * 0.7f, hy - s * 1.3f, hx, hy - s * 0.4f)
                cubicTo(hx + s * 0.7f, hy - s * 1.3f, hx + s * 1.7f, hy - s * 0.1f, hx, hy + s * 0.9f)
                close()
            }
            drawPath(heart, color = style.accent)
        }
        BallPattern.CRESCENT -> {
            val moon = Offset(c.x + r * 0.05f, c.y - r * 0.52f)
            drawCircle(color = style.accent, radius = r * 0.26f, center = moon)
            drawCircle(color = style.top, radius = r * 0.23f, center = Offset(moon.x + r * 0.13f, moon.y - r * 0.08f))
            drawCircle(color = style.accent, radius = r * 0.05f, center = Offset(c.x - r * 0.5f, c.y - r * 0.7f))
            drawCircle(color = style.accent, radius = r * 0.04f, center = Offset(c.x + r * 0.55f, c.y - r * 0.3f))
        }
        BallPattern.CENTER_STRIPE ->
            drawRect(color = style.accent, topLeft = Offset(c.x - r, c.y - r * 0.62f), size = Size(r * 2, r * 0.2f))
        BallPattern.TWO_BANDS -> {
            drawRect(color = style.accent, topLeft = Offset(c.x - r, c.y - r * 0.78f), size = Size(r * 2, r * 0.15f))
            drawRect(color = style.accent2, topLeft = Offset(c.x - r, c.y - r * 0.45f), size = Size(r * 2, r * 0.15f))
        }
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
