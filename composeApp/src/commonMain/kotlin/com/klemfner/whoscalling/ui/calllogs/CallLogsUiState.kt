package com.klemfner.whoscalling.ui.calllogs

import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.Spam

data class CallLogsUiState(
    val callLogs: List<CallLog> = emptyList(),
    val contacts: Map<String, Contact> = emptyMap(),
    val spamNumbers: Map<String, Spam> = emptyMap(),
    val selectedCallLog: CallLog? = null,
    val selectedNumberCallLogs: List<CallLog> = emptyList(),
    val selectedSpam: Spam? = null,
    val currentPane: CallLogsPane = CallLogsPane.LIST,
    val isRefreshing: Boolean = false,
    val isLoggedIn: Boolean = false,
    val refreshError: Boolean = false,
    val defaultCountryIso: String = "",
    val ringingCallId: String? = null,
    val showReportSpamDialog: Boolean = false,
    val showReportSafeDialog: Boolean = false,
    val reportDialogPhoneNumber: String = "",
    val reportDialogDisplayName: String = "",
)

enum class CallLogsPane {
    LIST, DETAILS
}
