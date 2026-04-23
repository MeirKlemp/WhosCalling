package com.klemfner.whoscalling.ui.contacts.contact_details

import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.Spam

data class ContactDetailsUiState(
    val contactCallLogs: List<CallLog> = emptyList(),
    val spams: Map<String, Spam> = emptyMap(),
)
