package com.klemfner.whoscalling.ui.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.debug
import whoscalling.composeapp.generated.resources.debug_logs

@Composable
fun DebugSection(
    onShowDebugLogs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(Res.string.debug),
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    HorizontalDivider()
    TextButton(
        onClick = onShowDebugLogs,
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Text(stringResource(Res.string.debug_logs))
    }
}
