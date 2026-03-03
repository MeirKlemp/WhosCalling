package com.klemfner.whoscalling.ui.contacts.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.ui.common.theme.AppTheme
import com.klemfner.whoscalling.ui.common.utils.formatDuration
import com.klemfner.whoscalling.ui.common.utils.formatTimestamp
import com.klemfner.whoscalling.ui.common.utils.previewCallLogs
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.incoming
import whoscalling.composeapp.generated.resources.missed
import whoscalling.composeapp.generated.resources.outgoing

@Composable
fun CallLogItem(
    callLog: CallLog,
    modifier: Modifier = Modifier,
) {
    ListItem(
        leadingContent = {
            if (callLog.missed) {
                Icon(
                    Icons.Default.PhoneMissed,
                    contentDescription = stringResource(Res.string.missed),
                    tint = MaterialTheme.colorScheme.error,
                )
            } else {
                when (callLog.type) {
                    CallType.INCOMING -> Icon(
                        Icons.Default.CallReceived,
                        contentDescription = stringResource(Res.string.incoming),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    CallType.OUTGOING -> Icon(
                        Icons.Default.CallMade,
                        contentDescription = stringResource(Res.string.outgoing),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
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

@Preview
@Composable
private fun CallLogItemLightPreview() {
    AppTheme(darkTheme = false) {
        CallLogItem(callLog = previewCallLogs[0])
    }
}

@Preview
@Composable
private fun CallLogItemDarkPreview() {
    AppTheme(darkTheme = true) {
        CallLogItem(callLog = previewCallLogs[0])
    }
}
