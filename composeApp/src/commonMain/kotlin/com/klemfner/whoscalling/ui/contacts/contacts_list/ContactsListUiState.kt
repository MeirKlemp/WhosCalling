package com.klemfner.whoscalling.ui.contacts.contacts_list

import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.Spam

data class ContactsListUiState(
    val contacts: List<Contact> = emptyList(),
    val callCounts: Map<String, Int> = emptyMap(),
    val spams: Map<String, Spam> = emptyMap(),
    val defaultCountryIso: String = "",
    val isDeleteMode: Boolean = false,
    val selectedForDeletion: Set<String> = emptySet(),
)
