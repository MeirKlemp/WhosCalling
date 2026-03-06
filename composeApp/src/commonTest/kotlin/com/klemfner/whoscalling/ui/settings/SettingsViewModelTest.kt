package com.klemfner.whoscalling.ui.settings

import app.cash.turbine.test
import com.klemfner.whoscalling.data.repository.ContactRepositoryImpl
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.fake.FakeContactLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var contactLocalDataSource: FakeContactLocalDataSource
    private lateinit var viewModel: SettingsViewModel

    private val contact1 = Contact("1", "Alice", "+1234567890", "alice@test.com")
    private val contact2 = Contact("2", "Bob", "+0987654321", null)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        contactLocalDataSource = FakeContactLocalDataSource()
        val contactRepository = ContactRepositoryImpl(
            localDataSource = contactLocalDataSource,
            normalizePhone = { it },
        )
        viewModel = SettingsViewModel(contactRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun exportContacts_emptyList() = runTest {
        val result = viewModel.exportContacts()
        assertEquals(0, result.count)
        val contacts = Json.decodeFromString<List<Contact>>(result.json)
        assertEquals(emptyList(), contacts)
    }

    @Test
    fun exportContacts_withContacts() = runTest {
        contactLocalDataSource.saveContact(contact1)
        contactLocalDataSource.saveContact(contact2)

        val result = viewModel.exportContacts()
        assertEquals(2, result.count)
        assertTrue(result.json.contains("\"Alice\""))
        assertTrue(result.json.contains("\"+1234567890\""))
        assertTrue(result.json.contains("\"alice@test.com\""))
        assertTrue(result.json.contains("\"Bob\""))
        assertTrue(result.json.contains("\"+0987654321\""))
        assertFalse(result.json.contains("\"id\""))
    }

    @Test
    fun importContacts_validJson() = runTest {
        val json = """
            [
                {"name":"Alice","phoneNumber":"+1234567890","email":"alice@test.com"},
                {"name":"Bob","phoneNumber":"+0987654321"}
            ]
        """.trimIndent()

        viewModel.importContacts(json)

        viewModel.importResult.test {
            skipItems(1)
            val result = awaitItem()
            assertTrue(result is ImportResult.Success)
            assertEquals(2, (result as ImportResult.Success).count)
        }
    }

    @Test
    fun importContacts_jsonWithIdIsIgnored() = runTest {
        val json = """
            [
                {"id":"old-id","name":"Alice","phoneNumber":"+1234567890"}
            ]
        """.trimIndent()

        viewModel.importContacts(json)

        viewModel.importResult.test {
            skipItems(1)
            val result = awaitItem()
            assertTrue(result is ImportResult.Success)
            assertEquals(1, (result as ImportResult.Success).count)
        }
    }

    @Test
    fun importContacts_invalidJson() = runTest {
        viewModel.importContacts("invalid json")

        viewModel.importResult.test {
            skipItems(1)
            val result = awaitItem()
            assertTrue(result is ImportResult.Error)
        }
    }

    @Test
    fun importContacts_partiallyValid() = runTest {
        val contactRepository = ContactRepositoryImpl(
            localDataSource = contactLocalDataSource,
            normalizePhone = { phone ->
                if (phone == "invalid") throw IllegalArgumentException("Invalid")
                phone
            },
        )
        viewModel = SettingsViewModel(contactRepository)

        val json = """
            [
                {"name":"Alice","phoneNumber":"+1234567890"},
                {"name":"Bob","phoneNumber":"invalid"}
            ]
        """.trimIndent()

        viewModel.importContacts(json)

        viewModel.importResult.test {
            skipItems(1)
            val result = awaitItem()
            assertTrue(result is ImportResult.Success)
            assertEquals(1, (result as ImportResult.Success).count)
        }
    }

    @Test
    fun clearImportResult_resetsToNull() = runTest {
        viewModel.importContacts("[]")

        viewModel.importResult.test {
            skipItems(1)
            awaitItem()

            viewModel.clearImportResult()
            val result = awaitItem()
            assertEquals(null, result)
        }
    }

    @Test
    fun exportThenImport_roundTrip() = runTest {
        contactLocalDataSource.saveContact(contact1)
        contactLocalDataSource.saveContact(contact2)

        val exportData = viewModel.exportContacts()

        // Clear and re-import
        contactLocalDataSource.deleteContact("1")
        contactLocalDataSource.deleteContact("2")

        viewModel.importContacts(exportData.json)

        viewModel.importResult.test {
            skipItems(1)
            val result = awaitItem()
            assertTrue(result is ImportResult.Success)
            assertEquals(2, (result as ImportResult.Success).count)
        }
    }
}
