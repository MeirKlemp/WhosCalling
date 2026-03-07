package com.klemfner.whoscalling.ui.contacts.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.ui.common.utils.LocalIsTouchMode
import org.jetbrains.compose.resources.stringResource
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
fun ContactList(
    contacts: List<Contact>,
    callCounts: Map<String, Int>,
    selectedContactId: String?,
    onContactClick: (Contact) -> Unit,
    onAddClick: () -> Unit,
    onDeleteModeEnter: () -> Unit,
    onDeleteModeExit: () -> Unit,
    onToggleSelection: (String) -> Unit,
    onSelectAll: () -> Unit,
    onUnselectAll: () -> Unit,
    onDeleteClick: () -> Unit,
    isDeleteMode: Boolean,
    selectedForDeletion: Set<String>,
    defaultCountryIso: String = "",
    modifier: Modifier = Modifier,
) {
    val isTouchMode = LocalIsTouchMode.current

    if (contacts.isEmpty()) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(stringResource(Res.string.contacts)) })
            },
            modifier = modifier,
        ) { paddingValues ->
            Box(
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Text(
                    stringResource(Res.string.no_contacts),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.contacts)) },
                navigationIcon = {
                    if (isDeleteMode) {
                        IconButton(onClick = onDeleteModeExit) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(Res.string.cancel),
                            )
                        }
                    }
                },
                actions = {
                    if (isDeleteMode) {
                        if (selectedForDeletion.size == contacts.size) {
                            TextButton(onClick = onUnselectAll) {
                                Text(stringResource(Res.string.unselect_all))
                            }
                        } else {
                            TextButton(onClick = onSelectAll) {
                                Text(stringResource(Res.string.select_all))
                            }
                        }
                    } else {
                        if (!isTouchMode) {
                            IconButton(onClick = onAddClick) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = stringResource(Res.string.add_contact),
                                )
                            }
                        }
                        IconButton(onClick = onDeleteModeEnter) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.delete_contact),
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (isTouchMode) {
                if (isDeleteMode) {
                    FloatingActionButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.delete),
                        )
                    }
                } else {
                    FloatingActionButton(onClick = onAddClick) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(Res.string.add_contact),
                        )
                    }
                }
            }
        },
        modifier = modifier,
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            Text(
                text = if (isDeleteMode) {
                    stringResource(Res.string.selected_count, selectedForDeletion.size)
                } else {
                    stringResource(Res.string.contact_count, contacts.size)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            val grouped = contacts.groupBy {
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
                            callCount = callCounts[contact.phoneNumber] ?: 0,
                            isSelected = contact.id == selectedContactId,
                            onClick = {
                                if (isDeleteMode) {
                                    onToggleSelection(contact.id)
                                } else {
                                    onContactClick(contact)
                                }
                            },
                            defaultCountryIso = defaultCountryIso,
                            isDeleteMode = isDeleteMode,
                            isChecked = contact.id in selectedForDeletion,
                        )
                    }
                }
            }

            if (!isTouchMode && isDeleteMode) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDeleteModeExit) {
                        Text(stringResource(Res.string.cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onDeleteClick) {
                        Text(stringResource(Res.string.delete))
                    }
                }
            }
        }
    }
}
