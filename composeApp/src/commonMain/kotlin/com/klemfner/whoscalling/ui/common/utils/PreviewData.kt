package com.klemfner.whoscalling.ui.common.utils

import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.model.Contact

val previewContacts = listOf(
    Contact("1", "Alice Smith", "+1234567890", "alice@example.com"),
    Contact("2", "Bob Jones", "+0987654321", null),
    Contact("3", "Charlie Brown", "+1122334455", "charlie@example.com"),
)

val previewCallLogs = listOf(
    CallLog("1", "+1234567890", CallType.INCOMING, false, 1709300000000L, 120L),
    CallLog("2", "+1234567890", CallType.OUTGOING, false, 1709290000000L, 300L),
    CallLog("3", "+1234567890", CallType.INCOMING, true, 1709280000000L, 0L),
)

val previewCallCounts = mapOf(
    "+1234567890" to 3,
    "+0987654321" to 1,
)
