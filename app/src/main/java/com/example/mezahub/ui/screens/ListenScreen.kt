package com.example.mezahub.ui.screens

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mezahub.R
import com.example.mezahub.audio.MicErrorReason
import com.example.mezahub.data.PokemonCryCatalog
import com.example.mezahub.data.StarTier
import com.example.mezahub.model.CryOutcome
import com.example.mezahub.model.ListenStatus
import com.example.mezahub.ui.components.MicButton
import com.example.mezahub.ui.components.PokedexHeader
import com.example.mezahub.ui.components.Pokeball
import com.example.mezahub.ui.components.PokeballWatermark
import com.example.mezahub.ui.components.PokemonIcon
import com.example.mezahub.ui.components.RarityBackground
import com.example.mezahub.ui.components.StarRow
import com.example.mezahub.ui.components.quantityString
import com.example.mezahub.ui.components.rarityStyleFor
import com.example.mezahub.ui.theme.BallStyle
import com.example.mezahub.viewmodel.ListenViewModel
import kotlinx.coroutines.delay

/** Listen screen. Drives off real mic capture + fingerprint matching via [ListenViewModel]. */
@Composable
fun ListenScreen(modifier: Modifier = Modifier, viewModel: ListenViewModel = viewModel()) {
    val status by viewModel.status.collectAsState()
    val result by viewModel.result.collectAsState()
    val amplitude by viewModel.amplitude.collectAsState()
    val showConfidence by viewModel.showConfidence.collectAsState()
    val errorReason by viewModel.errorReason.collectAsState()
    val permissionRevoked by viewModel.permissionRevoked.collectAsState()
    val loadedCryCount by viewModel.loadedCryCount.collectAsState()
    val activeVersion by viewModel.activeVersion.collectAsState()
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf(context.hasMicPermission()) }
    var showRationale by remember { mutableStateOf(false) }
    // Once the system stops showing its prompt ("Don't ask again", or a second denial), only
    // system settings can grant the permission — so point the user there instead.
    var permanentlyDenied by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        if (granted) {
            permanentlyDenied = false
            viewModel.onMicTapped()
        } else {
            val activity = context.findActivity()
            permanentlyDenied = activity != null &&
                !activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Re-check on return (e.g. from system settings), and stop a capture if the app is left
    // mid-listen — Android feeds silence to backgrounded recorders anyway.
    val currentStatus by rememberUpdatedState(status)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    hasPermission = context.hasMicPermission()
                    if (hasPermission) permanentlyDenied = false
                }
                Lifecycle.Event.ON_PAUSE -> if (currentStatus == ListenStatus.LISTENING) viewModel.onMicTapped()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val haptics = LocalHapticFeedback.current
    LaunchedEffect(status, result) {
        when (status) {
            ListenStatus.RESULT -> {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                if (result.any { it.tier == StarTier.SUPERSTAR }) {
                    delay(160)
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
            ListenStatus.NO_MATCH -> haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            else -> Unit
        }
    }

    val onMicClick: () -> Unit = {
        when {
            status != ListenStatus.IDLE || hasPermission -> viewModel.onMicTapped()
            permanentlyDenied -> context.openAppSettings()
            else -> showRationale = true
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        PokedexHeader(
            title = stringResource(R.string.app_name),
            subtitle = stringResource(R.string.app_tagline),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            PokeballWatermark(size = 340.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!hasPermission && permanentlyDenied && status == ListenStatus.IDLE) {
                    PermissionBlockedBanner(onOpenSettings = { context.openAppSettings() })
                    Spacer(modifier = Modifier.height(24.dp))
                }
                ListenStateBody(
                    status = status,
                    result = result,
                    micLevel = amplitude,
                    showConfidence = showConfidence,
                    errorMessage = micErrorMessage(errorReason, permissionRevoked),
                    databaseEmpty = loadedCryCount == 0,
                    activeVersion = activeVersion.number,
                    versionHasCards = PokemonCryCatalog.cards(activeVersion).isNotEmpty(),
                    onMicTapped = onMicClick,
                    onReset = viewModel::reset,
                )
            }
        }
    }

    if (showRationale) {
        MicRationaleDialog(
            onAllow = {
                showRationale = false
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            onDismiss = { showRationale = false },
        )
    }
}

@Composable
private fun ListenStateBody(
    status: ListenStatus,
    result: List<CryOutcome>,
    micLevel: Float,
    showConfidence: Boolean,
    errorMessage: String,
    databaseEmpty: Boolean,
    activeVersion: Int,
    versionHasCards: Boolean,
    onMicTapped: () -> Unit,
    onReset: () -> Unit,
) {
    when (status) {
        ListenStatus.IDLE -> {
            MicButton(isListening = false, onClick = onMicTapped)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.listen_idle_title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.listen_idle_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.listen_active_version, activeVersion),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
        ListenStatus.LISTENING -> {
            MicButton(isListening = true, onClick = onMicTapped, micLevel = micLevel)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.listen_listening_title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.listen_listening_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ListenStatus.PROCESSING -> {
            WobblingPokeball()
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.listen_processing_title), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Text(
                text = stringResource(R.string.listen_processing_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ListenStatus.RESULT -> BouncedIn(key = result) {
            ResultCard(outcomes = result, showConfidence = showConfidence, onListenAgain = onReset)
        }
        ListenStatus.NO_MATCH -> NoMatchBody(
            message = when {
                !versionHasCards -> stringResource(R.string.version_empty, activeVersion)
                databaseEmpty -> stringResource(R.string.no_match_empty_db)
                else -> stringResource(R.string.no_match_hint)
            },
            onTryAgain = onReset,
        )
        ListenStatus.MIC_ERROR -> MicErrorBody(message = errorMessage, onTryAgain = onReset)
    }
}

/** A Poké Ball rocking on its base, like one mid-catch — the "identifying" indicator. */
@Composable
private fun WobblingPokeball() {
    val transition = rememberInfiniteTransition(label = "wobble")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1400
                0f at 0
                -20f at 150
                18f at 400
                -10f at 600
                0f at 750
                0f at 1400
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "wobble-angle",
    )
    Pokeball(
        size = 110.dp,
        modifier = Modifier.graphicsLayer {
            rotationZ = angle
            transformOrigin = TransformOrigin(0.5f, 1f)
        },
    )
}

/** Scales [content] in from small, overshoots slightly past full size, then settles — a bob. */
@Composable
private fun BouncedIn(key: Any, content: @Composable () -> Unit) {
    val scale = remember(key) { Animatable(0.4f) }
    LaunchedEffect(key) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
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
private fun ResultCard(outcomes: List<CryOutcome>, showConfidence: Boolean, onListenAgain: () -> Unit) {
    val style = rarityStyleFor(outcomes)
    val textColor = style?.textColor ?: MaterialTheme.colorScheme.onSecondaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = if (style != null) {
            CardDefaults.cardColors(containerColor = Color.Transparent)
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        },
        border = style?.let { BorderStroke(2.dp, it.borderColor) },
        elevation = CardDefaults.cardElevation(defaultElevation = if (style != null) 8.dp else 1.dp),
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
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = style.textColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(style.labelRes),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = style.textColor,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = style.textColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (outcomes.size == 1) {
                    val outcome = outcomes.first()
                    PokemonIcon(
                        tagId = outcome.tagId,
                        version = outcome.version,
                        speciesName = outcome.speciesName,
                        tier = outcome.tier,
                        size = style?.iconSize ?: 96.dp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.listen_result_single, outcome.speciesName),
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StarRow(count = outcome.tier.stars, tint = textColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (showConfidence) {
                            stringResource(R.string.tier_with_confidence, stringResource(outcome.tier.labelRes), outcome.confidencePercent)
                        } else {
                            stringResource(outcome.tier.labelRes)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.8f),
                    )
                } else {
                    Text(
                        text = quantityString(R.plurals.listen_result_multi, outcomes.size, outcomes.size),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                    )
                    Text(
                        text = stringResource(R.string.listen_result_multi_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.8f),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        outcomes.forEach { outcome ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PokemonIcon(
                                    tagId = outcome.tagId,
                                    version = outcome.version,
                                    speciesName = outcome.speciesName,
                                    tier = outcome.tier,
                                    size = 48.dp,
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (showConfidence) {
                                            stringResource(
                                                R.string.outcome_line_confidence,
                                                outcome.speciesName,
                                                stringResource(outcome.tier.labelRes),
                                                outcome.confidencePercent,
                                            )
                                        } else {
                                            stringResource(R.string.outcome_line, outcome.speciesName, stringResource(outcome.tier.labelRes))
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor,
                                    )
                                    StarRow(count = outcome.tier.stars, tint = textColor, starSize = 12.dp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onListenAgain) { Text(stringResource(R.string.listen_again)) }
            }
        }
    }
}

@Composable
private fun NoMatchBody(message: String, onTryAgain: () -> Unit) {
    Pokeball(size = 72.dp, style = BallStyle.GREYED, modifier = Modifier.graphicsLayer { alpha = 0.7f })
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = stringResource(R.string.no_match_title), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(20.dp))
    OutlinedButton(onClick = onTryAgain) { Text(stringResource(R.string.try_again)) }
}

@Composable
private fun MicErrorBody(message: String, onTryAgain: () -> Unit) {
    Icon(
        imageVector = Icons.Filled.MicOff,
        contentDescription = null,
        modifier = Modifier.size(56.dp),
        tint = MaterialTheme.colorScheme.error,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = stringResource(R.string.mic_error_title), style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(20.dp))
    OutlinedButton(onClick = onTryAgain) { Text(stringResource(R.string.try_again)) }
}

@Composable
private fun PermissionBlockedBanner(onOpenSettings: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.mic_blocked_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.mic_blocked_body),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onOpenSettings) { Text(stringResource(R.string.open_settings)) }
        }
    }
}

@Composable
private fun MicRationaleDialog(onAllow: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Pokeball(size = 40.dp) },
        title = { Text(stringResource(R.string.mic_rationale_title)) },
        text = { Text(stringResource(R.string.mic_rationale_body)) },
        confirmButton = { Button(onClick = onAllow) { Text(stringResource(R.string.mic_rationale_allow)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.not_now)) } },
    )
}

@Composable
private fun micErrorMessage(reason: MicErrorReason?, permissionRevoked: Boolean): String = stringResource(
    when {
        permissionRevoked -> R.string.mic_error_permission_revoked
        reason == MicErrorReason.UNSUPPORTED_FORMAT -> R.string.mic_error_unsupported
        reason == MicErrorReason.OPEN_FAILED -> R.string.mic_error_open_failed
        reason == MicErrorReason.BUSY -> R.string.mic_error_busy
        reason == MicErrorReason.STOPPED_RESPONDING -> R.string.mic_error_stopped
        else -> R.string.mic_error_unknown
    },
)

private fun Context.hasMicPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Extremely rare (heavily customized ROMs); the banner text still explains what to do.
    }
}
