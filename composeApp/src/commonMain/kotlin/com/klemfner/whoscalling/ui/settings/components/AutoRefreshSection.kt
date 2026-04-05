package com.klemfner.whoscalling.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.auto_refresh
import whoscalling.composeapp.generated.resources.auto_refresh_custom
import whoscalling.composeapp.generated.resources.auto_refresh_label
import whoscalling.composeapp.generated.resources.auto_refresh_never
import whoscalling.composeapp.generated.resources.auto_refresh_on_startup
import whoscalling.composeapp.generated.resources.auto_refresh_seconds_input
import whoscalling.composeapp.generated.resources.save

private data class RefreshOption(val label: String, val seconds: Long)

private const val CUSTOM_MARKER = -1L

@Composable
fun AutoRefreshSection(
    refreshRateSeconds: Long,
    refreshOnStartup: Boolean,
    onRefreshRateSave: (Long) -> Unit,
    onRefreshOnStartupChange: (Boolean) -> Unit,
    isExpanded: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val compactOptions = listOf(
        RefreshOption(stringResource(Res.string.auto_refresh_never), 0),
        RefreshOption("5s", 5),
        RefreshOption("30s", 30),
        RefreshOption(stringResource(Res.string.auto_refresh_custom), CUSTOM_MARKER),
    )

    val expandedOptions = listOf(
        RefreshOption(stringResource(Res.string.auto_refresh_never), 0),
        RefreshOption("5s", 5),
        RefreshOption("30s", 30),
        RefreshOption("1m", 60),
        RefreshOption("5m", 300),
        RefreshOption(stringResource(Res.string.auto_refresh_custom), CUSTOM_MARKER),
    )

    val presetOptions = if (isExpanded) expandedOptions else compactOptions

    val isKnownPreset = presetOptions.any { it.seconds == refreshRateSeconds }
    val initialSelection = if (isKnownPreset) refreshRateSeconds else CUSTOM_MARKER
    val initialCustom = if (isKnownPreset) "" else refreshRateSeconds.toString()

    var selectedOption by remember(refreshRateSeconds) { mutableStateOf(initialSelection) }
    var customSeconds by remember(refreshRateSeconds) { mutableStateOf(initialCustom) }

    // Whether the selected option is a visible preset button in the current layout.
    val isVisiblePreset = selectedOption != CUSTOM_MARKER && presetOptions.any { it.seconds == selectedOption }
    // Which button to highlight — falls back to Custom for expanded-only presets in compact view.
    val activeButtonValue = if (isVisiblePreset) selectedOption else CUSTOM_MARKER
    // Custom text: use the expanded-only preset value when it's not a visible button.
    val effectiveCustomText = if (!isVisiblePreset && selectedOption != CUSTOM_MARKER) {
        selectedOption.toString()
    } else {
        customSeconds
    }

    val draftSeconds = if (selectedOption == CUSTOM_MARKER) {
        customSeconds.toLongOrNull() ?: 0L
    } else {
        selectedOption
    }

    val isCustomValid = activeButtonValue != CUSTOM_MARKER || run {
        val value = effectiveCustomText.toLongOrNull()
        value != null && value > 0
    }

    val hasChanges = draftSeconds != refreshRateSeconds
    val canSave = hasChanges && (activeButtonValue != CUSTOM_MARKER || isCustomValid)

    Text(
        text = stringResource(Res.string.auto_refresh),
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    HorizontalDivider()

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(Res.string.auto_refresh_label),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            presetOptions.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = activeButtonValue == option.seconds,
                    onClick = {
                        if (option.seconds == CUSTOM_MARKER && !isVisiblePreset && selectedOption != CUSTOM_MARKER) {
                            customSeconds = selectedOption.toString()
                        }
                        selectedOption = option.seconds
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = presetOptions.size,
                    ),
                ) {
                    Text(option.label)
                }
            }
        }

        if (activeButtonValue == CUSTOM_MARKER) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = effectiveCustomText,
                    onValueChange = { value ->
                        customSeconds = value.filter { it.isDigit() }
                        if (selectedOption != CUSTOM_MARKER) {
                            selectedOption = CUSTOM_MARKER
                        }
                    },
                    label = { Text(stringResource(Res.string.auto_refresh_seconds_input)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(120.dp),
                )
                val customValue = effectiveCustomText.toLongOrNull()
                if (customValue != null && customValue > 0) {
                    Text(
                        text = formatDuration(customValue),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        if (selectedOption == 0L) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = refreshOnStartup,
                    onCheckedChange = onRefreshOnStartupChange,
                )
                Text(
                    text = stringResource(Res.string.auto_refresh_on_startup),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Button(
            onClick = { onRefreshRateSave(draftSeconds) },
            enabled = canSave,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(stringResource(Res.string.save))
        }
    }
}

internal fun formatDuration(totalSeconds: Long): String {
    if (totalSeconds <= 0) return "0s"
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return buildString {
        if (minutes > 0) append("${minutes}m")
        if (seconds > 0) {
            if (minutes > 0) append(" ")
            append("${seconds}s")
        }
    }
}
