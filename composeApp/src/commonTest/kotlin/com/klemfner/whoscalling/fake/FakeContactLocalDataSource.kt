package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.data.local.ContactLocalDataSource
import com.klemfner.whoscalling.domain.model.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeContactLocalDataSource : ContactLocalDataSource {
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    override val contacts: Flow<List<Contact>> = _contacts

    override suspend fun saveContact(contact: Contact) {
        _contacts.update { current ->
            current.filterNot { it.id == contact.id } + contact
        }
    }

    override suspend fun deleteContact(contactId: String) {
        _contacts.update { current -> current.filterNot { it.id == contactId } }
    }
}
