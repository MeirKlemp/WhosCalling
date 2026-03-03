package com.klemfner.whoscalling.data.repository

import com.klemfner.whoscalling.data.local.ContactLocalDataSource
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow

class ContactRepositoryImpl(
    private val localDataSource: ContactLocalDataSource
) : ContactRepository {

    override fun getContacts(): Flow<List<Contact>> {
        return localDataSource.getContacts()
    }

    override suspend fun addContact(contact: Contact) {
        localDataSource.saveContact(contact)
    }

    override suspend fun deleteContact(contactId: String) {
        localDataSource.deleteContact(contactId)
    }
}
