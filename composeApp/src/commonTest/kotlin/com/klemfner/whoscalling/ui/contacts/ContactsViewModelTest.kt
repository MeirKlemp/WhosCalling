package com.klemfner.whoscalling.ui.contacts

import app.cash.turbine.test
import com.klemfner.whoscalling.domain.model.Contact
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var contactRepository: FakeContactRepository
    private lateinit var spamRepository: FakeSpamRepository
    private lateinit var viewModel: ContactsViewModel

    private val contact1 = Contact("1", "Alice", "+1234567890", "alice@test.com")
    private val contact2 = Contact("2", "Bob", "+0987654321", null)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        contactRepository = FakeContactRepository()
        spamRepository = FakeSpamRepository()
        viewModel = ContactsViewModel(contactRepository, FakeSettingsRepository(), spamRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateHasListPaneAndNoSelection() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(ContactsPane.LIST, state.currentPane)
            assertNull(state.selectedContact)
        }
    }

    @Test
    fun selectContactNavigatesToDetails() = runTest {
        viewModel.uiState.test {
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
            assertEquals(ContactFormMode.NEW, state.formMode)
            assertEquals("", state.newContactPhone)
        }
    }

    @Test
    fun openAddContactWithPhonePreFillsNewContactPhone() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.openAddContact("+1234567890")
            val state = awaitItem()
            assertEquals(ContactsPane.FORM, state.currentPane)
            assertEquals(ContactFormMode.NEW, state.formMode)
            assertEquals("+1234567890", state.newContactPhone)
        }
    }

    @Test
    fun openEditContactSetsEditMode() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.selectContact(contact1)
            awaitItem()

            viewModel.openEditContact()
            val state = awaitItem()
            assertEquals(ContactsPane.FORM, state.currentPane)
            assertEquals(ContactFormMode.EDIT, state.formMode)
        }
    }

    @Test
    fun goBackFromDetailsReturnsToList() = runTest {
        viewModel.uiState.test {
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
        viewModel.uiState.test {
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
    fun goBackFromNewFormWithNoContactReturnsToList() = runTest {
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
    fun goBackFromNewFormWithSelectedContactReturnsToDetails() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.selectContact(contact1)
            awaitItem()

            viewModel.openAddContact()
            awaitItem()

            viewModel.goBack()
            val state = awaitItem()
            assertEquals(ContactsPane.DETAILS, state.currentPane)
            assertEquals(contact1, state.selectedContact)
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
    fun onFormSavedNewReturnsToList() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.openAddContact()
            awaitItem()

            viewModel.onFormSavedNew()
            val state = awaitItem()
            assertEquals(ContactsPane.LIST, state.currentPane)
        }
    }

    @Test
    fun onFormSavedEditReturnsToDetailsWithUpdatedContact() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.selectContact(contact1)
            awaitItem()

            viewModel.openEditContact()
            awaitItem()

            val updated = contact1.copy(name = "Alice Updated")
            viewModel.onFormSavedEdit(updated)
            val state = awaitItem()
            assertEquals(ContactsPane.DETAILS, state.currentPane)
            assertEquals(updated, state.selectedContact)
        }
    }

    @Test
    fun requestDeleteContactSetsDialog() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDeleteContact(contact1)
            val state = awaitItem()
            assertTrue(state.showDeleteDialog)
            assertEquals("Alice", state.deleteDialogContactName)
            assertEquals(setOf("1"), state.pendingDeleteIds)
        }
    }

    @Test
    fun requestDeleteSelectedContactsWithSingleContactShowsName() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDeleteSelectedContacts(setOf("1"), listOf(contact1, contact2))
            val state = awaitItem()
            assertTrue(state.showDeleteDialog)
            assertEquals("Alice", state.deleteDialogContactName)
        }
    }

    @Test
    fun requestDeleteSelectedContactsWithMultipleContactsClearsName() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDeleteSelectedContacts(setOf("1", "2"), listOf(contact1, contact2))
            val state = awaitItem()
            assertTrue(state.showDeleteDialog)
            assertNull(state.deleteDialogContactName)
        }
    }

    @Test
    fun requestDeleteSelectedContactsWithEmptySelectionDoesNothing() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDeleteSelectedContacts(emptySet(), listOf(contact1, contact2))
            expectNoEvents()
        }
    }

    @Test
    fun dismissDeleteDialogClearsDialog() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDeleteContact(contact1)
            awaitItem()

            viewModel.dismissDeleteDialog()
            val state = awaitItem()
            assertFalse(state.showDeleteDialog)
            assertNull(state.deleteDialogContactName)
        }
    }

    @Test
    fun confirmDeleteClosesDialogAndDeletesFromRepository() = runTest(testDispatcher) {
        contactRepository.setContacts(listOf(contact1, contact2))

        viewModel.uiState.test {
            awaitItem()

            viewModel.requestDeleteContact(contact1)
            awaitItem()

            viewModel.confirmDelete()
            val state = awaitItem()
            assertFalse(state.showDeleteDialog)
            assertEquals(emptySet(), state.pendingDeleteIds)
        }
    }

    @Test
    fun confirmDeleteFromDetailsNavigatesToList() = runTest(testDispatcher) {
        contactRepository.setContacts(listOf(contact1))

        viewModel.uiState.test {
            awaitItem()

            viewModel.selectContact(contact1)
            awaitItem()

            viewModel.requestDeleteContact(contact1)
            awaitItem()

            viewModel.confirmDelete()
            val state = awaitItem()
            assertEquals(ContactsPane.LIST, state.currentPane)
            assertNull(state.selectedContact)
            assertFalse(state.showDeleteDialog)
        }
    }

    @Test
    fun requestReportSpamSetsDialog() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.requestReportSpam("+1234567890")
            val state = awaitItem()
            assertTrue(state.showReportSpamDialog)
            assertEquals("+1234567890", state.reportDialogPhoneNumber)
        }
    }

    @Test
    fun requestReportSafeSetsDialog() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.requestReportSafe("+1234567890")
            val state = awaitItem()
            assertTrue(state.showTrustNumberDialog)
            assertEquals("+1234567890", state.reportDialogPhoneNumber)
        }
    }

    @Test
    fun confirmReportSpamCallsRepoAndClosesDialog() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem()

            viewModel.requestReportSpam("+1234567890")
            awaitItem()

            viewModel.confirmReportSpam()
            val state = awaitItem()
            assertFalse(state.showReportSpamDialog)
            assertEquals("", state.reportDialogPhoneNumber)
        }
    }

    @Test
    fun confirmReportSafeCallsRepoAndClosesDialog() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem()

            viewModel.requestReportSafe("+1234567890")
            awaitItem()

            viewModel.confirmReportSafe()
            val state = awaitItem()
            assertFalse(state.showTrustNumberDialog)
            assertEquals("", state.reportDialogPhoneNumber)
        }
    }

    @Test
    fun dismissReportDialogClearsState() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.requestReportSpam("+1234567890")
            awaitItem()

            viewModel.dismissReportDialog()
            val state = awaitItem()
            assertFalse(state.showReportSpamDialog)
            assertFalse(state.showTrustNumberDialog)
            assertEquals("", state.reportDialogPhoneNumber)
        }
    }
}
