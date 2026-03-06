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
import kotlin.test.assertTrue

class ContactRepositoryImplTest {

    private lateinit var localDataSource: FakeContactLocalDataSource
    private lateinit var repository: ContactRepositoryImpl

    @BeforeTest
    fun setup() {
        localDataSource = FakeContactLocalDataSource()
        repository = createRepository()
    }

    private fun createRepository(
        normalizePhone: (String) -> String = { it }
    ) = ContactRepositoryImpl(localDataSource, normalizePhone = normalizePhone)

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
        repository = createRepository(normalizePhone = { "+1${it.filter { c -> c.isDigit() }}" })

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
        repository = createRepository(normalizePhone = { throw IllegalArgumentException("Invalid") })

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

    @Test
    fun addContact_generatesIdWhenEmpty() = runTest {
        val contact = Contact(name = "Alice", phoneNumber = "+1234567890")

        repository.addContact(contact)

        repository.contacts.test {
            val contacts = awaitItem()
            assertEquals(1, contacts.size)
            assertTrue(contacts[0].id.isNotEmpty())
            assertEquals("Alice", contacts[0].name)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun addContacts_addsMultipleContacts() = runTest {
        val contacts = listOf(
            Contact(name = "Alice", phoneNumber = "+1234567890"),
            Contact(name = "Bob", phoneNumber = "+0987654321"),
        )

        val count = repository.addContacts(contacts)

        assertEquals(2, count)
        repository.contacts.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun addContacts_skipsInvalidAndReturnsCount() = runTest {
        repository = createRepository(normalizePhone = { phone ->
            if (phone == "invalid") throw IllegalArgumentException("Invalid")
            phone
        })

        val contacts = listOf(
            Contact(name = "Alice", phoneNumber = "+1234567890"),
            Contact(name = "Bob", phoneNumber = "invalid"),
            Contact(name = "Charlie", phoneNumber = "+1112223333"),
        )

        val count = repository.addContacts(contacts)

        assertEquals(2, count)
        repository.contacts.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun addContacts_emptyList() = runTest {
        val count = repository.addContacts(emptyList())
        assertEquals(0, count)
    }
}
