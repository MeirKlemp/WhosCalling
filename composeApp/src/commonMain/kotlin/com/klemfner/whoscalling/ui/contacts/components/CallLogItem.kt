package com.klemfner.whoscalling.ui.contacts.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.ui.common.components.CallLogIcon
import com.klemfner.whoscalling.ui.common.utils.TimePeriod
import com.klemfner.whoscalling.ui.common.utils.formatDuration
import com.klemfner.whoscalling.ui.common.utils.formatShortDate
import com.klemfner.whoscalling.ui.common.utils.formatTimestamp
import com.klemfner.whoscalling.ui.common.utils.getTimePeriod

@Composable
fun CallLogItem(
    callLog: CallLog,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val period = getTimePeriod(callLog.timestamp)
    val displayTime = if (period == TimePeriod.TODAY || period == TimePeriod.YESTERDAY) {
        formatTimestamp(callLog.timestamp)
    } else {
        formatShortDate(callLog.timestamp)
    }

    ListItem(
        leadingContent = {
            CallLogIcon(callLog)
        },
        headlineContent = {
            Text(formatDuration(callLog.duration))
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
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
    )
}
