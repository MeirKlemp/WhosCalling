package com.klemfner.whoscalling.ui.contacts.components

import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.ui.common.components.CallLogIcon
import com.klemfner.whoscalling.ui.common.utils.formatDuration
import com.klemfner.whoscalling.ui.common.utils.formatTimestamp

@Composable
fun CallLogItem(
    callLog: CallLog,
    modifier: Modifier = Modifier,
) {
    ListItem(
        leadingContent = {
            CallLogIcon(callLog)
        },
        headlineContent = {
            Text(formatDuration(callLog.duration))
        },
        trailingContent = {
            Text(
                formatTimestamp(callLog.timestamp),
                style = MaterialTheme.typography.bodySmall,
            )
        },
        modifier = modifier,
    )
}
