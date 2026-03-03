package com.klemfner.whoscalling

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klemfner.whoscalling.di.appModules
import com.klemfner.whoscalling.di.platformModule
import org.koin.core.module.Module
import com.klemfner.whoscalling.ui.NavigationTab
import com.klemfner.whoscalling.ui.calllogs.CallLogsScreen
import com.klemfner.whoscalling.ui.common.theme.AppTheme
import com.klemfner.whoscalling.ui.common.utils.isPointerInputMode
import com.klemfner.whoscalling.ui.contacts.ContactsScreen
import com.klemfner.whoscalling.ui.contacts.ContactsViewModel
import com.klemfner.whoscalling.ui.settings.SettingsScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.call_logs
import whoscalling.composeapp.generated.resources.contacts
import whoscalling.composeapp.generated.resources.settings

@Composable
fun App(additionalModules: List<Module> = emptyList()) {
    KoinApplication(application = {
        modules(additionalModules + appModules + platformModule)
    }) {
        AppTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                val contactsViewModel: ContactsViewModel = koinViewModel()

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isExpanded = maxWidth >= 600.dp
                    val isTouchMode = !isPointerInputMode()

                    var selectedTab by remember { mutableStateOf(NavigationTab.CONTACTS) }

                    if (isExpanded) {
                        ExpandedLayout(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            isTouchMode = isTouchMode,
                            contactsViewModel = contactsViewModel,
                        )
                    } else {
                        CompactLayout(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            isTouchMode = isTouchMode,
                            contactsViewModel = contactsViewModel,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactLayout(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    isTouchMode: Boolean,
    contactsViewModel: ContactsViewModel,
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.CALL_LOGS,
                    onClick = { onTabSelected(NavigationTab.CALL_LOGS) },
                    icon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    label = { Text(stringResource(Res.string.call_logs)) },
                )
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.CONTACTS,
                    onClick = { onTabSelected(NavigationTab.CONTACTS) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text(stringResource(Res.string.contacts)) },
                )
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.SETTINGS,
                    onClick = { onTabSelected(NavigationTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(Res.string.settings)) },
                )
            }
        },
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues).fillMaxSize()) {
            NavigationContent(
                selectedTab = selectedTab,
                isExpanded = false,
                isTouchMode = isTouchMode,
                contactsViewModel = contactsViewModel,
            )
        }
    }
}

@Composable
private fun ExpandedLayout(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    isTouchMode: Boolean,
    contactsViewModel: ContactsViewModel,
) {
    Row(Modifier.fillMaxSize().safeContentPadding()) {
        NavigationRail {
            NavigationRailItem(
                selected = selectedTab == NavigationTab.CALL_LOGS,
                onClick = { onTabSelected(NavigationTab.CALL_LOGS) },
                icon = { Icon(Icons.Default.Phone, contentDescription = null) },
                label = { Text(stringResource(Res.string.call_logs)) },
            )
            NavigationRailItem(
                selected = selectedTab == NavigationTab.CONTACTS,
                onClick = { onTabSelected(NavigationTab.CONTACTS) },
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                label = { Text(stringResource(Res.string.contacts)) },
            )
            Spacer(Modifier.weight(1f))
            NavigationRailItem(
                selected = selectedTab == NavigationTab.SETTINGS,
                onClick = { onTabSelected(NavigationTab.SETTINGS) },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text(stringResource(Res.string.settings)) },
            )
        }

        NavigationContent(
            selectedTab = selectedTab,
            isExpanded = true,
            isTouchMode = isTouchMode,
            contactsViewModel = contactsViewModel,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun NavigationContent(
    selectedTab: NavigationTab,
    isExpanded: Boolean,
    isTouchMode: Boolean,
    contactsViewModel: ContactsViewModel,
    modifier: Modifier = Modifier,
) {
    when (selectedTab) {
        NavigationTab.CALL_LOGS -> CallLogsScreen(modifier)
        NavigationTab.CONTACTS -> ContactsScreen(
            viewModel = contactsViewModel,
            isExpanded = isExpanded,
            isTouchMode = isTouchMode,
            modifier = modifier,
        )
        NavigationTab.SETTINGS -> SettingsScreen(modifier)
    }
}