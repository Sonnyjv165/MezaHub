package com.example.mezahub.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mezahub.R
import com.example.mezahub.data.DetectionRecord
import com.example.mezahub.data.StarTier
import com.example.mezahub.model.CryOutcome
import com.example.mezahub.ui.components.PokedexHeader
import com.example.mezahub.ui.components.Pokeball
import com.example.mezahub.ui.components.PokemonIcon
import com.example.mezahub.ui.components.formatTimestamp
import com.example.mezahub.ui.components.quantityString
import com.example.mezahub.ui.components.RarityBackground
import com.example.mezahub.ui.components.StarRow
import com.example.mezahub.ui.components.rarityStyleFor
import com.example.mezahub.ui.theme.BallStyle
import com.example.mezahub.ui.theme.LocalBallTheme
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
            val headerTheme = LocalBallTheme.current
            PokedexHeader(
                title = stringResource(R.string.history_title),
                subtitle = quantityString(R.plurals.history_count, totalCount, totalCount),
                actions = {
                    IconButton(onClick = { filterDialogOpen = true }) {
                        // An active filter shows as an inverted pill, visible on any header color.
                        val active = tierFilter.isNotEmpty()
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(if (active) headerTheme.onHeader else Color.Transparent, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = stringResource(if (active) R.string.history_filter_active else R.string.history_filter),
                                tint = if (active) headerTheme.header else headerTheme.onHeader,
                            )
                        }
                    }
                    IconButton(onClick = viewModel::toggleSortDirection) {
                        Icon(
                            imageVector = if (sortAscending) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = stringResource(
                                if (sortAscending) R.string.history_sorted_oldest else R.string.history_sorted_newest,
                            ),
                            tint = headerTheme.onHeader,
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
                    placeholder = { Text(stringResource(R.string.history_search_hint)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.history_clear_search))
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
                            text = stringResource(
                                R.string.history_showing,
                                StarTier.entries.filter { it in tierFilter }.map { stringResource(it.labelRes) }.joinToString(),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = viewModel::clearTierFilter) { Text(stringResource(R.string.clear)) }
                    }
                }
            }

            when {
                totalCount == 0 -> EmptyHistory(stringResource(R.string.history_empty))
                entries.isEmpty() -> EmptyHistory(stringResource(R.string.history_no_results))
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
        // Resolved here: the snackbar is shown from a coroutine, outside composition.
        val deletedMessage = stringResource(R.string.history_deleted, speciesLabel)
        val undoLabel = stringResource(R.string.undo)
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.history_delete_title)) },
            text = { Text(stringResource(R.string.history_delete_body, speciesLabel)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeEntry(recordToDelete)
                        pendingDelete = null
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            val result = snackbarHostState.showSnackbar(
                                message = deletedMessage,
                                actionLabel = undoLabel,
                                duration = SnackbarDuration.Short,
                            )
                            if (result == SnackbarResult.ActionPerformed) viewModel.restoreEntry(recordToDelete)
                        }
                    },
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.cancel))
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
            Pokeball(size = 64.dp, style = BallStyle.GREYED, modifier = Modifier.graphicsLayer { alpha = 0.6f })
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
        title = { Text(stringResource(R.string.filter_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.filter_body),
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
                        Text(text = stringResource(tier.labelRes), style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        StarRow(count = tier.stars, tint = MaterialTheme.colorScheme.onSurfaceVariant, starSize = 12.dp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.done)) }
        },
        dismissButton = {
            TextButton(onClick = onClear) { Text(stringResource(R.string.filter_clear)) }
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
        stringResource(R.string.history_more, primary.speciesName, record.outcomes.size - 1)
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
                        version = primary.version,
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
                            text = stringResource(
                                R.string.meta_join,
                                formatTimestamp(record.timestampMillis),
                                stringResource(R.string.version_short, record.version),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = style?.textColor?.copy(alpha = 0.8f)
                                ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showConfidence) {
                        Text(
                            text = stringResource(R.string.confidence_percent, primary.confidencePercent),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = style?.textColor ?: MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (isExpandable) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = stringResource(R.string.cd_view_possible_cards),
                            tint = style?.textColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = stringResource(R.string.cd_delete_record),
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
        title = { Text(stringResource(R.string.history_detail_title, record.outcomes.first().speciesName)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = quantityString(
                        R.plurals.history_detail_body,
                        record.outcomes.size,
                        record.outcomes.size,
                        formatTimestamp(record.timestampMillis),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                record.outcomes.forEach { outcome -> OutcomeDetailRow(outcome, showConfidence) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
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
                    version = outcome.version,
                    speciesName = outcome.speciesName,
                    tier = outcome.tier,
                    size = 44.dp,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(outcome.tier.labelRes),
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
                            text = stringResource(R.string.confidence_line, outcome.confidencePercent),
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
