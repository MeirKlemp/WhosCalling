package com.klemfner.whoscalling.data.repository

import com.klemfner.whoscalling.data.local.ContactLocalDataSource
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.InvalidPhoneNumberException
import com.klemfner.whoscalling.domain.repository.ContactRepository
import com.klemfner.whoscalling.util.Logger
import com.klemfner.whoscalling.util.maskPhoneNumber
import com.klemfner.whoscalling.util.normalizePhoneNumber
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ContactRepositoryImpl(
    private val localDataSource: ContactLocalDataSource,
    private val normalizePhone: (String) -> String = { normalizePhoneNumber(it) }
) : ContactRepository {

    companion object {
        private const val TAG = "ContactRepository"
    }

    override val contacts: Flow<List<Contact>> = localDataSource.contacts

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun addContact(contact: Contact) {
        val id = contact.id.ifEmpty { Uuid.random().toString() }
        val normalized = try {
            normalizePhone(contact.phoneNumber)
        } catch (e: Exception) {
            Logger.w(TAG, "Invalid phone number: ${maskPhoneNumber(contact.phoneNumber)}", e)
            throw InvalidPhoneNumberException(contact.phoneNumber, e)
        }
        localDataSource.saveContact(contact.copy(id = id, phoneNumber = normalized))
    }

    override suspend fun addContacts(contacts: List<Contact>): Int {
        var imported = 0
        for (contact in contacts) {
            try {
                addContact(contact)
                imported++
            } catch (e: Exception) {
                Logger.w(TAG, "Failed to import contact: ${contact.name}", e)
            }
        }
        return imported
    }

    override suspend fun deleteContact(contactId: String) {
        localDataSource.deleteContact(contactId)
    }
}
