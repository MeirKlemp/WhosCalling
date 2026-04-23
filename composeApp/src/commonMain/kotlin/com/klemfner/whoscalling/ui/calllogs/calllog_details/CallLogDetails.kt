package com.klemfner.whoscalling.ui.calllogs.calllog_details

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.ui.common.components.CallLogIcon
import com.klemfner.whoscalling.ui.common.components.ContactCallLogItem
import com.klemfner.whoscalling.ui.common.components.FormattedPhoneText
import com.klemfner.whoscalling.ui.common.components.SpamStatusBanner
import com.klemfner.whoscalling.ui.calllogs.CallLogsViewModel
import com.klemfner.whoscalling.ui.calllogs.calllogs_list.CallLogsListViewModel
import com.klemfner.whoscalling.ui.common.utils.LocalIsTouchMode
import com.klemfner.whoscalling.ui.common.utils.TimePeriod
import com.klemfner.whoscalling.ui.common.utils.formatDuration
import com.klemfner.whoscalling.ui.common.utils.formatShortDate
import com.klemfner.whoscalling.ui.common.utils.formatShortTime
import com.klemfner.whoscalling.ui.common.utils.getTimePeriod
import com.klemfner.whoscalling.util.FormattedPhone
import com.klemfner.whoscalling.util.formatPhoneForDisplay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
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
import whoscalling.composeapp.generated.resources.report_spam
import whoscalling.composeapp.generated.resources.show_contact
import whoscalling.composeapp.generated.resources.this_month
import whoscalling.composeapp.generated.resources.this_week
import whoscalling.composeapp.generated.resources.time_label
import whoscalling.composeapp.generated.resources.today
import whoscalling.composeapp.generated.resources.trust_number
import whoscalling.composeapp.generated.resources.unknown_call
import whoscalling.composeapp.generated.resources.yesterday

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CallLogDetails(
    screenVM: CallLogsViewModel,
    listVM: CallLogsListViewModel,
    onAddContactClick: (String) -> Unit,
    onShowContactClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CallLogDetailsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenState by screenVM.uiState.collectAsStateWithLifecycle()
    val listState by listVM.uiState.collectAsStateWithLifecycle()
    val callLog = screenState.selectedCallLog ?: return
    val contact = listState.contacts[callLog.phoneNumber]
    val defaultCountryIso = listState.defaultCountryIso
    val isRinging = callLog.id == listState.ringingCallId
    val isTouchMode = LocalIsTouchMode.current
    val spam = uiState.spams[callLog.phoneNumber]
    val isSpam = spam?.isSpam == true

    LaunchedEffect(callLog.phoneNumber) {
        viewModel.setSelectedPhone(callLog.phoneNumber)
    }

    Column(modifier = modifier) {
        TopAppBar(
            title = { Text(stringResource(Res.string.details)) },
            navigationIcon = {
                IconButton(onClick = screenVM::goBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.back),
                    )
                }
            },
            actions = {
                CallLogDetailsActions(
                    isTouchMode = isTouchMode,
                    isSpam = isSpam,
                    contact = contact,
                    onReportSpam = { screenVM.requestReportSpam(callLog.phoneNumber) },
                    onReportSafe = { screenVM.requestReportSafe(callLog.phoneNumber) },
                    onAddContactClick = { onAddContactClick(callLog.phoneNumber) },
                    onShowContactClick = { onShowContactClick(contact!!.id) },
                )
            },
        )

        val formattedPhone = remember(callLog.phoneNumber, defaultCountryIso) {
            formatPhoneForDisplay(callLog.phoneNumber, defaultCountryIso)
        }
        val timePeriodGroups = remember(uiState.selectedNumberCallLogs) {
            uiState.selectedNumberCallLogs.groupBy { getTimePeriod(it.timestamp) }
                .toSortedMap(compareBy { it.ordinal })
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                CallLogHeader(
                    callLog = callLog,
                    formattedPhone = formattedPhone,
                    contact = contact,
                    isRinging = isRinging,
                    spam = spam,
                    onReportSafe = { screenVM.requestReportSafe(callLog.phoneNumber) },
                )
            }

            item {
                Text(
                    stringResource(Res.string.call_logs_by_number_count, uiState.selectedNumberCallLogs.size),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }

            if (uiState.selectedNumberCallLogs.isEmpty()) {
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
                    ContactCallLogItem(callLog = log, isSelected = log.id == callLog.id, onClick = { screenVM.selectCallLog(log) })
                }
            }
        }
    }
}

@Composable
private fun CallLogDetailsActions(
    isTouchMode: Boolean,
    isSpam: Boolean,
    contact: Contact?,
    onReportSpam: () -> Unit,
    onReportSafe: () -> Unit,
    onAddContactClick: () -> Unit,
    onShowContactClick: () -> Unit,
) {
    if (!isSpam) {
        IconButton(onClick = onReportSpam) {
            Icon(
                Icons.Default.Report,
                contentDescription = stringResource(Res.string.report_spam),
            )
        }
    } else {
        IconButton(onClick = onReportSafe) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = stringResource(Res.string.trust_number),
            )
        }
    }
    if (!isTouchMode) {
        if (contact == null) {
            IconButton(onClick = onAddContactClick) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = stringResource(Res.string.add_contact),
                )
            }
        } else {
            IconButton(onClick = onShowContactClick) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = stringResource(Res.string.show_contact),
                )
            }
        }
    }
}

@Composable
private fun CallLogHeader(
    callLog: CallLog,
    formattedPhone: FormattedPhone,
    contact: Contact?,
    isRinging: Boolean = false,
    spam: com.klemfner.whoscalling.domain.model.Spam? = null,
    onReportSafe: () -> Unit = {},
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SelectionContainer {
            ContactInfo(contact = contact, formattedPhone = formattedPhone)
        }
    }

    SpamStatusBanner(
        spam = spam,
        onReportSafe = onReportSafe,
    )

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HorizontalDivider()
        CallTypeRow(callLog, isRinging)
        TimeRow(callLog)
        DurationRow(callLog)
    }
    HorizontalDivider()
}

@Composable
private fun ContactInfo(
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
            callLog.type == CallType.OUTGOING ->
                stringResource(Res.string.outgoing_call)
            else ->
                stringResource(Res.string.unknown_call)
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
            "${formatShortDate(callLog.timestamp)} ${formatShortTime(callLog.timestamp)}",
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
