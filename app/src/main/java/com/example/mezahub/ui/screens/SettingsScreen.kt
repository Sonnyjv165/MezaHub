package com.example.mezahub.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

/** Settings screen. Cry Database reflects the real fingerprint repository; sensitivity is UI-only. */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = viewModel()) {
    val sensitivity by viewModel.sensitivity.collectAsState()
    val loadedCount by viewModel.loadedCryCount.collectAsState()
    val isUpdating by viewModel.isUpdatingDatabase.collectAsState()

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

        SectionHeader("Sensitivity")
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "How aggressively MezaHub tries to match a cry",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Slider(value = sensitivity, onValueChange = viewModel::onSensitivityChange)
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
