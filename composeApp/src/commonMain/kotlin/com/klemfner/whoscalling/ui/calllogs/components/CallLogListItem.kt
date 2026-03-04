package com.klemfner.whoscalling.ui.calllogs.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.PhoneMissed
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.ui.common.utils.formatDuration
import com.klemfner.whoscalling.ui.common.utils.formatTimestamp
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.incoming
import whoscalling.composeapp.generated.resources.missed
import whoscalling.composeapp.generated.resources.outgoing

@Composable
fun CallLogListItem(
    callLog: CallLog,
    contact: Contact?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        leadingContent = {
            if (callLog.missed) {
                Icon(
                    Icons.AutoMirrored.Filled.PhoneMissed,
                    contentDescription = stringResource(Res.string.missed),
                    tint = MaterialTheme.colorScheme.error,
                )
            } else {
                when (callLog.type) {
                    CallType.INCOMING -> Icon(
                        Icons.AutoMirrored.Filled.CallReceived,
                        contentDescription = stringResource(Res.string.incoming),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    CallType.OUTGOING -> Icon(
                        Icons.AutoMirrored.Filled.CallMade,
                        contentDescription = stringResource(Res.string.outgoing),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        },
        headlineContent = {
            Text(
                buildString {
                    append(contact?.name ?: callLog.phoneNumber)
                    append(" (${formatDuration(callLog.duration)})")
                },
            )
        },
        trailingContent = {
            Text(
                formatTimestamp(callLog.timestamp),
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
