package com.klemfner.whoscalling.domain.repository

import com.klemfner.whoscalling.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getContacts(): Flow<List<Contact>>
    suspend fun addContact(contact: Contact)
    suspend fun deleteContact(contactId: String)
}
