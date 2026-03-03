package com.klemfner.whoscalling.ui.contacts.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.ui.common.theme.AppTheme
import com.klemfner.whoscalling.ui.common.utils.LocalIsTouchMode
import com.klemfner.whoscalling.ui.common.utils.previewCallCounts
import com.klemfner.whoscalling.ui.common.utils.previewContacts
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.add_contact

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactList(
    contacts: List<Contact>,
    callCounts: Map<String, Int>,
    selectedContactId: String?,
    onContactClick: (Contact) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isTouchMode = LocalIsTouchMode.current
    Scaffold(
        floatingActionButton = {
            if (isTouchMode) {
                FloatingActionButton(onClick = onAddClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(Res.string.add_contact),
                    )
                }
            }
        },
        modifier = modifier,
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            if (!isTouchMode) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    FilledTonalButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.add_contact))
                    }
                }
            }

            val grouped = contacts.groupBy {
                it.name.firstOrNull()?.uppercaseChar() ?: '#'
            }

            LazyColumn(Modifier.fillMaxSize()) {
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
                            onClick = { onContactClick(contact) },
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ContactListLightPreview() {
    AppTheme(darkTheme = false) {
        ContactList(
            contacts = previewContacts,
            callCounts = previewCallCounts,
            selectedContactId = null,
            onContactClick = {},
            onAddClick = {},
        )
    }
}

@Preview
@Composable
private fun ContactListDarkPreview() {
    AppTheme(darkTheme = true) {
        ContactList(
            contacts = previewContacts,
            callCounts = previewCallCounts,
            selectedContactId = "1",
            onContactClick = {},
            onAddClick = {},
        )
    }
}
