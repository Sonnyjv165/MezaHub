package com.example.mezahub.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mezahub.data.DetectionRecord
import com.example.mezahub.data.StarTier
import com.example.mezahub.model.CryOutcome
import com.example.mezahub.ui.components.PokedexHeader
import com.example.mezahub.ui.components.Pokeball
import com.example.mezahub.ui.components.PokemonIcon
import com.example.mezahub.ui.components.formatTimestamp
import com.example.mezahub.ui.components.RarityBackground
import com.example.mezahub.ui.components.StarRow
import com.example.mezahub.ui.components.rarityStyleFor
import com.example.mezahub.ui.theme.PokemonYellow
import com.example.mezahub.viewmodel.HistoryViewModel
import kotlinx.coroutines.launch

/** History screen. Backed by real detections, persisted across restarts (see ListenViewModel). */
@Composable
fun HistoryScreen(modifier: Modifier = Modifier, viewModel: HistoryViewModel = viewModel()) {
    val entries by viewModel.entries.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val showConfidence by viewModel.showConfidence.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()
    val tierFilter by viewModel.tierFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var pendingDelete by remember { mutableStateOf<DetectionRecord?>(null) }
    var detailRecord by remember { mutableStateOf<DetectionRecord?>(null) }
    var filterDialogOpen by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PokedexHeader(
                title = "History",
                subtitle = "$totalCount ${if (totalCount == 1) "cry" else "cries"} logged",
                actions = {
                    IconButton(onClick = { filterDialogOpen = true }) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = if (tierFilter.isEmpty()) "Filter by star tier" else "Filter by star tier (active)",
                            tint = if (tierFilter.isEmpty()) Color.White else PokemonYellow,
                        )
                    }
                    IconButton(onClick = viewModel::toggleSortDirection) {
                        Icon(
                            imageVector = if (sortAscending) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = if (sortAscending) "Sorted oldest first" else "Sorted newest first",
                            tint = Color.White,
                        )
                    }
                },
            )

            if (totalCount > 0) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp),
                    placeholder = { Text("Search by Pokémon name") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    } else {
                        null
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                )
                if (tierFilter.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Showing: " + StarTier.entries.filter { it in tierFilter }.joinToString { it.label },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = viewModel::clearTierFilter) { Text("Clear") }
                    }
                }
            }

            when {
                totalCount == 0 -> EmptyHistory("No catches yet — hit Listen to identify your first cry!")
                entries.isEmpty() -> EmptyHistory("No catches match your search or filter.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
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
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    val recordToDelete = pendingDelete
    if (recordToDelete != null) {
        val speciesLabel = recordToDelete.outcomes.first().speciesName
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this record?") },
            text = { Text("This will remove the $speciesLabel entry from your history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeEntry(recordToDelete)
                        pendingDelete = null
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            val result = snackbarHostState.showSnackbar(
                                message = "Deleted $speciesLabel",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short,
                            )
                            if (result == SnackbarResult.ActionPerformed) viewModel.restoreEntry(recordToDelete)
                        }
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

    if (filterDialogOpen) {
        TierFilterDialog(
            selectedTiers = tierFilter,
            onToggleTier = viewModel::toggleTierFilter,
            onClear = viewModel::clearTierFilter,
            onDismiss = { filterDialogOpen = false },
        )
    }
}

@Composable
private fun EmptyHistory(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Pokeball(size = 64.dp, topColor = Color(0xFF9E9E9E), modifier = Modifier.graphicsLayer { alpha = 0.6f })
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TierFilterDialog(
    selectedTiers: Set<StarTier>,
    onToggleTier: (StarTier) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by star tier") },
        text = {
            Column {
                Text(
                    text = "Show only catches at these tiers. Leave all unchecked to show everything.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                StarTier.entries.forEach { tier ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleTier(tier) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = tier in selectedTiers, onCheckedChange = { onToggleTier(tier) })
                        Text(text = tier.label, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        StarRow(count = tier.stars, tint = MaterialTheme.colorScheme.onSurfaceVariant, starSize = 12.dp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onClear) { Text("Clear filter") }
        },
    )
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
                            text = formatTimestamp(record.timestampMillis),
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
                        "recorded ${formatTimestamp(record.timestampMillis)}.",
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
