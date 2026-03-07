package com.klemfner.whoscalling.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.debug
import whoscalling.composeapp.generated.resources.debug_logs
import whoscalling.composeapp.generated.resources.touch_mode

@Composable
fun DebugSection(
    isTouchMode: Boolean,
    onTouchModeChange: (Boolean) -> Unit,
    onShowDebugLogs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(Res.string.debug),
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    HorizontalDivider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTouchModeChange(!isTouchMode) }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isTouchMode,
            onCheckedChange = onTouchModeChange,
        )
        Text(
            text = stringResource(Res.string.touch_mode),
            modifier = Modifier.padding(start = 8.dp),
        )
    }
    TextButton(
        onClick = onShowDebugLogs,
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Text(stringResource(Res.string.debug_logs))
    }
}
