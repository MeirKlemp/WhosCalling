package com.klemfner.whoscalling.ui.contacts

import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.Contact

data class ContactsUiState(
    val contacts: List<Contact> = emptyList(),
    val callCounts: Map<String, Int> = emptyMap(),
    val selectedContact: Contact? = null,
    val contactCallLogs: List<CallLog> = emptyList(),
    val currentPane: ContactsPane = ContactsPane.LIST,
    val formState: ContactFormState = ContactFormState(),
    val errorMessage: String? = null,
)

enum class ContactsPane {
    LIST, DETAILS, FORM
}

data class ContactFormState(
    val id: String? = null,
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val isNew: Boolean = true,
)
