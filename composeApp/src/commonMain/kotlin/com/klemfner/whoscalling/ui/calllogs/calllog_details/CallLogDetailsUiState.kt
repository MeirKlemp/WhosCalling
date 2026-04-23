package com.klemfner.whoscalling.ui.calllogs.calllog_details

import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.Spam

data class CallLogDetailsUiState(
    val selectedNumberCallLogs: List<CallLog> = emptyList(),
    val spams: Map<String, Spam> = emptyMap(),
)
