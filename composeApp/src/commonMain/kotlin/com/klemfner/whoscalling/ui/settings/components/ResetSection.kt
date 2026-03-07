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
import whoscalling.composeapp.generated.resources.general
import whoscalling.composeapp.generated.resources.reset_to_default

@Composable
fun ResetSection(
    onResetToDefault: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(Res.string.general),
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    HorizontalDivider()
    TextButton(
        onClick = onResetToDefault,
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Text(stringResource(Res.string.reset_to_default))
    }
}
