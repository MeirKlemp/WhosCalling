package com.klemfner.whoscalling.data.repository

import app.cash.turbine.test
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.InvalidPhoneNumberException
import com.klemfner.whoscalling.fake.FakeContactLocalDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ContactRepositoryImplTest {

    private lateinit var localDataSource: FakeContactLocalDataSource
    private lateinit var repository: ContactRepositoryImpl
    private var fakeNormalizer: (String) -> String = { "+1${it.filter { c -> c.isDigit() }}" }

    @BeforeTest
    fun setup() {
        fakeNormalizer = { "+1${it.filter { c -> c.isDigit() }}" }
        localDataSource = FakeContactLocalDataSource()
        repository = ContactRepositoryImpl(localDataSource, normalizePhone = fakeNormalizer)
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
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun addContact_normalizesPhoneNumber() = runTest {
        fakeNormalizer = { "+1${it.filter { c -> c.isDigit() }}" }
        repository = ContactRepositoryImpl(localDataSource, normalizePhone = fakeNormalizer)

        val contact = Contact("1", "Alice", "2345678901", "alice@example.com")

        repository.addContact(contact)

        repository.contacts.test {
            val contacts = awaitItem()
            assertEquals("+12345678901", contacts[0].phoneNumber)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun addContact_throwsInvalidPhoneNumberExceptionOnError() = runTest {
        fakeNormalizer = { throw IllegalArgumentException("Invalid") }
        repository = ContactRepositoryImpl(localDataSource, normalizePhone = fakeNormalizer)

        val contact = Contact("1", "Alice", "invalid", "alice@example.com")

        assertFailsWith<InvalidPhoneNumberException> {
            repository.addContact(contact)
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
            assertEquals("Alice Smith", contacts[0].name)
            cancelAndConsumeRemainingEvents()
        }
    }
}
