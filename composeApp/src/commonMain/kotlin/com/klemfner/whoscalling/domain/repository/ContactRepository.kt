package com.klemfner.whoscalling.domain.repository

import com.klemfner.whoscalling.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    val contacts: Flow<List<Contact>>
    suspend fun addContact(contact: Contact)
    suspend fun addContacts(contacts: List<Contact>): Int
    suspend fun deleteContact(contactId: String)
}
