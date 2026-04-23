package com.klemfner.whoscalling.ui.contacts.contact_details

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Report
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
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.ui.contacts.ContactsViewModel
import com.klemfner.whoscalling.ui.common.components.ContactCallLogItem
import com.klemfner.whoscalling.ui.common.components.FormattedPhoneText
import com.klemfner.whoscalling.ui.common.components.SpamStatusBanner
import com.klemfner.whoscalling.ui.common.utils.LocalIsTouchMode
import com.klemfner.whoscalling.ui.common.utils.TimePeriod
import com.klemfner.whoscalling.ui.common.utils.getTimePeriod
import com.klemfner.whoscalling.util.formatPhoneForDisplay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.back
import whoscalling.composeapp.generated.resources.call_logs_by_number_count
import whoscalling.composeapp.generated.resources.delete_contact
import whoscalling.composeapp.generated.resources.details
import whoscalling.composeapp.generated.resources.edit_contact
import whoscalling.composeapp.generated.resources.long_time_ago
import whoscalling.composeapp.generated.resources.no_call_logs
import whoscalling.composeapp.generated.resources.report_spam
import whoscalling.composeapp.generated.resources.this_month
import whoscalling.composeapp.generated.resources.this_week
import whoscalling.composeapp.generated.resources.today
import whoscalling.composeapp.generated.resources.trust_number
import whoscalling.composeapp.generated.resources.yesterday

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ContactDetails(
    screenVM: ContactsViewModel,
    onCallLogClick: (CallLog) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContactDetailsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenState by screenVM.uiState.collectAsStateWithLifecycle()
    val contact = screenState.selectedContact ?: return
    val defaultCountryIso = screenState.defaultCountryIso
    val isTouchMode = LocalIsTouchMode.current
    val spam = uiState.spams[contact.phoneNumber]
    val isSpam = spam?.isSpam == true

    LaunchedEffect(contact.phoneNumber) {
        viewModel.setSelectedPhone(contact.phoneNumber)
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
                ContactDetailsActions(
                    isTouchMode = isTouchMode,
                    isSpam = isSpam,
                    onReportSpam = { screenVM.requestReportSpam(contact.phoneNumber) },
                    onReportSafe = { screenVM.requestReportSafe(contact.phoneNumber) },
                    onEditClick = screenVM::openEditContact,
                    onDeleteClick = { screenVM.requestDeleteContact(contact) },
                )
            },
        )

        val timePeriodGroups = remember(uiState.contactCallLogs) {
            uiState.contactCallLogs.groupBy { getTimePeriod(it.timestamp) }
                .toSortedMap(compareBy { it.ordinal })
        }
        val formattedPhone = remember(contact.phoneNumber, defaultCountryIso) {
            formatPhoneForDisplay(contact.phoneNumber, defaultCountryIso)
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                SelectionContainer {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
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
                        if (!contact.email.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(contact.email, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                SpamStatusBanner(
                    spam = spam,
                    onReportSafe = { screenVM.requestReportSafe(contact.phoneNumber) },
                )
                HorizontalDivider()
            }

            item {
                Text(
                    stringResource(Res.string.call_logs_by_number_count, uiState.contactCallLogs.size),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }

            if (uiState.contactCallLogs.isEmpty()) {
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
                    ContactCallLogItem(callLog = log, onClick = { onCallLogClick(log) })
                }
            }
        }
    }
}

@Composable
private fun ContactDetailsActions(
    isTouchMode: Boolean,
    isSpam: Boolean,
    onReportSpam: () -> Unit,
    onReportSafe: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
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
        IconButton(onClick = onEditClick) {
            Icon(
                Icons.Default.Edit,
                contentDescription = stringResource(Res.string.edit_contact),
            )
        }
    }
    IconButton(onClick = onDeleteClick) {
        Icon(
            Icons.Default.Delete,
            contentDescription = stringResource(Res.string.delete_contact),
        )
    }
}
