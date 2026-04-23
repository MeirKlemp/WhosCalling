package com.klemfner.whoscalling.ui.calllogs

import com.klemfner.whoscalling.domain.model.CallLog

data class CallLogsUiState(
    val currentPane: CallLogsPane = CallLogsPane.LIST,
    val selectedCallLog: CallLog? = null,
    val showReportSpamDialog: Boolean = false,
    val showTrustNumberDialog: Boolean = false,
    val reportDialogPhoneNumber: String = "",
)

enum class CallLogsPane {
    LIST, DETAILS
}
