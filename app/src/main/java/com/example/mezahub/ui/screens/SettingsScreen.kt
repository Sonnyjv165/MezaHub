package com.example.mezahub.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mezahub.BuildConfig
import com.example.mezahub.R
import com.example.mezahub.data.AppLanguage
import com.example.mezahub.data.MezastarVersion
import com.example.mezahub.ui.components.PokedexHeader
import com.example.mezahub.ui.components.Pokeball
import com.example.mezahub.ui.components.quantityString
import com.example.mezahub.ui.theme.BallTheme
import com.example.mezahub.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

/** Settings screen. Every value here reflects real, persisted state. */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = viewModel()) {
    val sensitivity by viewModel.sensitivity.collectAsState()
    val showConfidence by viewModel.showConfidence.collectAsState()
    val loadedCount by viewModel.loadedCryCount.collectAsState()
    val isUpdating by viewModel.isUpdatingDatabase.collectAsState()
    val ballTheme by viewModel.ballTheme.collectAsState()
    val activeVersion by viewModel.activeVersion.collectAsState()
    var showSensitivityHelp by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    // Changing language recreates the activity, so reading it once per composition is enough.
    val currentLanguage = remember { viewModel.currentLanguage() }

    Column(modifier = modifier.fillMaxSize()) {
        PokedexHeader(title = stringResource(R.string.settings_title))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, bottom = 24.dp),
        ) {
            SectionHeader(stringResource(R.string.settings_mezastar_version))
            Text(
                text = stringResource(R.string.settings_mezastar_version_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            MezastarVersion.entries.forEach { version ->
                VersionOption(
                    version = version,
                    cardCount = viewModel.cardCount(version),
                    isSelected = version == activeVersion,
                    enabled = !isUpdating,
                    onClick = { viewModel.onActiveVersionChange(version) },
                )
            }

            SectionDivider()

            SectionHeader(stringResource(R.string.settings_cry_database))
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = quantityString(R.plurals.settings_cries_loaded, loadedCount, loadedCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = viewModel::updateDatabase, enabled = !isUpdating) {
                        Text(stringResource(R.string.settings_update_database))
                    }
                    if (isUpdating) {
                        Spacer(modifier = Modifier.width(12.dp))
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }
                }
            }

            SectionDivider()

            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.settings_sensitivity),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                IconButton(onClick = { showSensitivityHelp = true }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Filled.HelpOutline,
                        contentDescription = stringResource(R.string.cd_sensitivity_help),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = stringResource(R.string.settings_sensitivity_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Slider(value = sensitivity, onValueChange = viewModel::onSensitivityChange)
                Text(
                    text = stringResource(
                        R.string.settings_sensitivity_value,
                        (sensitivity * 100).roundToInt(),
                        stringResource(sensitivityLabelRes(sensitivity)),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionDivider()

            SectionHeader(stringResource(R.string.settings_theme))
            Text(
                text = stringResource(R.string.settings_theme_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showThemePicker = true }
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Pokeball(size = 36.dp, style = ballTheme.ball)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(ballTheme.nameRes), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = stringResource(R.string.settings_theme_generation, romanNumeral(ballTheme.generation)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            SectionDivider()

            SectionHeader(stringResource(R.string.settings_display))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.settings_show_confidence), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = stringResource(R.string.settings_show_confidence_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = showConfidence, onCheckedChange = viewModel::onShowConfidenceChange)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLanguagePicker = true }
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.settings_language), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = languageName(currentLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            SectionDivider()

            SectionHeader(stringResource(R.string.settings_about))
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                val template = stringResource(R.string.about_disclaimer)
                val emphasized = stringResource(R.string.about_disclaimer_not)
                Text(
                    // The emphasized word is a %1$s placeholder so each language can place it naturally.
                    text = buildAnnotatedString {
                        val parts = template.split("%1\$s", limit = 2)
                        append(parts[0])
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(emphasized) }
                        if (parts.size > 1) append(parts[1])
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (showSensitivityHelp) {
        SensitivityHelpDialog(onDismiss = { showSensitivityHelp = false })
    }
    if (showThemePicker) {
        BallThemeDialog(
            selected = ballTheme,
            onSelect = viewModel::onBallThemeChange,
            onDismiss = { showThemePicker = false },
        )
    }
    if (showLanguagePicker) {
        LanguageDialog(
            current = currentLanguage,
            onSelect = { language ->
                showLanguagePicker = false
                viewModel.onLanguageChange(language)
            },
            onDismiss = { showLanguagePicker = false },
        )
    }
}

/**
 * Every ball theme, grouped by the generation that introduced it. Picking one applies it right
 * away (the dialog recolors live), so the dialog stays open for comparing until Done.
 */
@Composable
private fun BallThemeDialog(selected: BallTheme, onSelect: (BallTheme) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_theme)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                BallTheme.entries.groupBy { it.generation }.toSortedMap().forEach { (generation, themes) ->
                    Text(
                        text = stringResource(R.string.settings_theme_generation, romanNumeral(generation)),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    )
                    BallThemeGrid(themes = themes, selected = selected, onSelect = onSelect)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.done)) } },
    )
}

@Composable
private fun BallThemeGrid(themes: List<BallTheme>, selected: BallTheme, onSelect: (BallTheme) -> Unit) {
    Column {
        themes.chunked(4).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { theme ->
                    BallThemeOption(
                        theme = theme,
                        isSelected = theme == selected,
                        onClick = { onSelect(theme) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Keep cells the same width on a partial last row.
                repeat(4 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BallThemeOption(theme: BallTheme, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .selectable(selected = isSelected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .border(
                    width = 3.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape,
                )
                .padding(5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Pokeball(size = 44.dp, style = theme.ball)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(theme.nameRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun VersionOption(
    version: MezastarVersion,
    cardCount: Int,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = isSelected, enabled = enabled, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 24.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = isSelected, onClick = null, enabled = enabled)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = stringResource(R.string.version_label, version.number), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (cardCount > 0) {
                    quantityString(R.plurals.version_cards, cardCount, cardCount)
                } else {
                    stringResource(R.string.version_not_ready)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LanguageDialog(current: AppLanguage, onSelect: (AppLanguage) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                AppLanguage.entries.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = language == current, onClick = { onSelect(language) }, role = Role.RadioButton)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = language == current, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = languageName(language), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun languageName(language: AppLanguage): String =
    language.nativeName ?: stringResource(R.string.language_system)

private fun sensitivityLabelRes(sensitivity: Float): Int = when {
    sensitivity < 0.34f -> R.string.sensitivity_strict
    sensitivity < 0.67f -> R.string.sensitivity_balanced
    else -> R.string.sensitivity_lenient
}

private fun romanNumeral(generation: Int): String =
    listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX").getOrElse(generation - 1) { generation.toString() }

@Composable
private fun SensitivityHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sensitivity_help_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.sensitivity_help_intro))
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.sensitivity_help_strict))
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.sensitivity_help_lenient))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.sensitivity_help_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.got_it)) } },
    )
}

@Composable
private fun SectionDivider() {
    Divider(modifier = Modifier.padding(vertical = 24.dp))
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
