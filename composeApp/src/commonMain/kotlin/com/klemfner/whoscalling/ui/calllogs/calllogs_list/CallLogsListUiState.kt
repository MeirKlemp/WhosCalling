package com.klemfner.whoscalling.ui.calllogs.calllogs_list

import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.Spam

data class CallLogsListUiState(
    val callLogs: List<CallLog> = emptyList(),
    val contacts: Map<String, Contact> = emptyMap(),
    val spams: Map<String, Spam> = emptyMap(),
    val isRefreshing: Boolean = false,
    val isLoggedIn: Boolean = false,
    val refreshError: Boolean = false,
    val defaultCountryIso: String = "",
    val ringingCallId: String? = null,
)
