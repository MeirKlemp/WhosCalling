package com.klemfner.whoscalling.ui.contacts.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.ui.common.components.FormattedPhoneText
import com.klemfner.whoscalling.util.formatPhoneForDisplay

@Composable
fun ContactListItem(
    contact: Contact,
    callCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    defaultCountryIso: String = "",
    isDeleteMode: Boolean = false,
    isChecked: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val formattedPhone = remember(contact.phoneNumber, defaultCountryIso) {
        formatPhoneForDisplay(contact.phoneNumber, defaultCountryIso)
    }
    ListItem(
        leadingContent = {
            AnimatedVisibility(
                visible = isDeleteMode,
                enter = expandHorizontally(expandFrom = Alignment.Start),
                exit = shrinkHorizontally(shrinkTowards = Alignment.Start),
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = null,
                )
            }
        },
        headlineContent = {
            Text(
                buildString {
                    append(contact.name)
                    if (callCount > 0) append(" ($callCount)")
                },
            )
        },
        trailingContent = {
            FormattedPhoneText(
                formattedPhone = formattedPhone,
                style = MaterialTheme.typography.bodyMedium,
            )
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
