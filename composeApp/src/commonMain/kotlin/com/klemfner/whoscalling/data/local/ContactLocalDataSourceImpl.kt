package com.klemfner.whoscalling.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.klemfner.whoscalling.data.local.db.WhosCallingDatabase
import com.klemfner.whoscalling.domain.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ContactLocalDataSourceImpl(
    private val database: WhosCallingDatabase
) : ContactLocalDataSource {

    override fun getContacts(): Flow<List<Contact>> {
        return database.contactEntityQueries
            .getAllContacts()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities ->
                entities.map { entity ->
                    Contact(
                        id = entity.id,
                        name = entity.name,
                        phoneNumber = entity.phoneNumber,
                        email = entity.email
                    )
                }
            }
    }

    override suspend fun saveContact(contact: Contact) {
        withContext(Dispatchers.Default) {
            database.contactEntityQueries.insertContact(
                id = contact.id,
                name = contact.name,
                phoneNumber = contact.phoneNumber,
                email = contact.email
            )
        }
    }

    override suspend fun deleteContact(contactId: String) {
        withContext(Dispatchers.Default) {
            database.contactEntityQueries.deleteContact(contactId)
        }
    }
}
