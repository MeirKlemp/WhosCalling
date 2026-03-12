package com.klemfner.whoscalling.ui.common.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.ui.common.utils.TimePeriod
import com.klemfner.whoscalling.ui.common.utils.formatDuration
import com.klemfner.whoscalling.ui.common.utils.formatShortDate
import com.klemfner.whoscalling.ui.common.utils.formatShortTime
import com.klemfner.whoscalling.ui.common.utils.getTimePeriod

@Composable
fun ContactCallLogItem(
    callLog: CallLog,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        leadingContent = {
            CallLogIcon(callLog)
        },
        headlineContent = {
            Text(formatShortTime(callLog.timestamp))
        },
        trailingContent = {
            Text(
                formatShortDate(callLog.timestamp),
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
