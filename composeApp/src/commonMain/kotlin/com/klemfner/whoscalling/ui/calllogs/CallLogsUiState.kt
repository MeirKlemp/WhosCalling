package com.klemfner.whoscalling.ui.calllogs

import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.Contact

data class CallLogsUiState(
    val callLogs: List<CallLog> = emptyList(),
    val contacts: Map<String, Contact> = emptyMap(),
    val selectedCallLog: CallLog? = null,
    val selectedNumberCallLogs: List<CallLog> = emptyList(),
    val currentPane: CallLogsPane = CallLogsPane.LIST,
    val isRefreshing: Boolean = false,
)

enum class CallLogsPane {
    LIST, DETAILS
}
