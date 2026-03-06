package com.klemfner.whoscalling.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

data class NavState(
    val tab: NavigationTab = NavigationTab.USER,
    val action: NavAction? = null,
)

sealed interface NavAction {
    data class AddContact(val phoneNumber: String) : NavAction
    data class ShowContact(val phoneNumber: String) : NavAction
    data class ShowCallLog(val callLogId: String) : NavAction
}

class Navigator(initialTab: NavigationTab) {
    var navState by mutableStateOf(NavState(tab = initialTab))
        private set

    fun navigateTo(tab: NavigationTab, action: NavAction? = null) {
        navState = NavState(tab = tab, action = action)
    }

    fun consumeAction() {
        navState = navState.copy(action = null)
    }
}

val LocalNavigator = compositionLocalOf<Navigator> {
    error("No Navigator provided")
}

@Composable
fun rememberNavigator(): Navigator {
    return rememberSaveable(saver = navigatorSaver()) {
        Navigator(NavigationTab.USER)
    }
}

private fun navigatorSaver() = Saver<Navigator, String>(
    save = { it.navState.tab.name },
    restore = { Navigator(NavigationTab.valueOf(it)) },
)
