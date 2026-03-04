package com.klemfner.whoscalling.ui.calllogs.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.PhoneMissed
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.ui.common.utils.TimePeriod
import com.klemfner.whoscalling.ui.common.utils.formatDuration
import com.klemfner.whoscalling.ui.common.utils.formatTimestamp
import com.klemfner.whoscalling.ui.common.utils.getTimePeriod
import com.klemfner.whoscalling.ui.contacts.components.CallLogItem
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.add_contact
import whoscalling.composeapp.generated.resources.back
import whoscalling.composeapp.generated.resources.call_logs_by_number
import whoscalling.composeapp.generated.resources.details
import whoscalling.composeapp.generated.resources.duration_label
import whoscalling.composeapp.generated.resources.incoming_call
import whoscalling.composeapp.generated.resources.long_time_ago
import whoscalling.composeapp.generated.resources.missed_incoming_call
import whoscalling.composeapp.generated.resources.missed_outgoing_call
import whoscalling.composeapp.generated.resources.no_call_logs
import whoscalling.composeapp.generated.resources.outgoing_call
import whoscalling.composeapp.generated.resources.this_month
import whoscalling.composeapp.generated.resources.this_week
import whoscalling.composeapp.generated.resources.today
import whoscalling.composeapp.generated.resources.yesterday

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CallLogDetails(
    callLog: CallLog,
    contact: Contact?,
    numberCallLogs: List<CallLog>,
    onBackClick: () -> Unit,
    onAddContactClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.details)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back),
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        val timePeriodGroups = remember(numberCallLogs) {
            numberCallLogs.groupBy { getTimePeriod(it.timestamp) }
                .toSortedMap(compareBy { it.ordinal })
        }

        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
        ) {
            item {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (contact != null) {
                        Text(contact.name, style = MaterialTheme.typography.headlineMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                callLog.phoneNumber,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    } else {
                        Text(
                            callLog.phoneNumber,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }

                    if (contact == null) {
                        TextButton(onClick = { onAddContactClick(callLog.phoneNumber) }) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(Res.string.add_contact))
                        }
                    }

                    HorizontalDivider()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val callTypeText: String
                        if (callLog.missed) {
                            Icon(
                                Icons.AutoMirrored.Filled.PhoneMissed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp),
                            )
                            callTypeText = when (callLog.type) {
                                CallType.INCOMING -> stringResource(Res.string.missed_incoming_call)
                                CallType.OUTGOING -> stringResource(Res.string.missed_outgoing_call)
                            }
                        } else {
                            when (callLog.type) {
                                CallType.INCOMING -> {
                                    Icon(
                                        Icons.AutoMirrored.Filled.CallReceived,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    callTypeText = stringResource(Res.string.incoming_call)
                                }
                                CallType.OUTGOING -> {
                                    Icon(
                                        Icons.AutoMirrored.Filled.CallMade,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    callTypeText = stringResource(Res.string.outgoing_call)
                                }
                            }
                        }
                        Text(callTypeText, style = MaterialTheme.typography.bodyLarge)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            "${stringResource(Res.string.duration_label)}: ${formatDuration(callLog.duration)}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            formatTimestamp(callLog.timestamp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                HorizontalDivider()
            }

            item {
                Text(
                    stringResource(Res.string.call_logs_by_number),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }

            if (numberCallLogs.isEmpty()) {
                item {
                    Text(
                        stringResource(Res.string.no_call_logs),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            timePeriodGroups.forEach { (period, logs) ->
                stickyHeader(key = "period_${period.name}") {
                    val periodText = when (period) {
                        TimePeriod.TODAY -> stringResource(Res.string.today)
                        TimePeriod.YESTERDAY -> stringResource(Res.string.yesterday)
                        TimePeriod.THIS_WEEK -> stringResource(Res.string.this_week)
                        TimePeriod.THIS_MONTH -> stringResource(Res.string.this_month)
                        TimePeriod.LONG_TIME_AGO -> stringResource(Res.string.long_time_ago)
                    }
                    Text(
                        text = periodText,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
                items(logs, key = { it.id }) { log ->
                    CallLogItem(callLog = log)
                }
            }
        }
    }
}
