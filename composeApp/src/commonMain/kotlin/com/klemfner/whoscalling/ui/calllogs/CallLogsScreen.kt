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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.klemfner.whoscalling.ui.calllogs.calllog_details.CallLogDetails
import com.klemfner.whoscalling.ui.calllogs.calllogs_list.CallLogsListViewModel
import com.klemfner.whoscalling.ui.calllogs.calllogs_list.CallLogsList
import com.klemfner.whoscalling.ui.common.components.TrustNumberDialog
import com.klemfner.whoscalling.ui.common.components.ReportSpamDialog
import com.klemfner.whoscalling.ui.common.utils.LocalIsExpanded
import com.klemfner.whoscalling.ui.common.utils.LocalIsTouchMode
import com.klemfner.whoscalling.ui.common.utils.PlatformBackHandler
import com.klemfner.whoscalling.ui.navigation.LocalNavigator
import com.klemfner.whoscalling.ui.navigation.NavAction
import com.klemfner.whoscalling.ui.navigation.NavigationTab
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.add_contact
import whoscalling.composeapp.generated.resources.failed_to_refresh
import whoscalling.composeapp.generated.resources.select_call_log
import whoscalling.composeapp.generated.resources.show_contact

@Composable
fun CallLogsScreen(
    modifier: Modifier = Modifier,
    screenVM: CallLogsViewModel = koinViewModel(),
    listVM: CallLogsListViewModel = koinViewModel(),
) {
    val screenState by screenVM.uiState.collectAsStateWithLifecycle()
    val listState by listVM.uiState.collectAsStateWithLifecycle()
    val isExpanded = LocalIsExpanded.current
    val isTouchMode = LocalIsTouchMode.current
    val navigator = LocalNavigator.current

    PlatformBackHandler(enabled = screenState.currentPane != CallLogsPane.LIST) {
        screenVM.goBack()
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
            val callLog = listState.callLogs.find { it.id == action.callLogId }
            if (callLog != null) screenVM.selectCallLog(callLog)
            navigator.consumeAction()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val failedToRefreshMessage = stringResource(Res.string.failed_to_refresh)

    LaunchedEffect(listState.refreshError) {
        if (listState.refreshError) {
            snackbarHostState.showSnackbar(failedToRefreshMessage)
            listVM.clearRefreshError()
        }
    }

    if (screenState.showReportSpamDialog) {
        ReportSpamDialog(
            phoneNumber = screenState.reportDialogPhoneNumber,
            contactName = listState.contacts[screenState.reportDialogPhoneNumber]?.name,
            defaultCountryIso = listState.defaultCountryIso,
            onConfirm = screenVM::confirmReportSpam,
            onDismiss = screenVM::dismissReportDialog,
        )
    }

    if (screenState.showTrustNumberDialog) {
        TrustNumberDialog(
            phoneNumber = screenState.reportDialogPhoneNumber,
            contactName = listState.contacts[screenState.reportDialogPhoneNumber]?.name,
            defaultCountryIso = listState.defaultCountryIso,
            onConfirm = screenVM::confirmReportSafe,
            onDismiss = screenVM::dismissReportDialog,
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusable()
            .onPreviewKeyEvent { event ->
                handleKeyEvent(event, listVM, listState.isLoggedIn)
            },
    ) {
        if (isExpanded) {
            ExpandedCallLogsLayout(screenState, screenVM, listVM, onAddContact, onShowContact, onLoginClick)
        } else {
            CompactCallLogsLayout(screenState, screenVM, listVM, onAddContact, onShowContact, onLoginClick)
        }

        // FAB (touch mode only, details pane)
        if (isTouchMode && screenState.currentPane == CallLogsPane.DETAILS) {
            val callLog = screenState.selectedCallLog
            if (callLog != null) {
                val contact = listState.contacts[callLog.phoneNumber]
                if (contact == null) {
                    FloatingActionButton(
                        onClick = { onAddContact(callLog.phoneNumber) },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = stringResource(Res.string.add_contact))
                    }
                } else {
                    FloatingActionButton(
                        onClick = { onShowContact(contact.id) },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
                    ) {
                        Icon(Icons.Default.Person, contentDescription = stringResource(Res.string.show_contact))
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private fun handleKeyEvent(
    event: KeyEvent,
    listVM: CallLogsListViewModel,
    isLoggedIn: Boolean,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    return when {
        event.key == Key.Escape -> {
            false // handled by PlatformBackHandler
        }
        isLoggedIn && event.isCtrlPressed && event.key == Key.R -> {
            listVM.refresh(); true
        }
        isLoggedIn && event.key == Key.F5 -> {
            listVM.refresh(); true
        }
        else -> false
    }
}

@Composable
private fun CompactCallLogsLayout(
    screenState: CallLogsUiState,
    screenVM: CallLogsViewModel,
    listVM: CallLogsListViewModel,
    onAddContact: (String) -> Unit,
    onShowContact: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    AnimatedContent(
        targetState = screenState.currentPane,
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
                screenVM = screenVM,
                onLoginClick = onLoginClick,
                viewModel = listVM,
                modifier = Modifier.fillMaxSize(),
            )
            CallLogsPane.DETAILS -> CallLogDetails(
                screenVM = screenVM,
                listVM = listVM,
                onAddContactClick = onAddContact,
                onShowContactClick = onShowContact,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ExpandedCallLogsLayout(
    screenState: CallLogsUiState,
    screenVM: CallLogsViewModel,
    listVM: CallLogsListViewModel,
    onAddContact: (String) -> Unit,
    onShowContact: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    Row(Modifier.fillMaxSize()) {
        CallLogsList(
            screenVM = screenVM,
            onLoginClick = onLoginClick,
            viewModel = listVM,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )

        AnimatedContent(
            targetState = screenState.currentPane,
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
                CallLogsPane.DETAILS -> CallLogDetails(
                    screenVM = screenVM,
                    listVM = listVM,
                    onAddContactClick = onAddContact,
                    onShowContactClick = onShowContact,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

