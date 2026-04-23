package com.klemfner.whoscalling.ui.contacts.contacts_list

import app.cash.turbine.test
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.Spam
import com.klemfner.whoscalling.domain.model.SpamReport
import com.klemfner.whoscalling.fake.FakeCallLogRepository
import com.klemfner.whoscalling.fake.FakeContactRepository
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
class ContactsListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var contactRepository: FakeContactRepository
    private lateinit var callLogRepository: FakeCallLogRepository
    private lateinit var spamRepository: FakeSpamRepository
    private lateinit var viewModel: ContactsListViewModel

    private val contact1 = Contact("1", "Alice", "+1234567890", "alice@test.com")
    private val contact2 = Contact("2", "Bob", "+0987654321", null)

    private val callLog1 = CallLog("log1", "+1234567890", CallType.INCOMING, false, 1000L, 120L)
    private val callLog2 = CallLog("log2", "+1234567890", CallType.OUTGOING, false, 2000L, 60L)
    private val callLog3 = CallLog("log3", "+0987654321", CallType.INCOMING, true, 3000L, 0L)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        contactRepository = FakeContactRepository()
        callLogRepository = FakeCallLogRepository()
        spamRepository = FakeSpamRepository()
        viewModel = ContactsListViewModel(contactRepository, callLogRepository, FakeSettingsRepository(), spamRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsEmpty() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(emptyList(), state.contacts)
            assertEquals(emptyMap(), state.callCounts)
            assertEquals(emptyMap(), state.spams)
            assertFalse(state.isDeleteMode)
        }
    }

    @Test
    fun contactsAreSortedByName() = runTest {
        contactRepository.setContacts(listOf(contact2, contact1))

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals(2, state.contacts.size)
            assertEquals("Alice", state.contacts[0].name)
            assertEquals("Bob", state.contacts[1].name)
        }
    }

    @Test
    fun callCountsAreComputedFromCallLogs() = runTest {
        callLogRepository.setCallLogs(listOf(callLog1, callLog2, callLog3))

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals(2, state.callCounts["+1234567890"])
            assertEquals(1, state.callCounts["+0987654321"])
        }
    }

    @Test
    fun spamsAreResolvedByPhoneNumber() = runTest {
        spamRepository.reportAsSpam("+1234567890")

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals(SpamReport.SPAM, state.spams["+1234567890"]?.reportedAs)
        }
    }

    @Test
    fun enterDeleteModeUpdatesState() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.enterDeleteMode()
            val state = awaitItem()
            assertTrue(state.isDeleteMode)
            assertEquals(emptySet(), state.selectedForDeletion)
        }
    }

    @Test
    fun exitDeleteModeClearsState() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.enterDeleteMode()
            awaitItem()

            viewModel.exitDeleteMode()
            val state = awaitItem()
            assertFalse(state.isDeleteMode)
            assertEquals(emptySet(), state.selectedForDeletion)
        }
    }

    @Test
    fun toggleContactSelectionAddsAndRemoves() = runTest {
        contactRepository.setContacts(listOf(contact1, contact2))

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.enterDeleteMode()
            awaitItem()

            viewModel.toggleContactSelection("1")
            var state = awaitItem()
            assertEquals(setOf("1"), state.selectedForDeletion)

            viewModel.toggleContactSelection("2")
            state = awaitItem()
            assertEquals(setOf("1", "2"), state.selectedForDeletion)

            viewModel.toggleContactSelection("1")
            state = awaitItem()
            assertEquals(setOf("2"), state.selectedForDeletion)
        }
    }

    @Test
    fun selectAllContactsSelectsAll() = runTest {
        contactRepository.setContacts(listOf(contact1, contact2))

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.enterDeleteMode()
            awaitItem()

            viewModel.selectAllContacts()
            val state = awaitItem()
            assertEquals(setOf("1", "2"), state.selectedForDeletion)
        }
    }

    @Test
    fun unselectAllContactsClearsSelection() = runTest {
        contactRepository.setContacts(listOf(contact1, contact2))

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.enterDeleteMode()
            awaitItem()

            viewModel.selectAllContacts()
            awaitItem()

            viewModel.unselectAllContacts()
            val state = awaitItem()
            assertEquals(emptySet(), state.selectedForDeletion)
        }
    }
}
