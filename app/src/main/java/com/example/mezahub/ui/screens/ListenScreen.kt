package com.example.mezahub.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mezahub.model.CryOutcome
import com.example.mezahub.model.ListenStatus
import com.example.mezahub.ui.components.MicButton
import com.example.mezahub.ui.components.PokemonIcon
import com.example.mezahub.ui.components.RarityBackground
import com.example.mezahub.ui.components.StarRow
import com.example.mezahub.ui.components.rarityStyleFor
import com.example.mezahub.viewmodel.ListenViewModel

/** Listen screen. Drives off real mic capture + fingerprint matching via [ListenViewModel]. */
@Composable
fun ListenScreen(modifier: Modifier = Modifier, viewModel: ListenViewModel = viewModel()) {
    val status by viewModel.status.collectAsState()
    val result by viewModel.result.collectAsState()
    val amplitude by viewModel.amplitude.collectAsState()
    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionDenied = !granted
        if (granted) viewModel.onMicTapped()
    }

    val onMicClick: () -> Unit = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        if (status == ListenStatus.IDLE && !hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            permissionDenied = false
            viewModel.onMicTapped()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "MezaHub",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Pokemon Mezastar bonus catch listener for pokemon cries",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(40.dp))

        ListenStateBody(
            status = status,
            result = result,
            micLevel = amplitude,
            onMicTapped = onMicClick,
            onListenAgain = viewModel::reset,
            onTryAgain = viewModel::reset,
        )

        if (permissionDenied) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Microphone permission is required to listen for cries.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ListenStateBody(
    status: ListenStatus,
    result: List<CryOutcome>,
    micLevel: Float,
    onMicTapped: () -> Unit,
    onListenAgain: () -> Unit,
    onTryAgain: () -> Unit,
) {
    when (status) {
        ListenStatus.IDLE -> {
            MicButton(isListening = false, onClick = onMicTapped)
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Tap to Listen", style = MaterialTheme.typography.titleMedium)
        }
        ListenStatus.LISTENING -> {
            MicButton(isListening = true, onClick = onMicTapped, micLevel = micLevel)
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Listening…", style = MaterialTheme.typography.titleMedium)
        }
        ListenStatus.PROCESSING -> {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Identifying…", style = MaterialTheme.typography.titleMedium)
        }
        ListenStatus.RESULT -> {
            BouncedIn(key = result) {
                ResultCard(outcomes = result, onListenAgain = onListenAgain)
            }
        }
        ListenStatus.NO_MATCH -> {
            NoMatchBody(onTryAgain = onTryAgain)
        }
    }
}

/** Scales [content] in from small, overshoots slightly past full size, then settles — a bob. */
@Composable
private fun BouncedIn(key: Any, content: @Composable () -> Unit) {
    val scale = remember(key) { Animatable(0.4f) }
    LaunchedEffect(key) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }
    Column(
        modifier = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
    ) {
        content()
    }
}

@Composable
private fun ResultCard(outcomes: List<CryOutcome>, onListenAgain: () -> Unit) {
    val style = rarityStyleFor(outcomes)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = if (style != null) {
            CardDefaults.cardColors(containerColor = Color.Transparent)
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        },
        border = style?.let { BorderStroke(2.dp, it.borderColor) },
        elevation = if (style != null) {
            CardDefaults.cardElevation(defaultElevation = 8.dp)
        } else {
            CardDefaults.cardElevation()
        },
    ) {
        RarityBackground(style = style, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (style != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = style.textColor,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = style.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = style.textColor,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = style.textColor,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (outcomes.size == 1) {
                    val outcome = outcomes.first()
                    PokemonIcon(
                        tagId = outcome.tagId,
                        speciesName = outcome.speciesName,
                        tier = outcome.tier,
                        size = style?.iconSize ?: 96.dp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = outcome.speciesName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = style?.textColor ?: MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StarRow(count = outcome.tier.stars, tint = style?.textColor ?: MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = outcome.tier.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = style?.textColor?.copy(alpha = 0.8f)
                            ?: MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                } else {
                    Text(
                        text = "Could be one of ${outcomes.size} cards",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = style?.textColor ?: MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "This cry sounds identical across these tags",
                        style = MaterialTheme.typography.bodySmall,
                        color = style?.textColor?.copy(alpha = 0.8f)
                            ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        outcomes.forEach { outcome ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PokemonIcon(
                                    tagId = outcome.tagId,
                                    speciesName = outcome.speciesName,
                                    tier = outcome.tier,
                                    size = 48.dp,
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${outcome.speciesName} — ${outcome.tier.label}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = style?.textColor ?: MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                    StarRow(
                                        count = outcome.tier.stars,
                                        tint = style?.textColor ?: MaterialTheme.colorScheme.onSecondaryContainer,
                                        starSize = 12.dp,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onListenAgain) {
                    Text("Listen Again")
                }
            }
        }
    }
}

@Composable
private fun NoMatchBody(onTryAgain: () -> Unit) {
    Icon(
        imageVector = Icons.Filled.SearchOff,
        contentDescription = null,
        modifier = Modifier.size(56.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = "No cry recognized", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Try holding your device closer to the audio source.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(20.dp))
    OutlinedButton(onClick = onTryAgain) {
        Text("Try Again")
    }
}
