package com.example.mezahub.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Row of [count] filled stars (e.g. Zygarde -> 6 stars). Renders nothing for count <= 0. */
@Composable
fun StarRow(count: Int, tint: Color, modifier: Modifier = Modifier, starSize: Dp = 15.dp) {
    if (count <= 0) return
    Row(modifier = modifier) {
        repeat(count) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(starSize),
            )
        }
    }
}
