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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
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
import com.klemfner.whoscalling.ui.common.components.CallLogIcon
import com.klemfner.whoscalling.ui.common.components.FormattedPhoneText
import com.klemfner.whoscalling.ui.common.utils.TimePeriod
import com.klemfner.whoscalling.ui.common.utils.formatDuration
import com.klemfner.whoscalling.ui.common.utils.formatShortDate
import com.klemfner.whoscalling.ui.common.utils.formatTimestamp
import com.klemfner.whoscalling.ui.common.utils.getTimePeriod
import com.klemfner.whoscalling.ui.common.components.ContactCallLogItem
import com.klemfner.whoscalling.util.FormattedPhone
import com.klemfner.whoscalling.util.formatPhoneForDisplay
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.add_contact
import whoscalling.composeapp.generated.resources.back
import whoscalling.composeapp.generated.resources.call_logs_by_number_count
import whoscalling.composeapp.generated.resources.details
import whoscalling.composeapp.generated.resources.duration_label
import whoscalling.composeapp.generated.resources.incoming_call
import whoscalling.composeapp.generated.resources.incoming_ringing_call
import whoscalling.composeapp.generated.resources.long_time_ago
import whoscalling.composeapp.generated.resources.missed_incoming_call
import whoscalling.composeapp.generated.resources.missed_outgoing_call
import whoscalling.composeapp.generated.resources.no_call_logs
import whoscalling.composeapp.generated.resources.outgoing_call
import whoscalling.composeapp.generated.resources.show_contact
import whoscalling.composeapp.generated.resources.this_month
import whoscalling.composeapp.generated.resources.this_week
import whoscalling.composeapp.generated.resources.time_label
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
    onShowContactClick: (String) -> Unit,
    onCallLogClick: (CallLog) -> Unit,
    defaultCountryIso: String = "",
    isRinging: Boolean = false,
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
        val formattedPhone = remember(callLog.phoneNumber, defaultCountryIso) {
            formatPhoneForDisplay(callLog.phoneNumber, defaultCountryIso)
        }
        val timePeriodGroups = remember(numberCallLogs) {
            numberCallLogs.groupBy { getTimePeriod(it.timestamp) }
                .toSortedMap(compareBy { it.ordinal })
        }

        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
        ) {
            item {
                CallLogHeader(
                    callLog = callLog,
                    contact = contact,
                    formattedPhone = formattedPhone,
                    onAddContactClick = onAddContactClick,
                    onShowContactClick = onShowContactClick,
                    isRinging = isRinging,
                )
                HorizontalDivider()
            }

            item {
                Text(
                    stringResource(Res.string.call_logs_by_number_count, numberCallLogs.size),
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
                    TimePeriodHeader(period)
                }
                items(logs, key = { it.id }) { log ->
                    ContactCallLogItem(callLog = log, isSelected = log.id == callLog.id, onClick = { onCallLogClick(log) })
                }
            }
        }
    }
}

@Composable
private fun CallLogHeader(
    callLog: CallLog,
    contact: Contact?,
    formattedPhone: FormattedPhone,
    onAddContactClick: (String) -> Unit,
    onShowContactClick: (String) -> Unit,
    isRinging: Boolean = false,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SelectionContainer {
            ContactInfo(callLog = callLog, contact = contact, formattedPhone = formattedPhone)
        }

        if (contact == null) {
            AddContactButton(phoneNumber = callLog.phoneNumber, onClick = onAddContactClick)
        } else {
            ShowContactButton(contactId = contact.id, onClick = onShowContactClick)
        }

        HorizontalDivider()

        CallTypeRow(callLog, isRinging)
        TimeRow(callLog)
        DurationRow(callLog)
    }
}

@Composable
private fun ContactInfo(
    callLog: CallLog,
    contact: Contact?,
    formattedPhone: FormattedPhone,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (contact != null) {
            Text(contact.name, style = MaterialTheme.typography.headlineMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                FormattedPhoneText(formattedPhone = formattedPhone)
            }
        } else {
            FormattedPhoneText(
                formattedPhone = formattedPhone,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}

@Composable
private fun AddContactButton(
    phoneNumber: String,
    onClick: (String) -> Unit,
) {
    TextButton(onClick = { onClick(phoneNumber) }) {
        Icon(
            Icons.Default.PersonAdd,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(stringResource(Res.string.add_contact))
    }
}

@Composable
private fun ShowContactButton(
    contactId: String,
    onClick: (String) -> Unit,
) {
    TextButton(onClick = { onClick(contactId) }) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(stringResource(Res.string.show_contact))
    }
}

@Composable
private fun CallTypeRow(callLog: CallLog, isRinging: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CallLogIcon(callLog, isRinging = isRinging, modifier = Modifier.size(20.dp))

        val callTypeText = when {
            isRinging ->
                stringResource(Res.string.incoming_ringing_call)
            callLog.missed && callLog.type == CallType.INCOMING ->
                stringResource(Res.string.missed_incoming_call)
            callLog.missed && callLog.type == CallType.OUTGOING ->
                stringResource(Res.string.missed_outgoing_call)
            callLog.type == CallType.INCOMING ->
                stringResource(Res.string.incoming_call)
            else ->
                stringResource(Res.string.outgoing_call)
        }
        Text(callTypeText, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun TimeRow(callLog: CallLog) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Default.Schedule,
            contentDescription = stringResource(Res.string.time_label),
            modifier = Modifier.size(20.dp),
        )
        Text(
            "${formatShortDate(callLog.timestamp)} ${formatTimestamp(callLog.timestamp)}",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun DurationRow(callLog: CallLog) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Default.Timer,
            contentDescription = stringResource(Res.string.duration_label),
            modifier = Modifier.size(20.dp),
        )
        Text(
            "${stringResource(Res.string.duration_label)}: ${formatDuration(callLog.duration)}",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun TimePeriodHeader(period: TimePeriod) {
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
