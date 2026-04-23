package com.klemfner.whoscalling.ui.contacts.contact_form

import app.cash.turbine.test
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.fake.FakeContactRepository
import com.klemfner.whoscalling.fake.FakeSettingsRepository
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ContactFormViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var contactRepository: FakeContactRepository
    private lateinit var viewModel: ContactFormViewModel

    private val contact1 = Contact("1", "Alice", "+12345678901", "alice@test.com")

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        contactRepository = FakeContactRepository()
        viewModel = ContactFormViewModel(contactRepository, FakeSettingsRepository())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateHasEmptyForm() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.formState.isNew)
            assertEquals("", state.formState.name)
            assertEquals("", state.formState.phoneNumber)
            assertEquals("", state.formState.email)
            assertNull(state.error)
        }
    }

    @Test
    fun initForNewResetsFormToInitialState() = runTest {
        viewModel.uiState.test {
            awaitItem()

            // Dirty the form first
            viewModel.updateFormName("Alice")
            awaitItem()
            viewModel.updateFormPhone("+1111111111")
            awaitItem()

            // Now reset via initForNew
            viewModel.initForNew()
            val state = awaitItem()
            assertTrue(state.formState.isNew)
            assertEquals("", state.formState.name)
            assertEquals("", state.formState.phoneNumber)
            assertNull(state.error)
        }
    }

    @Test
    fun initForNewWithPhonePreFillsPhoneNumber() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.initForNew(phoneNumber = "+12345678901", defaultCountryIso = "US")
            val state = awaitItem()
            assertTrue(state.formState.isNew)
            // phone number is reformatted to national number for display — just check non-empty
            assertTrue(state.formState.phoneNumber.isNotEmpty())
        }
    }

    @Test
    fun initForEditPopulatesFormFromContact() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.initForEdit(contact1)
            val state = awaitItem()
            assertFalse(state.formState.isNew)
            assertEquals(contact1.id, state.formState.id)
            assertEquals(contact1.name, state.formState.name)
            assertEquals(contact1.email, state.formState.email)
            assertNull(state.error)
        }
    }

    @Test
    fun updateFormNameUpdatesState() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.updateFormName("Test Name")
            val state = awaitItem()
            assertEquals("Test Name", state.formState.name)
        }
    }

    @Test
    fun updateFormPhoneUpdatesState() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.updateFormPhone("+1111111111")
            val state = awaitItem()
            assertEquals("+1111111111", state.formState.phoneNumber)
        }
    }

    @Test
    fun updateFormEmailUpdatesState() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.updateFormEmail("test@test.com")
            val state = awaitItem()
            assertEquals("test@test.com", state.formState.email)
        }
    }

    @Test
    fun updateFormCountryIsoUpdatesState() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.updateFormCountryIso("DE")
            val state = awaitItem()
            assertEquals("DE", state.formState.selectedCountryIso)
        }
    }

    @Test
    fun clearErrorClearsErrorState() = runTest {
        viewModel.uiState.test {
            awaitItem()

            // Trigger an error by saving with an invalid phone
            viewModel.updateFormName("Alice")
            awaitItem()
            viewModel.updateFormPhone("not-a-phone")
            awaitItem()
            viewModel.saveContact()
            testDispatcher.scheduler.advanceUntilIdle()
            val errorState = awaitItem()
            assertNotNull(errorState.error)

            viewModel.clearError()
            val clearedState = awaitItem()
            assertNull(clearedState.error)
        }
    }

    @Test
    fun saveNewContactEmitsSavedNewEvent() = runTest(testDispatcher) {
        viewModel.saveEvent.test {
            viewModel.updateFormName("Alice")
            viewModel.updateFormPhone("+12125551234")
            viewModel.updateFormCountryIso("US")
            viewModel.saveContact()
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ContactFormSaveEvent.SavedNew)
        }
    }

    @Test
    fun saveEditContactEmitsSavedEditEvent() = runTest(testDispatcher) {
        viewModel.initForEdit(contact1)

        viewModel.saveEvent.test {
            viewModel.updateFormName("Alice Updated")
            viewModel.updateFormPhone("+12125551234")
            viewModel.updateFormCountryIso("US")
            viewModel.saveContact()
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ContactFormSaveEvent.SavedEdit)
        }
    }

    @Test
    fun saveContactWithInvalidPhoneSetsError() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem()

            viewModel.updateFormName("Alice")
            awaitItem()
            viewModel.updateFormPhone("not-a-real-phone-$$$$")
            awaitItem()
            viewModel.saveContact()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertNotNull(state.error)
        }
    }
}
