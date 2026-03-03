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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.klemfner.whoscalling.ui.common.utils.LocalIsExpanded
import com.klemfner.whoscalling.ui.common.utils.PlatformBackHandler
import com.klemfner.whoscalling.ui.contacts.components.ContactDetails
import com.klemfner.whoscalling.ui.contacts.components.ContactForm
import com.klemfner.whoscalling.ui.contacts.components.ContactList
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.select_contact

@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isExpanded = LocalIsExpanded.current

    PlatformBackHandler(enabled = uiState.currentPane != ContactsPane.LIST) {
        viewModel.goBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusable()
            .onPreviewKeyEvent { event ->
                handleKeyEvent(event, uiState, viewModel)
            },
    ) {
        if (isExpanded) {
            ExpandedContactsLayout(uiState, viewModel)
        } else {
            CompactContactsLayout(uiState, viewModel)
        }
    }
}

private fun handleKeyEvent(
    event: KeyEvent,
    uiState: ContactsUiState,
    viewModel: ContactsViewModel,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    return when {
        event.key == Key.Escape -> {
            viewModel.goBack(); true
        }
        event.isCtrlPressed && event.key == Key.N -> {
            viewModel.openAddContact(); true
        }
        event.isCtrlPressed && event.key == Key.R &&
            uiState.currentPane == ContactsPane.DETAILS -> {
            viewModel.openEditContact(); true
        }
        else -> false
    }
}

@Composable
private fun CompactContactsLayout(
    uiState: ContactsUiState,
    viewModel: ContactsViewModel,
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
            ContactsPane.LIST -> ContactList(
                contacts = uiState.contacts,
                callCounts = uiState.callCounts,
                selectedContactId = null,
                onContactClick = viewModel::selectContact,
                onAddClick = viewModel::openAddContact,
                modifier = Modifier.fillMaxSize(),
            )
            ContactsPane.DETAILS -> {
                val contact = uiState.selectedContact
                if (contact != null) {
                    ContactDetails(
                        contact = contact,
                        callLogs = uiState.contactCallLogs,
                        onBackClick = viewModel::goBack,
                        onEditClick = viewModel::openEditContact,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            ContactsPane.FORM -> ContactForm(
                formState = uiState.formState,
                onNameChange = viewModel::updateFormName,
                onPhoneChange = viewModel::updateFormPhone,
                onEmailChange = viewModel::updateFormEmail,
                onSave = viewModel::saveContact,
                onCancel = viewModel::goBack,
                errorMessage = uiState.errorMessage,
                onErrorDismiss = viewModel::clearError,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ExpandedContactsLayout(
    uiState: ContactsUiState,
    viewModel: ContactsViewModel,
) {
    Row(Modifier.fillMaxSize()) {
        ContactList(
            contacts = uiState.contacts,
            callCounts = uiState.callCounts,
            selectedContactId = uiState.selectedContact?.id,
            onContactClick = viewModel::selectContact,
            onAddClick = viewModel::openAddContact,
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
                ContactsPane.DETAILS -> {
                    val contact = uiState.selectedContact
                    if (contact != null) {
                        ContactDetails(
                            contact = contact,
                            callLogs = uiState.contactCallLogs,
                            onBackClick = viewModel::goBack,
                            onEditClick = viewModel::openEditContact,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                ContactsPane.FORM -> ContactForm(
                    formState = uiState.formState,
                    onNameChange = viewModel::updateFormName,
                    onPhoneChange = viewModel::updateFormPhone,
                    onEmailChange = viewModel::updateFormEmail,
                    onSave = viewModel::saveContact,
                    onCancel = viewModel::goBack,
                    errorMessage = uiState.errorMessage,
                    onErrorDismiss = viewModel::clearError,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
