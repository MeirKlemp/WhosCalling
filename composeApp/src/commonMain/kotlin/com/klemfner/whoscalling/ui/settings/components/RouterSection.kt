package com.klemfner.whoscalling.ui.settings.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.router_configuration
import whoscalling.composeapp.generated.resources.router_ip
import whoscalling.composeapp.generated.resources.save

@Composable
fun RouterSection(
    routerIp: String,
    onRouterIpSave: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by rememberSaveable(routerIp) { mutableStateOf(routerIp) }

    Text(
        text = stringResource(Res.string.router_configuration),
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    HorizontalDivider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            label = { Text(stringResource(Res.string.router_ip)) },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        Button(
            onClick = { onRouterIpSave(draft) },
            enabled = draft != routerIp,
            modifier = Modifier.padding(start = 8.dp),
        ) {
            Text(stringResource(Res.string.save))
        }
    }
}
