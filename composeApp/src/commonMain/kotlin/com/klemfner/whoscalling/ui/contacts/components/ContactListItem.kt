package com.klemfner.whoscalling.ui.contacts.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.ui.common.components.FormattedPhoneText
import com.klemfner.whoscalling.util.formatPhoneForDisplay
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.spam_warning

@Composable
fun ContactListItem(
    contact: Contact,
    callCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    defaultCountryIso: String = "",
    isDeleteMode: Boolean = false,
    isChecked: Boolean = false,
    isSpam: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val formattedPhone = remember(contact.phoneNumber, defaultCountryIso) {
        formatPhoneForDisplay(contact.phoneNumber, defaultCountryIso)
    }
    ListItem(
        leadingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                if (isSpam) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = stringResource(Res.string.spam_warning),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        },
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(contact.name)
                if (callCount > 0) {
                    Text(
                        "($callCount)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
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
