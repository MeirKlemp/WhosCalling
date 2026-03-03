package com.klemfner.whoscalling.ui.contacts.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.ui.common.theme.AppTheme
import com.klemfner.whoscalling.ui.common.utils.previewContacts
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ContactListItem(
    contact: Contact,
    callCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = {
            Text(
                buildString {
                    append(contact.name)
                    if (callCount > 0) append(" ($callCount)")
                },
            )
        },
        trailingContent = {
            Text(contact.phoneNumber, style = MaterialTheme.typography.bodyMedium)
        },
        colors = if (isSelected) {
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            )
        } else {
            ListItemDefaults.colors()
        },
        modifier = modifier.clickable(onClick = onClick),
    )
}

@Preview
@Composable
private fun ContactListItemLightPreview() {
    AppTheme(darkTheme = false) {
        ContactListItem(
            contact = previewContacts[0],
            callCount = 3,
            isSelected = false,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun ContactListItemDarkPreview() {
    AppTheme(darkTheme = true) {
        ContactListItem(
            contact = previewContacts[0],
            callCount = 3,
            isSelected = true,
            onClick = {},
        )
    }
}
