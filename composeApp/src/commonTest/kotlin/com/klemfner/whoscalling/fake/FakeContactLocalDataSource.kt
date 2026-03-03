package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.data.local.ContactLocalDataSource
import com.klemfner.whoscalling.domain.model.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeContactLocalDataSource : ContactLocalDataSource {
    private val contacts = MutableStateFlow<List<Contact>>(emptyList())

    override fun getContacts(): Flow<List<Contact>> = contacts

    override suspend fun saveContact(contact: Contact) {
        contacts.update { current ->
            current.filterNot { it.id == contact.id } + contact
        }
    }

    override suspend fun deleteContact(contactId: String) {
        contacts.update { current -> current.filterNot { it.id == contactId } }
    }
}
