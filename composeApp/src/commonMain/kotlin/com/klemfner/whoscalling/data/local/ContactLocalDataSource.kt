package com.klemfner.whoscalling.data.local

import com.klemfner.whoscalling.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactLocalDataSource {
    fun getContacts(): Flow<List<Contact>>
    suspend fun saveContact(contact: Contact)
    suspend fun deleteContact(contactId: String)
}
