package com.klemfner.whoscalling.ui.calllogs.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.ui.common.components.CallLogIcon
import com.klemfner.whoscalling.ui.common.components.FormattedPhoneText
import com.klemfner.whoscalling.ui.common.utils.TimePeriod
import com.klemfner.whoscalling.ui.common.utils.formatDuration
import com.klemfner.whoscalling.ui.common.utils.formatShortDate
import com.klemfner.whoscalling.ui.common.utils.formatTimestamp
import com.klemfner.whoscalling.ui.common.utils.getTimePeriod
import com.klemfner.whoscalling.util.formatPhoneForDisplay

@Composable
fun CallLogListItem(
    callLog: CallLog,
    contact: Contact?,
    isSelected: Boolean,
    onClick: () -> Unit,
    defaultCountryIso: String = "",
    modifier: Modifier = Modifier,
) {
    val period = getTimePeriod(callLog.timestamp)
    val displayTime = if (period == TimePeriod.TODAY || period == TimePeriod.YESTERDAY) {
        formatTimestamp(callLog.timestamp)
    } else {
        formatShortDate(callLog.timestamp)
    }
    val formattedPhone = remember(callLog.phoneNumber, defaultCountryIso) {
        formatPhoneForDisplay(callLog.phoneNumber, defaultCountryIso)
    }

    ListItem(
        leadingContent = {
            CallLogIcon(callLog)
        },
        headlineContent = {
            if (contact != null) {
                Text("${contact.name} (${formatDuration(callLog.duration)})")
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FormattedPhoneText(
                        formattedPhone = formattedPhone,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        "(${formatDuration(callLog.duration)})",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        },
        trailingContent = {
            Text(
                displayTime,
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
