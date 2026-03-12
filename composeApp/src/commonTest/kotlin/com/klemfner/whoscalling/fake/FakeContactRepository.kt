package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeContactRepository(
    initialContacts: List<Contact> = emptyList()
) : ContactRepository {

    private val _contacts = MutableStateFlow(initialContacts)
    override val contacts: Flow<List<Contact>> = _contacts.asStateFlow()

    override suspend fun addContact(contact: Contact) {
        _contacts.value += contact
    }

    override suspend fun addContacts(contacts: List<Contact>): Int {
        _contacts.value += contacts
        return contacts.size
    }

    override suspend fun deleteContact(contactId: String) {
        _contacts.value = _contacts.value.filterNot { it.id == contactId }
    }

    // For testing: Clear all contacts
    fun setContacts(contacts: List<Contact>) {
        _contacts.value = contacts
    }

    fun clear() {
        _contacts.value = emptyList()
    }
}
