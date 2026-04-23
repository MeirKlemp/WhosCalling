package com.klemfner.whoscalling.ui.contacts

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.ui.common.components.ConfirmDeleteDialog
import com.klemfner.whoscalling.ui.common.components.TrustNumberDialog
import com.klemfner.whoscalling.ui.common.components.ReportSpamDialog
import com.klemfner.whoscalling.ui.common.utils.LocalIsExpanded
import com.klemfner.whoscalling.ui.common.utils.LocalIsTouchMode
import com.klemfner.whoscalling.ui.common.utils.PlatformBackHandler
import com.klemfner.whoscalling.ui.contacts.contact_form.ContactForm
import com.klemfner.whoscalling.ui.contacts.contacts_list.ContactsListViewModel
import com.klemfner.whoscalling.ui.contacts.contacts_list.ContactsList
import com.klemfner.whoscalling.ui.contacts.contact_details.ContactDetails
import com.klemfner.whoscalling.ui.navigation.LocalNavigator
import com.klemfner.whoscalling.ui.navigation.NavAction
import com.klemfner.whoscalling.ui.navigation.NavigationTab
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.add_contact
import whoscalling.composeapp.generated.resources.delete
import whoscalling.composeapp.generated.resources.edit_contact
import whoscalling.composeapp.generated.resources.select_contact

@Composable
fun ContactsScreen(
    modifier: Modifier = Modifier,
    screenVM: ContactsViewModel = koinViewModel(),
    listVM: ContactsListViewModel = koinViewModel(),
) {
    val screenState by screenVM.uiState.collectAsStateWithLifecycle()
    val listState by listVM.uiState.collectAsStateWithLifecycle()
    val isExpanded = LocalIsExpanded.current
    val isTouchMode = LocalIsTouchMode.current
    val navigator = LocalNavigator.current

    LaunchedEffect(navigator.navState.action) {
        val action = navigator.navState.action
        when (action) {
            is NavAction.AddContact -> {
                screenVM.openAddContact(action.phoneNumber)
                navigator.consumeAction()
            }
            is NavAction.ShowContact -> {
                val contact = listState.contacts.find { it.id == action.contactId }
                if (contact != null) {
                    screenVM.selectContact(contact)
                }
                navigator.consumeAction()
            }
            else -> {}
        }
    }

    PlatformBackHandler(enabled = screenState.currentPane != ContactsPane.LIST) {
        screenVM.goBack()
    }

    if (screenState.showDeleteDialog) {
        ConfirmDeleteDialog(
            contactName = screenState.deleteDialogContactName,
            deleteCount = screenState.pendingDeleteIds.size,
            onConfirm = {
                screenVM.confirmDelete()
                listVM.exitDeleteMode()
            },
            onDismiss = screenVM::dismissDeleteDialog,
        )
    }

    if (screenState.showReportSpamDialog) {
        ReportSpamDialog(
            phoneNumber = screenState.reportDialogPhoneNumber,
            contactName = screenState.selectedContact?.name,
            defaultCountryIso = screenState.defaultCountryIso,
            onConfirm = screenVM::confirmReportSpam,
            onDismiss = screenVM::dismissReportDialog,
        )
    }

    if (screenState.showTrustNumberDialog) {
        TrustNumberDialog(
            phoneNumber = screenState.reportDialogPhoneNumber,
            contactName = screenState.selectedContact?.name,
            defaultCountryIso = screenState.defaultCountryIso,
            onConfirm = screenVM::confirmReportSafe,
            onDismiss = screenVM::dismissReportDialog,
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusable()
            .onPreviewKeyEvent { event ->
                handleKeyEvent(event, screenState, screenVM, listVM)
            },
    ) {
        val onCallLogClick: (CallLog) -> Unit = { callLog ->
            navigator.navigateTo(
                NavigationTab.CALL_LOGS,
                NavAction.ShowCallLog(callLog.id),
            )
        }
        if (isExpanded) {
            ExpandedContactsLayout(screenState, screenVM, listVM, onCallLogClick)
        } else {
            CompactContactsLayout(screenState, screenVM, listVM, onCallLogClick)
        }

        // FAB (touch mode only)
        if (isTouchMode) {
            when {
                listState.isDeleteMode -> FloatingActionButton(
                    onClick = {
                        screenVM.requestDeleteSelectedContacts(
                            listState.selectedForDeletion,
                            listState.contacts,
                        )
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.delete))
                }
                screenState.currentPane == ContactsPane.FORM -> { /* no FAB */ }
                screenState.currentPane == ContactsPane.DETAILS -> FloatingActionButton(
                    onClick = screenVM::openEditContact,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
                ) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.edit_contact))
                }
                else -> FloatingActionButton(
                    onClick = { screenVM.openAddContact() },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.add_contact))
                }
            }
        }
    }
}

private fun handleKeyEvent(
    event: KeyEvent,
    screenState: ContactsUiState,
    screenVM: ContactsViewModel,
    listVM: ContactsListViewModel,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    return when {
        event.key == Key.Escape -> {
            if (listVM.uiState.value.isDeleteMode) listVM.exitDeleteMode()
            else screenVM.goBack()
            true
        }
        event.isCtrlPressed && event.key == Key.N -> {
            screenVM.openAddContact(); true
        }
        event.isCtrlPressed && event.key == Key.E &&
            screenState.currentPane == ContactsPane.DETAILS -> {
            screenVM.openEditContact(); true
        }
        else -> false
    }
}

@Composable
private fun CompactContactsLayout(
    screenState: ContactsUiState,
    screenVM: ContactsViewModel,
    listVM: ContactsListViewModel,
    onCallLogClick: (CallLog) -> Unit,
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
            ContactsPane.LIST -> ContactsList(
                screenVM = screenVM,
                viewModel = listVM,
                modifier = Modifier.fillMaxSize(),
            )
            ContactsPane.DETAILS -> ContactDetails(
                screenVM = screenVM,
                onCallLogClick = onCallLogClick,
                modifier = Modifier.fillMaxSize(),
            )
            ContactsPane.FORM -> ContactForm(
                screenVM = screenVM,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ExpandedContactsLayout(
    screenState: ContactsUiState,
    screenVM: ContactsViewModel,
    listVM: ContactsListViewModel,
    onCallLogClick: (CallLog) -> Unit,
) {
    Row(Modifier.fillMaxSize()) {
        ContactsList(
            screenVM = screenVM,
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
                ContactsPane.LIST -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(Res.string.select_contact),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                ContactsPane.DETAILS -> ContactDetails(
                    screenVM = screenVM,
                    onCallLogClick = onCallLogClick,
                    modifier = Modifier.fillMaxSize(),
                )
                ContactsPane.FORM -> ContactForm(
                    screenVM = screenVM,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

