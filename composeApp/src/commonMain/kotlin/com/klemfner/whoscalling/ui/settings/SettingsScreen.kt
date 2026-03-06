package com.klemfner.whoscalling.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.klemfner.whoscalling.ui.common.utils.LocalTouchModeState
import com.klemfner.whoscalling.util.rememberFileLoader
import com.klemfner.whoscalling.util.rememberFileSaver
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.contacts
import whoscalling.composeapp.generated.resources.contacts_exported
import whoscalling.composeapp.generated.resources.contacts_imported
import whoscalling.composeapp.generated.resources.debug
import whoscalling.composeapp.generated.resources.debug_logs
import whoscalling.composeapp.generated.resources.export_contacts
import whoscalling.composeapp.generated.resources.import_contacts
import whoscalling.composeapp.generated.resources.import_error
import whoscalling.composeapp.generated.resources.settings
import whoscalling.composeapp.generated.resources.touch_mode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    var showDebugLogs by rememberSaveable { mutableStateOf(false) }

    if (showDebugLogs) {
        DebugLogsScreen(
            onBack = { showDebugLogs = false },
            modifier = modifier,
        )
        return
    }

    val viewModel: SettingsViewModel = koinViewModel()
    val importResult by viewModel.importResult.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val fileSaver = rememberFileSaver()
    val fileLoader = rememberFileLoader()

    val contactsExportedMessage = stringResource(Res.string.contacts_exported)
    val contactsImportedMessage = stringResource(Res.string.contacts_imported)
    val importErrorMessage = stringResource(Res.string.import_error)

    LaunchedEffect(importResult) {
        when (val result = importResult) {
            is ImportResult.Success -> {
                snackbarHostState.showSnackbar(
                    "$contactsImportedMessage: ${result.count}",
                )
                viewModel.clearImportResult()
            }
            is ImportResult.Error -> {
                snackbarHostState.showSnackbar(importErrorMessage)
                viewModel.clearImportResult()
            }
            null -> {}
        }
    }

    val touchModeState = LocalTouchModeState.current
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(Res.string.settings)) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            Text(
                text = stringResource(Res.string.contacts),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            HorizontalDivider()
            TextButton(
                onClick = {
                    scope.launch {
                        val json = viewModel.exportContacts()
                        fileSaver("contacts.json", json)
                        snackbarHostState.showSnackbar(contactsExportedMessage)
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Text(stringResource(Res.string.export_contacts))
            }
            TextButton(
                onClick = {
                    scope.launch {
                        val json = fileLoader() ?: return@launch
                        viewModel.importContacts(json)
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Text(stringResource(Res.string.import_contacts))
            }

            Text(
                text = stringResource(Res.string.debug),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { touchModeState.setTouchMode(!touchModeState.isTouchMode) }
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = touchModeState.isTouchMode,
                    onCheckedChange = touchModeState.setTouchMode,
                )
                Text(
                    text = stringResource(Res.string.touch_mode),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            TextButton(
                onClick = { showDebugLogs = true },
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Text(stringResource(Res.string.debug_logs))
            }
        }
    }
}
