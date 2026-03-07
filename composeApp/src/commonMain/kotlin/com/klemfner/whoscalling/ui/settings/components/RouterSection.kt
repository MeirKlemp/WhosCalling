package com.klemfner.whoscalling.ui.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.router_configuration
import whoscalling.composeapp.generated.resources.router_ip

@Composable
fun RouterSection(
    routerIp: String,
    onRouterIpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(Res.string.router_configuration),
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    HorizontalDivider()
    OutlinedTextField(
        value = routerIp,
        onValueChange = onRouterIpChange,
        label = { Text(stringResource(Res.string.router_ip)) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
