package com.klemfner.whoscalling.ui.settings

import app.cash.turbine.test
import com.klemfner.whoscalling.data.repository.ContactRepositoryImpl
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.ThemeMode
import com.klemfner.whoscalling.domain.model.UserPreferences
import com.klemfner.whoscalling.fake.FakeContactLocalDataSource
import com.klemfner.whoscalling.fake.FakeSettingsRepository
import com.klemfner.whoscalling.fake.FakeSpamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var spamRepository: FakeSpamRepository
    private lateinit var viewModel: SettingsViewModel

    private val contact1 = Contact("1", "Alice", "+1234567890", "alice@test.com")
    private val contact2 = Contact("2", "Bob", "+0987654321", null)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        contactLocalDataSource = FakeContactLocalDataSource()
        settingsRepository = FakeSettingsRepository(
            UserPreferences(countryIso = "US", touchMode = true, refreshRateSeconds = 5),
        )
        spamRepository = FakeSpamRepository()
        val contactRepository = ContactRepositoryImpl(
            localDataSource = contactLocalDataSource,
            normalizePhone = { it },
        )
        viewModel = SettingsViewModel(contactRepository, settingsRepository, spamRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun exportContacts_emptyList() = runTest(testDispatcher) {
        val result = viewModel.exportContacts()
        assertEquals(0, result.contactCount)
        assertEquals(0, result.spamCount)
    }

    @Test
    fun exportContacts_withContacts() = runTest(testDispatcher) {
        contactLocalDataSource.saveContact(contact1)
        contactLocalDataSource.saveContact(contact2)

        val result = viewModel.exportContacts()
        assertEquals(2, result.contactCount)
        assertTrue(result.json.contains("\"Alice\""))
        assertTrue(result.json.contains("\"+1234567890\""))
        assertTrue(result.json.contains("\"alice@test.com\""))
        assertTrue(result.json.contains("\"Bob\""))
        assertTrue(result.json.contains("\"+0987654321\""))
    }

    @Test
    fun importContacts_validJson() = runTest(testDispatcher) {
        val json = """
            [
                {"name":"Alice","phoneNumber":"+1234567890","email":"alice@test.com"},
                {"name":"Bob","phoneNumber":"+0987654321"}
            ]
        """.trimIndent()

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.importContacts(json)

            var state = awaitItem()
            while (state.importResult == null) {
                state = awaitItem()
            }
            assertTrue(state.importResult is ImportResult.Success)
            assertEquals(2, state.importResult.count)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun importContacts_jsonWithIdIsIgnored() = runTest(testDispatcher) {
        val json = """
            [
                {"id":"old-id","name":"Alice","phoneNumber":"+1234567890"}
            ]
        """.trimIndent()

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.importContacts(json)

            var state = awaitItem()
            while (state.importResult == null) {
                state = awaitItem()
            }
            assertTrue(state.importResult is ImportResult.Success)
            assertEquals(1, state.importResult.count)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun importContacts_invalidJson() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.importContacts("invalid json")

            val state = awaitItem()
            assertTrue(state.importResult is ImportResult.Error)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun importContacts_partiallyValid() = runTest(testDispatcher) {
        val contactRepository = ContactRepositoryImpl(
            localDataSource = contactLocalDataSource,
            normalizePhone = { phone ->
                if (phone == "invalid") throw IllegalArgumentException("Invalid")
                phone
            },
        )
        viewModel = SettingsViewModel(contactRepository, settingsRepository, spamRepository)

        val json = """
            [
                {"name":"Alice","phoneNumber":"+1234567890"},
                {"name":"Bob","phoneNumber":"invalid"}
            ]
        """.trimIndent()

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.importContacts(json)

            var state = awaitItem()
            while (state.importResult == null) {
                state = awaitItem()
            }
            assertTrue(state.importResult is ImportResult.Success)
            assertEquals(1, state.importResult.count)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun clearImportResult_resetsToNull() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.importContacts("[]")

            var state = awaitItem()
            while (state.importResult == null) {
                state = awaitItem()
            }
            assertTrue(state.importResult is ImportResult.Success)

            viewModel.clearImportResult()
            val stateCleared = awaitItem()
            assertEquals(null, stateCleared.importResult)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun exportThenImport_roundTrip() = runTest(testDispatcher) {
        contactLocalDataSource.saveContact(contact1)
        contactLocalDataSource.saveContact(contact2)

        val exportData = viewModel.exportContacts()

        contactLocalDataSource.deleteContact("1")
        contactLocalDataSource.deleteContact("2")

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.importContacts(exportData.json)

            var state = awaitItem()
            while (state.importResult == null) {
                state = awaitItem()
            }
            assertTrue(state.importResult is ImportResult.Success)
            assertEquals(2, state.importResult.count)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun contactCount_initiallyZero() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals(0, awaitItem().contactCount)
        }
    }

    @Test
    fun contactCount_updatesWhenContactsAdded() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals(0, awaitItem().contactCount)

            contactLocalDataSource.saveContact(contact1)
            assertEquals(1, awaitItem().contactCount)

            contactLocalDataSource.saveContact(contact2)
            assertEquals(2, awaitItem().contactCount)
        }
    }

    @Test
    fun contactCount_updatesWhenContactsRemoved() = runTest(testDispatcher) {
        contactLocalDataSource.saveContact(contact1)
        contactLocalDataSource.saveContact(contact2)

        viewModel.uiState.test {
            assertEquals(0, awaitItem().contactCount) // stateIn initial value

            assertEquals(2, awaitItem().contactCount)

            contactLocalDataSource.deleteContact("1")
            assertEquals(1, awaitItem().contactCount)

            contactLocalDataSource.deleteContact("2")
            assertEquals(0, awaitItem().contactCount)
        }
    }

    @Test
    fun countryIso_initialValue() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals("US", awaitItem().countryIso)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setCountryIso_updatesUiState() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals("US", awaitItem().countryIso)

            viewModel.setCountryIso("IL")
            assertEquals("IL", awaitItem().countryIso)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun resetCountryIsoToDefault_restoresDefault() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals("US", awaitItem().countryIso)

            viewModel.setCountryIso("IL")
            assertEquals("IL", awaitItem().countryIso)

            viewModel.resetToDefault()
            assertEquals("US", awaitItem().countryIso)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun routerIp_initialValue() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals("", awaitItem().routerIp)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setRouterIp_updatesUiState() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals("", awaitItem().routerIp)

            viewModel.setRouterIp("192.168.1.1")
            assertEquals("192.168.1.1", awaitItem().routerIp)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun resetRouterIpToDefault_restoresDefault() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals("", awaitItem().routerIp)

            viewModel.setRouterIp("10.0.0.1")
            assertEquals("10.0.0.1", awaitItem().routerIp)

            viewModel.resetToDefault()
            assertEquals("", awaitItem().routerIp)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun refreshRateSeconds_initialValue() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals(5L, awaitItem().refreshRateSeconds)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setRefreshRateSeconds_updatesUiState() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals(5L, awaitItem().refreshRateSeconds)

            viewModel.setRefreshRateSeconds(30L)
            assertEquals(30L, awaitItem().refreshRateSeconds)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setRefreshRateSeconds_neverDisablesRefresh() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals(5L, awaitItem().refreshRateSeconds)

            viewModel.setRefreshRateSeconds(0L)
            assertEquals(0L, awaitItem().refreshRateSeconds)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun resetRefreshRateToDefault_restoresDefault() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals(5L, awaitItem().refreshRateSeconds)

            viewModel.setRefreshRateSeconds(60L)
            assertEquals(60L, awaitItem().refreshRateSeconds)

            viewModel.resetToDefault()
            assertEquals(5L, awaitItem().refreshRateSeconds)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun themeMode_initialValue() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem().themeMode)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setThemeMode_updatesUiState() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem().themeMode)

            viewModel.setThemeMode(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, awaitItem().themeMode)

            viewModel.setThemeMode(ThemeMode.LIGHT)
            assertEquals(ThemeMode.LIGHT, awaitItem().themeMode)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun resetThemeModeToDefault_restoresDefault() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem().themeMode)

            viewModel.setThemeMode(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, awaitItem().themeMode)

            viewModel.resetToDefault()
            assertEquals(ThemeMode.SYSTEM, awaitItem().themeMode)

            cancelAndConsumeRemainingEvents()
        }
    }
}
