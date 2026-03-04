package com.klemfner.whoscalling.ui.contacts

import app.cash.turbine.test
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.fake.FakeCallLogLocalDataSource
import com.klemfner.whoscalling.fake.FakeCallLogRemoteDataSource
import com.klemfner.whoscalling.fake.FakeContactLocalDataSource
import com.klemfner.whoscalling.data.repository.CallLogRepositoryImpl
import com.klemfner.whoscalling.data.repository.ContactRepositoryImpl
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var contactLocalDataSource: FakeContactLocalDataSource
    private lateinit var callLogLocalDataSource: FakeCallLogLocalDataSource
    private lateinit var callLogRemoteDataSource: FakeCallLogRemoteDataSource
    private lateinit var viewModel: ContactsViewModel

    private val contact1 = Contact("1", "Alice", "+1234567890", "alice@test.com")
    private val contact2 = Contact("2", "Bob", "+0987654321", null)

    private val callLog1 = CallLog("log1", "+1234567890", CallType.INCOMING, false, 1000L, 120L)
    private val callLog2 = CallLog("log2", "+1234567890", CallType.OUTGOING, false, 2000L, 60L)
    private val callLog3 = CallLog("log3", "+0987654321", CallType.INCOMING, true, 3000L, 0L)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        contactLocalDataSource = FakeContactLocalDataSource()
        callLogLocalDataSource = FakeCallLogLocalDataSource()
        callLogRemoteDataSource = FakeCallLogRemoteDataSource()

        val contactRepository = ContactRepositoryImpl(
            localDataSource = contactLocalDataSource,
            normalizePhone = { it },
        )
        val callLogRepository = CallLogRepositoryImpl(
            remoteDataSource = callLogRemoteDataSource,
            localDataSource = callLogLocalDataSource,
            normalizePhone = { it },
        )
        viewModel = ContactsViewModel(contactRepository, callLogRepository)
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
            assertEquals(ContactsPane.LIST, state.currentPane)
            assertNull(state.selectedContact)
        }
    }

    @Test
    fun contactsAreSortedByName() = runTest {
        contactLocalDataSource.saveContact(contact2)
        contactLocalDataSource.saveContact(contact1)

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals(2, state.contacts.size)
            assertEquals("Alice", state.contacts[0].name)
            assertEquals("Bob", state.contacts[1].name)
        }
    }

    @Test
    fun selectContactNavigatesToDetails() = runTest {
        contactLocalDataSource.saveContact(contact1)

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.selectContact(contact1)
            val state = awaitItem()
            assertEquals(ContactsPane.DETAILS, state.currentPane)
            assertEquals(contact1, state.selectedContact)
        }
    }

    @Test
    fun openAddContactNavigatesToForm() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.openAddContact()
            val state = awaitItem()
            assertEquals(ContactsPane.FORM, state.currentPane)
            assertEquals(true, state.formState.isNew)
            assertEquals("", state.formState.name)
        }
    }

    @Test
    fun openEditContactPopulatesForm() = runTest {
        contactLocalDataSource.saveContact(contact1)

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.selectContact(contact1)
            awaitItem()

            viewModel.openEditContact()
            val state = awaitItem()
            assertEquals(ContactsPane.FORM, state.currentPane)
            assertEquals(false, state.formState.isNew)
            assertEquals(contact1.name, state.formState.name)
            assertEquals(contact1.phoneNumber, state.formState.phoneNumber)
            assertEquals(contact1.email, state.formState.email)
        }
    }

    @Test
    fun goBackFromDetailsReturnsToList() = runTest {
        contactLocalDataSource.saveContact(contact1)

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.selectContact(contact1)
            awaitItem()

            viewModel.goBack()
            val state = awaitItem()
            assertEquals(ContactsPane.LIST, state.currentPane)
            assertNull(state.selectedContact)
        }
    }

    @Test
    fun goBackFromEditFormReturnsToDetails() = runTest {
        contactLocalDataSource.saveContact(contact1)

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.selectContact(contact1)
            awaitItem()

            viewModel.openEditContact()
            awaitItem()

            viewModel.goBack()
            val state = awaitItem()
            assertEquals(ContactsPane.DETAILS, state.currentPane)
            assertEquals(contact1, state.selectedContact)
        }
    }

    @Test
    fun goBackFromAddFormReturnsToList() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.openAddContact()
            awaitItem()

            viewModel.goBack()
            val state = awaitItem()
            assertEquals(ContactsPane.LIST, state.currentPane)
        }
    }

    @Test
    fun goBackFromAddFormReturnsToDetailsWhenContactSelected() = runTest {
        contactLocalDataSource.saveContact(contact1)

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.selectContact(contact1)
            awaitItem()

            viewModel.openAddContact()
            awaitItem()

            viewModel.goBack()
            val state = awaitItem()
            assertEquals(ContactsPane.DETAILS, state.currentPane)
        }
    }

    @Test
    fun updateFormFieldsUpdatesState() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.openAddContact()
            awaitItem()

            viewModel.updateFormName("Test Name")
            var state = awaitItem()
            assertEquals("Test Name", state.formState.name)

            viewModel.updateFormPhone("+1111111111")
            state = awaitItem()
            assertEquals("+1111111111", state.formState.phoneNumber)

            viewModel.updateFormEmail("test@test.com")
            state = awaitItem()
            assertEquals("test@test.com", state.formState.email)
        }
    }

    @Test
    fun callCountsAreComputedFromCallLogs() = runTest {
        callLogLocalDataSource.saveCallLogs(listOf(callLog1, callLog2, callLog3))

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals(2, state.callCounts["+1234567890"])
            assertEquals(1, state.callCounts["+0987654321"])
        }
    }

    @Test
    fun selectingContactFiltersCallLogs() = runTest {
        contactLocalDataSource.saveContact(contact1)
        callLogLocalDataSource.saveCallLogs(listOf(callLog1, callLog2, callLog3))

        viewModel.uiState.test {
            skipItems(2)
            awaitItem()

            viewModel.selectContact(contact1)
            // selectContact directly updates state (pane/selectedContact),
            // then combine flow re-fires asynchronously with filtered call logs
            awaitItem()
            val state = awaitItem()
            assertEquals(2, state.contactCallLogs.size)
            assertEquals(callLog2, state.contactCallLogs[0])
            assertEquals(callLog1, state.contactCallLogs[1])
        }
    }

    @Test
    fun goBackFromListDoesNothing() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.goBack()
            expectNoEvents()
        }
    }

    @Test
    fun openEditContactWithNoSelectionDoesNothing() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.openEditContact()
            expectNoEvents()
        }
    }
}
