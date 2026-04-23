package com.klemfner.whoscalling.ui.contacts.contacts_list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.ui.common.utils.LocalIsTouchMode
import com.klemfner.whoscalling.ui.contacts.ContactsViewModel
import com.klemfner.whoscalling.ui.contacts.components.ContactListItem
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.add_contact
import whoscalling.composeapp.generated.resources.cancel
import whoscalling.composeapp.generated.resources.contact_count
import whoscalling.composeapp.generated.resources.contacts
import whoscalling.composeapp.generated.resources.delete
import whoscalling.composeapp.generated.resources.delete_contact
import whoscalling.composeapp.generated.resources.no_contacts
import whoscalling.composeapp.generated.resources.select_all
import whoscalling.composeapp.generated.resources.selected_count
import whoscalling.composeapp.generated.resources.unselect_all

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContactsList(
    screenVM: ContactsViewModel,
    modifier: Modifier = Modifier,
    viewModel: ContactsListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenState by screenVM.uiState.collectAsStateWithLifecycle()
    val selectedContactId = screenState.selectedContact?.id
    val isTouchMode = LocalIsTouchMode.current

    Column(modifier = modifier) {
        TopAppBar(
            title = { Text(stringResource(Res.string.contacts)) },
            navigationIcon = {
                if (uiState.isDeleteMode) {
                    IconButton(onClick = viewModel::exitDeleteMode) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(Res.string.cancel),
                        )
                    }
                }
            },
            actions = {
                if (uiState.isDeleteMode) {
                    if (uiState.selectedForDeletion.size == uiState.contacts.size) {
                        TextButton(onClick = viewModel::unselectAllContacts) {
                            Text(stringResource(Res.string.unselect_all))
                        }
                    } else {
                        TextButton(onClick = viewModel::selectAllContacts) {
                            Text(stringResource(Res.string.select_all))
                        }
                    }
                    if (!isTouchMode) {
                        IconButton(
                            onClick = { screenVM.requestDeleteSelectedContacts(uiState.selectedForDeletion, uiState.contacts) },
                            enabled = uiState.selectedForDeletion.isNotEmpty(),
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.delete),
                            )
                        }
                    }
                } else {
                    if (!isTouchMode) {
                        IconButton(onClick = { screenVM.openAddContact() }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(Res.string.add_contact),
                            )
                        }
                    }
                    IconButton(onClick = viewModel::enterDeleteMode, enabled = uiState.contacts.isNotEmpty()) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.delete_contact),
                        )
                    }
                }
            },
        )

        if (uiState.contacts.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(Res.string.no_contacts),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Text(
                text = if (uiState.isDeleteMode) {
                    stringResource(Res.string.selected_count, uiState.selectedForDeletion.size)
                } else {
                    stringResource(Res.string.contact_count, uiState.contacts.size)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            val grouped = uiState.contacts.groupBy {
                it.name.firstOrNull()?.uppercaseChar() ?: '#'
            }

            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                grouped.forEach { (letter, contactsInGroup) ->
                    stickyHeader(key = "header_$letter") {
                        Text(
                            text = letter.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                    items(contactsInGroup, key = { it.id }) { contact ->
                        ContactListItem(
                            contact = contact,
                            callCount = uiState.callCounts[contact.phoneNumber] ?: 0,
                            isSelected = contact.id == selectedContactId,
                            onClick = {
                                if (uiState.isDeleteMode) {
                                    viewModel.toggleContactSelection(contact.id)
                                } else {
                                    screenVM.selectContact(contact)
                                }
                            },
                            defaultCountryIso = uiState.defaultCountryIso,
                            isDeleteMode = uiState.isDeleteMode,
                            isChecked = contact.id in uiState.selectedForDeletion,
                            isSpam = uiState.spams[contact.phoneNumber]?.isSpam == true,
                        )
                    }
                }
            }
        }
    }
}
