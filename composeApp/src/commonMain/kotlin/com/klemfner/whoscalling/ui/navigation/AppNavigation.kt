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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.klemfner.whoscalling.ui.calllogs.CallLogsScreen
import com.klemfner.whoscalling.ui.common.utils.LocalIsExpanded
import com.klemfner.whoscalling.ui.contacts.ContactsScreen
import com.klemfner.whoscalling.ui.settings.SettingsScreen
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.call_logs
import whoscalling.composeapp.generated.resources.contacts
import whoscalling.composeapp.generated.resources.settings

@Composable
fun AppNavigation() {
    val isExpanded = LocalIsExpanded.current
    val navigator = rememberNavigator()

    CompositionLocalProvider(LocalNavigator provides navigator) {
        AppLayout(
            isExpanded = isExpanded,
            selectedTab = navigator.navState.tab,
            onTabSelected = { navigator.navigateTo(it) },
        ) { modifier ->
            NavigationContent(modifier = modifier)
        }
    }
}

@Composable
private fun AppLayout(
    isExpanded: Boolean,
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    if (isExpanded) {
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

            content(Modifier.fillMaxSize())
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            content(Modifier.weight(1f))

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
}

@Composable
private fun NavigationContent(modifier: Modifier = Modifier) {
    val navigator = LocalNavigator.current
    when (navigator.navState.tab) {
        NavigationTab.CALL_LOGS -> CallLogsScreen(modifier = modifier)
        NavigationTab.CONTACTS -> ContactsScreen(modifier = modifier)
        NavigationTab.SETTINGS -> SettingsScreen(modifier)
    }
}
