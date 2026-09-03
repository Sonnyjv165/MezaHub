package com.example.mezahub.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mezahub.data.DetectionRecord
import com.example.mezahub.model.CryOutcome
import com.example.mezahub.ui.components.PokemonIcon
import com.example.mezahub.ui.components.RarityBackground
import com.example.mezahub.ui.components.StarRow
import com.example.mezahub.ui.components.rarityStyleFor
import com.example.mezahub.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val TIMESTAMP_FORMAT = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

/** History screen. Backed by real detections recorded during this session (see ListenViewModel). */
@Composable
fun HistoryScreen(modifier: Modifier = Modifier, viewModel: HistoryViewModel = viewModel()) {
    val entries by viewModel.entries.collectAsState()
    val showConfidence by viewModel.showConfidence.collectAsState()
    var pendingDelete by remember { mutableStateOf<DetectionRecord?>(null) }
    var detailRecord by remember { mutableStateOf<DetectionRecord?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Detection History",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(24.dp),
        )
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No catches yet — hit Listen to identify your first cry!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(entries, key = { it.id }) { record ->
                    HistoryRow(
                        record = record,
                        showConfidence = showConfidence,
                        onDeleteClick = { pendingDelete = record },
                        onRowClick = { if (record.outcomes.size > 1) detailRecord = record },
                    )
                }
            }
        }
    }

    val recordToDelete = pendingDelete
    if (recordToDelete != null) {
        val speciesLabel = recordToDelete.outcomes.first().speciesName
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this record?") },
            text = { Text("This will remove the $speciesLabel entry from your history. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeEntry(recordToDelete)
                        pendingDelete = null
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel")
                }
            },
        )
    }

    val recordForDetail = detailRecord
    if (recordForDetail != null) {
        DetailDialog(record = recordForDetail, showConfidence = showConfidence, onDismiss = { detailRecord = null })
    }
}

@Composable
private fun HistoryRow(
    record: DetectionRecord,
    showConfidence: Boolean,
    onDeleteClick: () -> Unit,
    onRowClick: () -> Unit,
) {
    val primary = record.outcomes.first()
    val isExpandable = record.outcomes.size > 1
    val title = if (isExpandable) {
        "${primary.speciesName} +${record.outcomes.size - 1} more"
    } else {
        primary.speciesName
    }
    val style = rarityStyleFor(record.outcomes)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (isExpandable) it.clickable(onClick = onRowClick) else it },
        shape = RoundedCornerShape(12.dp),
        colors = if (style != null) {
            CardDefaults.cardColors(containerColor = Color.Transparent)
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        },
        border = style?.let { BorderStroke(1.5.dp, it.borderColor) },
    ) {
        RarityBackground(style = style, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PokemonIcon(
                        tagId = primary.tagId,
                        speciesName = primary.speciesName,
                        tier = primary.tier,
                        size = 56.dp,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = style?.textColor ?: MaterialTheme.colorScheme.onSurface,
                        )
                        StarRow(
                            count = primary.tier.stars,
                            tint = style?.textColor ?: MaterialTheme.colorScheme.onSurface,
                            starSize = 13.dp,
                        )
                        Text(
                            text = TIMESTAMP_FORMAT.format(Date(record.timestampMillis)),
                            style = MaterialTheme.typography.bodySmall,
                            color = style?.textColor?.copy(alpha = 0.8f)
                                ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showConfidence) {
                        Text(
                            text = "${primary.confidencePercent}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = style?.textColor ?: MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (isExpandable) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "View all possible cards",
                            tint = style?.textColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Delete this record",
                            tint = style?.textColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailDialog(record: DetectionRecord, showConfidence: Boolean, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${record.outcomes.first().speciesName} — possible cards") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "This cry matched ${record.outcomes.size} cards with identical audio, " +
                        "recorded ${TIMESTAMP_FORMAT.format(Date(record.timestampMillis))}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                record.outcomes.forEach { outcome -> OutcomeDetailRow(outcome, showConfidence) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

@Composable
private fun OutcomeDetailRow(outcome: CryOutcome, showConfidence: Boolean) {
    val style = rarityStyleFor(listOf(outcome))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = if (style != null) {
            CardDefaults.cardColors(containerColor = Color.Transparent)
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        },
        border = style?.let { BorderStroke(1.dp, it.borderColor) },
    ) {
        RarityBackground(style = style, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PokemonIcon(
                    tagId = outcome.tagId,
                    speciesName = outcome.speciesName,
                    tier = outcome.tier,
                    size = 44.dp,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = outcome.tier.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = style?.textColor ?: MaterialTheme.colorScheme.onSurface,
                    )
                    StarRow(
                        count = outcome.tier.stars,
                        tint = style?.textColor ?: MaterialTheme.colorScheme.onSurface,
                        starSize = 12.dp,
                    )
                    if (showConfidence) {
                        Text(
                            text = "${outcome.confidencePercent}% confidence",
                            style = MaterialTheme.typography.bodySmall,
                            color = style?.textColor?.copy(alpha = 0.8f)
                                ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
