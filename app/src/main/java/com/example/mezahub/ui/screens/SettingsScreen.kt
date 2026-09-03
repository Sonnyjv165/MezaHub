package com.example.mezahub.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mezahub.BuildConfig
import com.example.mezahub.R
import com.example.mezahub.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

/** Settings screen. Cry Database, sensitivity, and version info all reflect real, persisted state. */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = viewModel()) {
    val sensitivity by viewModel.sensitivity.collectAsState()
    val showConfidence by viewModel.showConfidence.collectAsState()
    val loadedCount by viewModel.loadedCryCount.collectAsState()
    val isUpdating by viewModel.isUpdatingDatabase.collectAsState()
    var showSensitivityHelp by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(24.dp),
        )

        SectionHeader("Cry Database")
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "$loadedCount cries loaded",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = viewModel::updateDatabase, enabled = !isUpdating) {
                    Text("Update Database")
                }
                if (isUpdating) {
                    Spacer(modifier = Modifier.width(12.dp))
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 24.dp))

        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Sensitivity",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            IconButton(onClick = { showSensitivityHelp = true }, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Filled.HelpOutline,
                    contentDescription = "How to calibrate sensitivity",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "How aggressively MezaHub tries to match a cry",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Slider(value = sensitivity, onValueChange = viewModel::onSensitivityChange)
            Text(
                text = "${(sensitivity * 100).roundToInt()}% — ${sensitivityLabel(sensitivity)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Divider(modifier = Modifier.padding(vertical = 24.dp))

        SectionHeader("Display")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Show match confidence", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Show the % confidence next to identified cries",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = showConfidence, onCheckedChange = viewModel::onShowConfidenceChange)
        }

        Divider(modifier = Modifier.padding(vertical = 24.dp))

        SectionHeader("About")
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = buildAnnotatedString {
                    append("This application is a fan-made work and it is ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("NOT") }
                    append(" associated with Nintendo.")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showSensitivityHelp) {
        SensitivityHelpDialog(onDismiss = { showSensitivityHelp = false })
    }
}

private fun sensitivityLabel(sensitivity: Float): String = when {
    sensitivity < 0.34f -> "Strict"
    sensitivity < 0.67f -> "Balanced"
    else -> "Lenient"
}

@Composable
private fun SensitivityHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calibrating sensitivity") },
        text = {
            Column {
                Text(
                    "Sensitivity controls how strict cry matching is — it directly changes how " +
                        "many votes a candidate needs to count as a match.",
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("• Lower (Strict): needs a cleaner, closer match. Fewer false positives, " +
                    "but may miss quieter or noisier cries.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Higher (Lenient): matches more readily. Catches faint or noisy cries, " +
                    "but may occasionally misidentify or show more \"possible outcomes.\"")
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "If you're getting frequent \"No cry recognized\" results in a noisy arcade, " +
                        "try raising sensitivity. If you're getting wrong matches, try lowering it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Got it") }
        },
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
    )
}
