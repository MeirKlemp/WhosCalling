package com.klemfner.whoscalling.ui.contacts

import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.Spam

data class ContactsUiState(
    val contacts: List<Contact> = emptyList(),
    val callCounts: Map<String, Int> = emptyMap(),
    val spamNumbers: Map<String, Spam> = emptyMap(),
    val selectedContact: Contact? = null,
    val contactCallLogs: List<CallLog> = emptyList(),
    val selectedSpam: Spam? = null,
    val currentPane: ContactsPane = ContactsPane.LIST,
    val formState: ContactFormState = ContactFormState(),
    val defaultCountryIso: String = "",
    val isDeleteMode: Boolean = false,
    val selectedForDeletion: Set<String> = emptySet(),
    val showDeleteDialog: Boolean = false,
    val deleteDialogContactName: String? = null,
    val error: ContactsError? = null,
    val showReportSpamDialog: Boolean = false,
    val showTrustNumberDialog: Boolean = false,
    val reportDialogPhoneNumber: String = "",
    val reportDialogDisplayName: String = "",
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
    val selectedCountryIso: String = "",
)
