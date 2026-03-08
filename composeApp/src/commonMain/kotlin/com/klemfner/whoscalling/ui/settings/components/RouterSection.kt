package com.klemfner.whoscalling.ui.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.router_configuration
import whoscalling.composeapp.generated.resources.router_ip
import whoscalling.composeapp.generated.resources.router_ip_warning
import whoscalling.composeapp.generated.resources.save

@Composable
fun RouterSection(
    routerIp: String,
    onRouterIpSave: (String) -> Unit,
    focusRequested: Boolean = false,
    onFocusConsumed: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var draft by rememberSaveable(routerIp, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(routerIp))
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusRequested) {
        if (focusRequested) {
            draft = draft.copy(selection = TextRange(0, draft.text.length))
            focusRequester.requestFocus()
            onFocusConsumed()
        }
    }

    Text(
        text = stringResource(Res.string.router_configuration),
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    HorizontalDivider()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text(stringResource(Res.string.router_ip)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onRouterIpSave(draft.text) }),
                modifier = Modifier.weight(1f).focusRequester(focusRequester),
            )
            Button(
                onClick = { onRouterIpSave(draft.text) },
                enabled = draft.text != routerIp,
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text(stringResource(Res.string.save))
            }
        }
        if (routerIp.isBlank()) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(Res.string.router_ip_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
