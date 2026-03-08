package com.klemfner.whoscalling.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class NavigatorTest {

    @Test
    fun navigateToSettingsWithFocusRouterIpAction() {
        val navigator = Navigator(NavigationTab.USER)

        navigator.navigateTo(NavigationTab.SETTINGS, NavAction.FocusRouterIp)

        assertEquals(NavigationTab.SETTINGS, navigator.navState.tab)
        assertIs<NavAction.FocusRouterIp>(navigator.navState.action)
    }

    @Test
    fun consumeActionClearsFocusRouterIpAction() {
        val navigator = Navigator(NavigationTab.USER)
        navigator.navigateTo(NavigationTab.SETTINGS, NavAction.FocusRouterIp)

        navigator.consumeAction()

        assertEquals(NavigationTab.SETTINGS, navigator.navState.tab)
        assertNull(navigator.navState.action)
    }
}
