package com.klemfner.whoscalling.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.klemfner.whoscalling.domain.repository.AuthRepository
import com.klemfner.whoscalling.ui.calllogs.CallLogsScreen
import com.klemfner.whoscalling.ui.common.utils.LocalIsExpanded
import com.klemfner.whoscalling.ui.contacts.ContactsScreen
import com.klemfner.whoscalling.ui.ringing_banner.RingingCallBanner
import com.klemfner.whoscalling.ui.settings.SettingsScreen
import com.klemfner.whoscalling.ui.user.UserScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.call_logs
import whoscalling.composeapp.generated.resources.contacts
import whoscalling.composeapp.generated.resources.settings
import whoscalling.composeapp.generated.resources.user

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val isExpanded = LocalIsExpanded.current
    val authRepository: AuthRepository = koinInject()
    val initialTab = remember { if (authRepository.loggedInUser.value != null) NavigationTab.CALL_LOGS else NavigationTab.USER }
    val navigator = rememberNavigator(initialTab)

    CompositionLocalProvider(LocalNavigator provides navigator) {
        Scaffold(
            bottomBar = {
                if (!isExpanded) {
                    AppNavigationBar(
                        selectedTab = navigator.navState.tab,
                        onTabSelected = { navigator.navigateTo(it) },
                    )
                }
            },
        ) { paddingValues ->
            if (isExpanded) {
                Row(Modifier.fillMaxSize().padding(paddingValues)) {
                    AppNavigationRail(
                        selectedTab = navigator.navState.tab,
                        onTabSelected = { navigator.navigateTo(it) },
                    )
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        NavigationContent(modifier = Modifier.fillMaxSize())
                        RingingCallBanner(modifier = Modifier.align(Alignment.BottomCenter))
                    }
                }
            } else {
                Box(Modifier.fillMaxSize().padding(paddingValues)) {
                    NavigationContent(modifier = Modifier.fillMaxSize())
                    RingingCallBanner(modifier = Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    }
}

@Composable
private fun AppNavigationBar(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == NavigationTab.USER,
            onClick = { onTabSelected(NavigationTab.USER) },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            label = { Text(stringResource(Res.string.user)) },
        )
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

@Composable
private fun AppNavigationRail(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
) {
    NavigationRail(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        NavigationRailItem(
            selected = selectedTab == NavigationTab.USER,
            onClick = { onTabSelected(NavigationTab.USER) },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            label = { Text(stringResource(Res.string.user)) },
        )
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
}

@Composable
private fun NavigationContent(modifier: Modifier = Modifier) {
    val navigator = LocalNavigator.current
    when (navigator.navState.tab) {
        NavigationTab.USER -> UserScreen(modifier = modifier)
        NavigationTab.CALL_LOGS -> CallLogsScreen(modifier = modifier)
        NavigationTab.CONTACTS -> ContactsScreen(modifier = modifier)
        NavigationTab.SETTINGS -> SettingsScreen(modifier)
    }
}
