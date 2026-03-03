package com.klemfner.whoscalling.data.repository

import app.cash.turbine.test
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.fake.FakeContactLocalDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContactRepositoryImplTest {

    private lateinit var localDataSource: FakeContactLocalDataSource
    private lateinit var repository: ContactRepositoryImpl

    @BeforeTest
    fun setup() {
        localDataSource = FakeContactLocalDataSource()
        repository = ContactRepositoryImpl(localDataSource)
    }

    @Test
    fun contacts_emptyInitially() = runTest {
        repository.contacts.test {
            assertEquals(emptyList(), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun addContact_addsToLocalDataSource() = runTest {
        val contact = Contact("1", "Alice", "+1234567890", "alice@example.com")

        repository.addContact(contact)

        repository.contacts.test {
            val contacts = awaitItem()
            assertEquals(1, contacts.size)
            assertEquals(contact, contacts[0])
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun addContact_multipleContacts() = runTest {
        val alice = Contact("1", "Alice", "+1234567890", "alice@example.com")
        val bob = Contact("2", "Bob", "+0987654321", null)

        repository.addContact(alice)
        repository.addContact(bob)

        repository.contacts.test {
            val contacts = awaitItem()
            assertEquals(2, contacts.size)
            assertTrue(contacts.contains(alice))
            assertTrue(contacts.contains(bob))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun deleteContact_removesFromLocalDataSource() = runTest {
        val contact = Contact("1", "Alice", "+1234567890", "alice@example.com")
        repository.addContact(contact)

        repository.deleteContact("1")

        repository.contacts.test {
            assertEquals(emptyList(), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun deleteContact_onlyRemovesSpecifiedContact() = runTest {
        val alice = Contact("1", "Alice", "+1234567890")
        val bob = Contact("2", "Bob", "+0987654321")

        repository.addContact(alice)
        repository.addContact(bob)

        repository.deleteContact("1")

        repository.contacts.test {
            val contacts = awaitItem()
            assertEquals(1, contacts.size)
            assertEquals(bob, contacts[0])
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun addContact_updatesExistingContact() = runTest {
        val original = Contact("1", "Alice", "+1234567890", "alice@old.com")
        val updated = Contact("1", "Alice Smith", "+1234567890", "alice@new.com")

        repository.addContact(original)
        repository.addContact(updated)

        repository.contacts.test {
            val contacts = awaitItem()
            assertEquals(1, contacts.size)
            assertEquals(updated, contacts[0])
            cancelAndConsumeRemainingEvents()
        }
    }
}
