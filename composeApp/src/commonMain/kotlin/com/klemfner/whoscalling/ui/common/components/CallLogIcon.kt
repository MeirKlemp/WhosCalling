package com.klemfner.whoscalling.ui.common.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.PhoneMissed
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.incoming
import whoscalling.composeapp.generated.resources.missed
import whoscalling.composeapp.generated.resources.outgoing
import whoscalling.composeapp.generated.resources.unknown

@Composable
fun CallLogIcon(
    callLog: CallLog,
    modifier: Modifier = Modifier,
    isRinging: Boolean = false,
) {
    if (isRinging) {
        Icon(
            Icons.Default.Phone,
            contentDescription = stringResource(Res.string.incoming),
            tint = MaterialTheme.colorScheme.primary,
            modifier = modifier,
        )
    } else if (callLog.missed) {
        Icon(
            Icons.AutoMirrored.Filled.PhoneMissed,
            contentDescription = stringResource(Res.string.missed),
            tint = MaterialTheme.colorScheme.error,
            modifier = modifier,
        )
    } else {
        when (callLog.type) {
            CallType.INCOMING -> Icon(
                Icons.AutoMirrored.Filled.CallReceived,
                contentDescription = stringResource(Res.string.incoming),
                tint = MaterialTheme.colorScheme.primary,
                modifier = modifier,
            )
            CallType.OUTGOING -> Icon(
                Icons.AutoMirrored.Filled.CallMade,
                contentDescription = stringResource(Res.string.outgoing),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = modifier,
            )
            CallType.UNKNOWN -> Icon(
                Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = stringResource(Res.string.unknown),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = modifier,
            )
        }
    }
}
