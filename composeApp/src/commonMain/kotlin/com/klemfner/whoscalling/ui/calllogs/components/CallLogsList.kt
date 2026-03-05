package com.klemfner.whoscalling.ui.calllogs.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.ui.common.utils.LocalIsTouchMode
import com.klemfner.whoscalling.ui.common.utils.TimePeriod
import com.klemfner.whoscalling.ui.common.utils.getTimePeriod
import org.jetbrains.compose.resources.stringResource
import whoscalling.composeapp.generated.resources.Res
import whoscalling.composeapp.generated.resources.login
import whoscalling.composeapp.generated.resources.long_time_ago
import whoscalling.composeapp.generated.resources.no_call_logs
import whoscalling.composeapp.generated.resources.not_logged_in_warning
import whoscalling.composeapp.generated.resources.refresh
import whoscalling.composeapp.generated.resources.this_month
import whoscalling.composeapp.generated.resources.this_week
import whoscalling.composeapp.generated.resources.today
import whoscalling.composeapp.generated.resources.yesterday

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CallLogsList(
    callLogs: List<CallLog>,
    contacts: Map<String, Contact>,
    selectedCallLogId: String?,
    isRefreshing: Boolean,
    isLoggedIn: Boolean,
    onCallLogClick: (CallLog) -> Unit,
    onRefresh: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isTouchMode = LocalIsTouchMode.current
    Scaffold(
        modifier = modifier,
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues).fillMaxSize()) {
            if (!isLoggedIn) {
                NotLoggedInBanner(onLoginClick)
            }

            val content: @Composable () -> Unit = {
                val timePeriodGroups = remember(callLogs) {
                    callLogs.groupBy { getTimePeriod(it.timestamp) }
                        .toSortedMap(compareBy { it.ordinal })
                }

                LazyColumn(Modifier.fillMaxSize()) {
                    if (!isTouchMode) {
                        item(key = "refresh_button") {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                FilledTonalButton(onClick = onRefresh) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Text(stringResource(Res.string.refresh))
                                }
                            }
                        }
                    }

                    if (callLogs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    stringResource(Res.string.no_call_logs),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
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
                            CallLogListItem(
                                callLog = log,
                                contact = contacts[log.phoneNumber],
                                isSelected = log.id == selectedCallLogId,
                                onClick = { onCallLogClick(log) },
                            )
                        }
                    }
                }
            }

            if (isTouchMode) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) {
                    content()
                }
            } else {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun NotLoggedInBanner(onLoginClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.not_logged_in_warning),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onLoginClick) {
                Text(stringResource(Res.string.login))
            }
        }
    }
}
