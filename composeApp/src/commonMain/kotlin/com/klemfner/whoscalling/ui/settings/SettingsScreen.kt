package com.klemfner.whoscalling.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.klemfner.whoscalling.ui.common.utils.LocalTouchModeState
import com.klemfner.whoscalling.ui.settings.components.ContactsSection
import com.klemfner.whoscalling.ui.settings.components.DebugSection
import com.klemfner.whoscalling.util.rememberFileLoader
import com.klemfner.whoscalling.util.rememberFileSaver
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.contacts_exported
import whoscalling.composeapp.generated.resources.contacts_imported
import whoscalling.composeapp.generated.resources.import_error
import whoscalling.composeapp.generated.resources.settings

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
    val contactCount by viewModel.contactCount.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val fileSaver = rememberFileSaver()
    val fileLoader = rememberFileLoader()

    val importErrorMessage = stringResource(Res.string.import_error)

    LaunchedEffect(importResult) {
        when (val result = importResult) {
            is ImportResult.Success -> {
                val message = getString(Res.string.contacts_imported, result.count)
                snackbarHostState.showSnackbar(message)
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
            ContactsSection(
                contactCount = contactCount,
                onExport = {
                    scope.launch {
                        val exportData = viewModel.exportContacts()
                        val saved = fileSaver("contacts.json", exportData.json)
                        if (saved) {
                            val message = getString(Res.string.contacts_exported, exportData.count)
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                },
                onImport = {
                    scope.launch {
                        val json = fileLoader() ?: return@launch
                        viewModel.importContacts(json)
                    }
                },
            )

            DebugSection(
                isTouchMode = touchModeState.isTouchMode,
                onTouchModeChange = touchModeState.setTouchMode,
                onShowDebugLogs = { showDebugLogs = true },
            )
        }
    }
}
