package com.klemfner.whoscalling.data.repository

import com.klemfner.whoscalling.data.local.ContactLocalDataSource
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.InvalidPhoneNumberException
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.util.normalizePhoneNumber
import kotlinx.coroutines.flow.Flow

class ContactRepositoryImpl(
    private val localDataSource: ContactLocalDataSource,
    private val normalizePhone: (String) -> String = ::normalizePhoneNumber
) : ContactRepository {

    override val contacts: Flow<List<Contact>> = localDataSource.contacts

    override suspend fun addContact(contact: Contact) {
        val normalized = try {
            normalizePhone(contact.phoneNumber)
        } catch (e: Exception) {
            throw InvalidPhoneNumberException(contact.phoneNumber)
        }
        localDataSource.saveContact(contact.copy(phoneNumber = normalized))
    }

    override suspend fun deleteContact(contactId: String) {
        localDataSource.deleteContact(contactId)
    }
}
