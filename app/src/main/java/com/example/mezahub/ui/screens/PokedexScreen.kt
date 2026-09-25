package com.example.mezahub.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mezahub.R
import com.example.mezahub.data.CardProgress
import com.example.mezahub.data.PokedexStats
import com.example.mezahub.data.SpeciesCount
import com.example.mezahub.data.TierProgress
import com.example.mezahub.ui.components.PokedexHeader
import com.example.mezahub.ui.components.PokemonIcon
import com.example.mezahub.ui.components.formatTimestamp
import com.example.mezahub.ui.components.quantityString
import com.example.mezahub.ui.components.StarRow
import com.example.mezahub.ui.components.tierAccentColor
import com.example.mezahub.viewmodel.PokedexViewModel

/** Pokédex: which of the catalog's cards you've heard at the cabinet, and how often. */
@Composable
fun PokedexScreen(modifier: Modifier = Modifier, viewModel: PokedexViewModel = viewModel()) {
    val stats by viewModel.stats.collectAsState()
    var selected by remember { mutableStateOf<CardProgress?>(null) }
    val fullWidth: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

    Column(modifier = modifier.fillMaxSize()) {
        PokedexHeader(
            title = stringResource(R.string.pokedex_title),
            subtitle = stringResource(
                R.string.meta_join,
                stringResource(R.string.version_label, stats.version),
                stringResource(R.string.pokedex_subtitle, stats.heardCount, stats.totalCards),
            ),
        )
        if (stats.totalCards == 0) {
            VersionNotReady(version = stats.version, modifier = Modifier.weight(1f))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item(span = fullWidth) { CompletionCard(stats) }
                if (stats.mostHeard.isNotEmpty()) {
                    item(span = fullWidth) { MostHeardCard(stats.mostHeard) }
                }
                item(span = fullWidth) {
                    Text(
                        text = stringResource(R.string.pokedex_all_cards),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                items(stats.cards, key = { it.entry.tagId }) { card ->
                    CardCell(card = card, onClick = { selected = card })
                }
            }
        }
    }

    selected?.let { CardDetailDialog(card = it, onDismiss = { selected = null }) }
}

/** Shown for a Mezastar version whose card list hasn't been filled in yet. */
@Composable
private fun VersionNotReady(version: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.version_empty, version),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CompletionCard(stats: PokedexStats) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${stats.heardCount}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = " " + stringResource(R.string.pokedex_of_total, stats.totalCards),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = if (stats.totalCards == 0) 0f else stats.heardCount.toFloat() / stats.totalCards,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = quantityString(R.plurals.pokedex_detections, stats.totalDetections, stats.totalDetections),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(14.dp))
            stats.tiers.forEach { TierProgressRow(it) }
        }
    }
}

@Composable
private fun TierProgressRow(progress: TierProgress) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(progress.tier.labelRes),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(84.dp),
        )
        LinearProgressIndicator(
            progress = progress.heard.toFloat() / progress.total,
            color = tierAccentColor(progress.tier),
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        )
        Text(
            text = stringResource(R.string.tier_progress, progress.heard, progress.total),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End,
            modifier = Modifier.width(44.dp),
        )
    }
}

@Composable
private fun MostHeardCard(species: List<SpeciesCount>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.pokedex_most_heard), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                species.forEach { s ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        PokemonIcon(tagId = s.iconTagId, version = s.version, speciesName = s.speciesName, tier = s.tier, size = 60.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = s.speciesName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = stringResource(R.string.times_count, s.count),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardCell(card: CardProgress, onClick: () -> Unit) {
    val entry = card.entry
    val tierLabel = stringResource(entry.tier.labelRes)
    val description = if (card.heard) {
        quantityString(R.plurals.cd_card_heard, card.timesHeard, card.timesHeard, entry.speciesName, tierLabel)
    } else {
        stringResource(R.string.cd_card_not_heard, entry.speciesName, tierLabel)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (card.heard) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PokemonIcon(
                tagId = entry.tagId,
                version = entry.version,
                speciesName = entry.speciesName,
                tier = entry.tier,
                size = 60.dp,
                desaturated = !card.heard,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.tagId,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = entry.speciesName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            StarRow(count = entry.tier.stars, tint = tierAccentColor(entry.tier), starSize = 9.dp)
            Text(
                text = if (card.heard) stringResource(R.string.times_count, card.timesHeard) else "—",
                style = MaterialTheme.typography.labelSmall,
                color = if (card.heard) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CardDetailDialog(card: CardProgress, onDismiss: () -> Unit) {
    val entry = card.entry
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(entry.speciesName) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                PokemonIcon(
                    tagId = entry.tagId,
                    version = entry.version,
                    speciesName = entry.speciesName,
                    tier = entry.tier,
                    size = 110.dp,
                    desaturated = !card.heard,
                )
                Spacer(modifier = Modifier.height(10.dp))
                StarRow(count = entry.tier.stars, tint = tierAccentColor(entry.tier), starSize = 16.dp)
                Text(
                    text = stringResource(R.string.pokedex_tier_tag, stringResource(entry.tier.labelRes), entry.tagId),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (card.heard) {
                        val heard = quantityString(R.plurals.pokedex_heard_times, card.timesHeard, card.timesHeard)
                        card.lastHeardMillis
                            ?.let { stringResource(R.string.pokedex_last_heard, heard, formatTimestamp(it)) }
                            ?: heard
                    } else {
                        stringResource(R.string.pokedex_not_heard)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
    )
}
