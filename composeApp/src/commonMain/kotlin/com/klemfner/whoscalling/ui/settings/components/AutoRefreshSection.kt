package com.klemfner.whoscalling.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import whoscalling.composeapp.generated.resources.auto_refresh_seconds_input
import whoscalling.composeapp.generated.resources.save

private data class RefreshOption(val label: String, val seconds: Long)

private const val CUSTOM_MARKER = -1L

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AutoRefreshSection(
    refreshRateSeconds: Long,
    onRefreshRateSave: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val presetOptions = listOf(
        RefreshOption(stringResource(Res.string.auto_refresh_never), 0),
        RefreshOption("5s", 5),
        RefreshOption("10s", 10),
        RefreshOption("30s", 30),
        RefreshOption("1m", 60),
        RefreshOption("5m", 300),
    )

    val isPreset = presetOptions.any { it.seconds == refreshRateSeconds }
    val initialSelection = if (isPreset) refreshRateSeconds else CUSTOM_MARKER
    val initialCustom = if (isPreset) "" else refreshRateSeconds.toString()

    var selectedOption by rememberSaveable(refreshRateSeconds) { mutableStateOf(initialSelection) }
    var customSeconds by rememberSaveable(refreshRateSeconds) { mutableStateOf(initialCustom) }

    val draftSeconds = if (selectedOption == CUSTOM_MARKER) {
        customSeconds.toLongOrNull() ?: 0L
    } else {
        selectedOption
    }

    val isCustomValid = selectedOption != CUSTOM_MARKER ||
            (customSeconds.toLongOrNull() != null && customSeconds.toLongOrNull()!! > 0)

    val hasChanges = draftSeconds != refreshRateSeconds
    val canSave = hasChanges && (selectedOption != CUSTOM_MARKER || isCustomValid)

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

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            presetOptions.forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedOption == option.seconds,
                        onClick = { selectedOption = option.seconds },
                    )
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedOption == CUSTOM_MARKER,
                    onClick = { selectedOption = CUSTOM_MARKER },
                )
                Text(
                    text = stringResource(Res.string.auto_refresh_custom),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (selectedOption == CUSTOM_MARKER) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = customSeconds,
                    onValueChange = { value ->
                        customSeconds = value.filter { it.isDigit() }
                    },
                    label = { Text(stringResource(Res.string.auto_refresh_seconds_input)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(120.dp),
                )
                val customValue = customSeconds.toLongOrNull()
                if (customValue != null && customValue > 0) {
                    Text(
                        text = formatDuration(customValue),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
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
