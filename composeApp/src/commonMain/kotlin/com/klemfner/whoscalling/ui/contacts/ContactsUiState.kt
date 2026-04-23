package com.klemfner.whoscalling.ui.contacts

import com.klemfner.whoscalling.domain.model.Contact

data class ContactsUiState(
    val currentPane: ContactsPane = ContactsPane.LIST,
    val selectedContact: Contact? = null,
    val defaultCountryIso: String = "",
    val formMode: ContactFormMode = ContactFormMode.NEW,
    val newContactPhone: String = "",
    val showDeleteDialog: Boolean = false,
    val deleteDialogContactName: String? = null,
    val pendingDeleteIds: Set<String> = emptySet(),
    val showReportSpamDialog: Boolean = false,
    val showTrustNumberDialog: Boolean = false,
    val reportDialogPhoneNumber: String = "",
)

enum class ContactsPane {
    LIST, DETAILS, FORM
}

enum class ContactFormMode {
    NEW, EDIT
}
