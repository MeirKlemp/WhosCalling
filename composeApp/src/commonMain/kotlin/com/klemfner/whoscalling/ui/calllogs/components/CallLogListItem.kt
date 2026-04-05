package com.klemfner.whoscalling.ui.calllogs.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.ui.common.components.CallLogIcon
import com.klemfner.whoscalling.ui.common.components.FormattedPhoneText
import com.klemfner.whoscalling.ui.common.utils.TimePeriod
import com.klemfner.whoscalling.ui.common.utils.formatDuration
import com.klemfner.whoscalling.ui.common.utils.formatShortDate
import com.klemfner.whoscalling.ui.common.utils.formatShortTime
import com.klemfner.whoscalling.ui.common.utils.getTimePeriod
import com.klemfner.whoscalling.util.formatPhoneForDisplay
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.spam_warning

@Composable
fun CallLogListItem(
    callLog: CallLog,
    contact: Contact?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    defaultCountryIso: String = "",
    isSpam: Boolean = false,
) {
    val formattedPhone = remember(callLog.phoneNumber, defaultCountryIso) {
        formatPhoneForDisplay(callLog.phoneNumber, defaultCountryIso)
    }

    ListItem(
        leadingContent = {
            CallLogIcon(callLog)
        },
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (isSpam) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = stringResource(Res.string.spam_warning),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                }
                if (contact != null) {
                    Text(
                        contact.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    FormattedPhoneText(
                        formattedPhone = formattedPhone,
                        style = MaterialTheme.typography.bodyLarge,
                        overflowWithEllipsis = true,
                    )
                }
            }
        },
        trailingContent = {
            Text(
                "${formatShortDate(callLog.timestamp)} ${formatShortTime(callLog.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
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
