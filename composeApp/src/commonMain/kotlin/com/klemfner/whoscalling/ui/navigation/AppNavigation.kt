package com.klemfner.whoscalling.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.klemfner.whoscalling.ui.calllogs.CallLogsScreen
import com.klemfner.whoscalling.ui.calllogs.CallLogsViewModel
import com.klemfner.whoscalling.ui.common.utils.LocalIsExpanded
import com.klemfner.whoscalling.ui.contacts.ContactsScreen
import com.klemfner.whoscalling.ui.contacts.ContactsViewModel
import com.klemfner.whoscalling.ui.settings.SettingsScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.call_logs
import whoscalling.composeapp.generated.resources.contacts
import whoscalling.composeapp.generated.resources.settings

@Composable
fun AppNavigation() {
    val contactsViewModel: ContactsViewModel = koinViewModel()
    val callLogsViewModel: CallLogsViewModel = koinViewModel()
    val isExpanded = LocalIsExpanded.current

    var selectedTab by remember { mutableStateOf(NavigationTab.CONTACTS) }

    val onAddContact: (String) -> Unit = { phoneNumber ->
        selectedTab = NavigationTab.CONTACTS
        contactsViewModel.openAddContact(phoneNumber)
    }

    if (isExpanded) {
        ExpandedLayout(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            contactsViewModel = contactsViewModel,
            callLogsViewModel = callLogsViewModel,
            onAddContact = onAddContact,
        )
    } else {
        CompactLayout(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            contactsViewModel = contactsViewModel,
            callLogsViewModel = callLogsViewModel,
            onAddContact = onAddContact,
        )
    }
}

@Composable
private fun CompactLayout(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    contactsViewModel: ContactsViewModel,
    callLogsViewModel: CallLogsViewModel,
    onAddContact: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        NavigationContent(
            selectedTab = selectedTab,
            contactsViewModel = contactsViewModel,
            callLogsViewModel = callLogsViewModel,
            onAddContact = onAddContact,
            modifier = Modifier.weight(1f),
        )
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
    }
}

@Composable
private fun ExpandedLayout(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    contactsViewModel: ContactsViewModel,
    callLogsViewModel: CallLogsViewModel,
    onAddContact: (String) -> Unit,
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
            contactsViewModel = contactsViewModel,
            callLogsViewModel = callLogsViewModel,
            onAddContact = onAddContact,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun NavigationContent(
    selectedTab: NavigationTab,
    contactsViewModel: ContactsViewModel,
    callLogsViewModel: CallLogsViewModel,
    onAddContact: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (selectedTab) {
        NavigationTab.CALL_LOGS -> CallLogsScreen(
            viewModel = callLogsViewModel,
            onAddContact = onAddContact,
            modifier = modifier,
        )
        NavigationTab.CONTACTS -> ContactsScreen(
            viewModel = contactsViewModel,
            modifier = modifier,
        )
        NavigationTab.SETTINGS -> SettingsScreen(modifier)
    }
}
