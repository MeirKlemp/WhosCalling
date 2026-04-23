package com.klemfner.whoscalling.ui.contacts.contact_form

import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.ui.contacts.ContactsError

data class ContactFormUiState(
    val formState: ContactFormState = ContactFormState(),
    val error: ContactsError.FormError? = null,
)

data class ContactFormState(
    val id: String? = null,
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val isNew: Boolean = true,
    val selectedCountryIso: String = "",
)

sealed interface ContactFormSaveEvent {
    data object SavedNew : ContactFormSaveEvent
    data class SavedEdit(val contact: Contact) : ContactFormSaveEvent
}
