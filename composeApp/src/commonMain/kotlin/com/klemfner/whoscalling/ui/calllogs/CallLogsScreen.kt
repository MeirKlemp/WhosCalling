package com.klemfner.whoscalling.ui.calllogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.klemfner.whoscalling.ui.calllogs.components.CallLogDetails
import com.klemfner.whoscalling.ui.calllogs.components.CallLogsList
import com.klemfner.whoscalling.ui.common.utils.LocalIsExpanded
import com.klemfner.whoscalling.ui.common.utils.PlatformBackHandler
import com.klemfner.whoscalling.ui.navigation.LocalNavigator
import com.klemfner.whoscalling.ui.navigation.NavAction
import com.klemfner.whoscalling.ui.navigation.NavigationTab
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.failed_to_refresh
import whoscalling.composeapp.generated.resources.select_call_log

@Composable
fun CallLogsScreen(
    modifier: Modifier = Modifier,
    viewModel: CallLogsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isExpanded = LocalIsExpanded.current
    val navigator = LocalNavigator.current

    PlatformBackHandler(enabled = uiState.currentPane != CallLogsPane.LIST) {
        viewModel.goBack()
    }

    val onAddContact: (String) -> Unit = { phoneNumber ->
        navigator.navigateTo(NavigationTab.CONTACTS, NavAction.AddContact(phoneNumber))
    }

    val onShowContact: (String) -> Unit = { contactId ->
        navigator.navigateTo(NavigationTab.CONTACTS, NavAction.ShowContact(contactId))
    }

    val onLoginClick: () -> Unit = {
        navigator.navigateTo(NavigationTab.USER)
    }

    LaunchedEffect(navigator.navState.action) {
        val action = navigator.navState.action
        if (action is NavAction.ShowCallLog) {
            viewModel.selectCallLogById(action.callLogId)
            navigator.consumeAction()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val failedToRefreshMessage = stringResource(Res.string.failed_to_refresh)

    LaunchedEffect(uiState.refreshError) {
        if (uiState.refreshError) {
            snackbarHostState.showSnackbar(failedToRefreshMessage)
            viewModel.clearRefreshError()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusable()
            .onPreviewKeyEvent { event ->
                handleKeyEvent(event, viewModel)
            },
    ) {
        if (isExpanded) {
            ExpandedCallLogsLayout(uiState, viewModel, onAddContact, onShowContact, onLoginClick)
        } else {
            CompactCallLogsLayout(uiState, viewModel, onAddContact, onShowContact, onLoginClick)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private fun handleKeyEvent(
    event: KeyEvent,
    viewModel: CallLogsViewModel,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    return when {
        event.key == Key.Escape -> {
            viewModel.goBack()
            true
        }
        event.isCtrlPressed && event.key == Key.R -> {
            viewModel.refresh()
            true
        }
        event.key == Key.F5 -> {
            viewModel.refresh()
            true
        }
        else -> false
    }
}

@Composable
private fun CompactCallLogsLayout(
    uiState: CallLogsUiState,
    viewModel: CallLogsViewModel,
    onAddContact: (String) -> Unit,
    onShowContact: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    AnimatedContent(
        targetState = uiState.currentPane,
        transitionSpec = {
            if (targetState.ordinal > initialState.ordinal) {
                slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
            } else {
                slideInHorizontally { -it } + fadeIn() togetherWith
                    slideOutHorizontally { it } + fadeOut()
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { pane ->
        when (pane) {
            CallLogsPane.LIST -> CallLogsList(
                callLogs = uiState.callLogs,
                contacts = uiState.contacts,
                selectedCallLogId = null,
                isRefreshing = uiState.isRefreshing,
                isLoggedIn = uiState.isLoggedIn,
                onCallLogClick = viewModel::selectCallLog,
                onRefresh = viewModel::refresh,
                onLoginClick = onLoginClick,
                modifier = Modifier.fillMaxSize(),
            )
            CallLogsPane.DETAILS -> {
                val callLog = uiState.selectedCallLog
                if (callLog != null) {
                    CallLogDetails(
                        callLog = callLog,
                        contact = uiState.contacts[callLog.phoneNumber],
                        numberCallLogs = uiState.selectedNumberCallLogs,
                        onBackClick = viewModel::goBack,
                        onAddContactClick = onAddContact,
                        onShowContactClick = onShowContact,
                        onCallLogClick = viewModel::selectCallLog,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedCallLogsLayout(
    uiState: CallLogsUiState,
    viewModel: CallLogsViewModel,
    onAddContact: (String) -> Unit,
    onShowContact: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    Row(Modifier.fillMaxSize()) {
        CallLogsList(
            callLogs = uiState.callLogs,
            contacts = uiState.contacts,
            selectedCallLogId = uiState.selectedCallLog?.id,
            isRefreshing = uiState.isRefreshing,
            isLoggedIn = uiState.isLoggedIn,
            onCallLogClick = viewModel::selectCallLog,
            onRefresh = viewModel::refresh,
            onLoginClick = onLoginClick,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )

        AnimatedContent(
            targetState = uiState.currentPane,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                        slideOutHorizontally { it } + fadeOut()
                }
            },
            modifier = Modifier.weight(1f).fillMaxHeight(),
        ) { pane ->
            when (pane) {
                CallLogsPane.LIST -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(Res.string.select_call_log),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                CallLogsPane.DETAILS -> {
                    val callLog = uiState.selectedCallLog
                    if (callLog != null) {
                        CallLogDetails(
                            callLog = callLog,
                            contact = uiState.contacts[callLog.phoneNumber],
                            numberCallLogs = uiState.selectedNumberCallLogs,
                            onBackClick = viewModel::goBack,
                            onAddContactClick = onAddContact,
                            onShowContactClick = onShowContact,
                            onCallLogClick = viewModel::selectCallLog,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

