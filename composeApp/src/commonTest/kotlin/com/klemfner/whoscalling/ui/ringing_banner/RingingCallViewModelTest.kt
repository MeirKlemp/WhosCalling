package com.klemfner.whoscalling.ui.ringing_banner

import app.cash.turbine.test
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.domain.model.UserPreferences
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RingingCallViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var callLogRepository: FakeCallLogRepository
    private lateinit var contactRepository: FakeContactRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var spamRepository: FakeSpamRepository
    private lateinit var viewModel: RingingCallViewModel

    private val contact1 = Contact(id = "1", name = "Alice", phoneNumber = "+1234567890", email = "alice@test.com")
    private val callLog1 = CallLog(id = "log1", phoneNumber = "+1234567890", type = CallType.INCOMING, missed = false, timestamp = 1000L, duration = 120L)
    private val callLog2 = CallLog(id = "log2", phoneNumber = "+0987654321", type = CallType.INCOMING, missed = false, timestamp = 2000L, duration = 23L)
    private val callLog1Copy = CallLog(id = "log1", phoneNumber = "+1234567890", type = CallType.INCOMING, missed = false, timestamp = 1000L, duration = 121L) // Different duration

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        callLogRepository = FakeCallLogRepository()
        contactRepository = FakeContactRepository()
        settingsRepository = FakeSettingsRepository(UserPreferences(countryIso = "US"))
        spamRepository = FakeSpamRepository()
        viewModel = RingingCallViewModel(callLogRepository, contactRepository, settingsRepository, spamRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasDefaults() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertNull(initial.ringingCall)
            assertNull(initial.contact)
            assertEquals("US", initial.defaultCountryIso)
            assertFalse(initial.isDismissed)
            assertFalse(initial.showBanner)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun updatesDefaultCountryIso_whenSettingsChanges() = runTest(testDispatcher) {
        viewModel.uiState.test {
            // Initial "US"
            assertEquals("US", awaitItem().defaultCountryIso)
            settingsRepository.setCountryIso("IL")
            assertEquals("IL", awaitItem().defaultCountryIso)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun ringingCallUiState_updatesWithRingingCall() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // initial
            callLogRepository.setRingingCall(callLog1)
            val state = awaitItem()
            assertEquals(callLog1, state.ringingCall)
            assertNull(state.contact)
            assertFalse(state.isDismissed)
            assertTrue(state.showBanner)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun hidesBanner_whenDismissed() = runTest(testDispatcher) {
        callLogRepository.setRingingCall(callLog1)
        viewModel.uiState.test {
            awaitItem() // initial
            awaitItem() // call is ringing

            viewModel.dismiss()
            val state = awaitItem()
            assertTrue(state.isDismissed)
            assertFalse(state.showBanner)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun showBannerResets_whenRingingCallChanges() = runTest(testDispatcher) {
        // Start with first call
        callLogRepository.setRingingCall(callLog1)
        viewModel.uiState.test {
            awaitItem() // initial
            awaitItem() // callLog1, not dismissed

            viewModel.dismiss()
            var state = awaitItem()
            assertTrue(state.isDismissed)
            assertFalse(state.showBanner)

            // Now if ringing call changes, isDismissed reset for new call
            callLogRepository.setRingingCall(callLog2)
            state = awaitItem()
            assertEquals(callLog2, state.ringingCall)
            assertFalse(state.isDismissed)
            assertTrue(state.showBanner)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun bannerIsHiddenWhenRingingCallIsNull() = runTest(testDispatcher) {
        callLogRepository.setRingingCall(callLog1)
        viewModel.uiState.test {
            awaitItem() // initial
            awaitItem() // callLog1

            callLogRepository.setRingingCall(null)
            val state = awaitItem()
            assertNull(state.ringingCall)
            assertFalse(state.isDismissed)
            assertFalse(state.showBanner)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun contactIsResolvedFromContactsList() = runTest(testDispatcher) {
        // Add call, with no contact first
        callLogRepository.setRingingCall(callLog1)
        viewModel.uiState.test {
            awaitItem() // initial
            val noContactState = awaitItem()
            assertNull(noContactState.contact)

            // Now add Alice
            contactRepository.setContacts(listOf(contact1))
            val withContactState = awaitItem()
            assertEquals(contact1, withContactState.contact)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun contactIsNull_whenNumberIsNotInContacts() = runTest(testDispatcher) {
        callLogRepository.setRingingCall(callLog2)
        contactRepository.setContacts(listOf(contact1)) // contact1 does not match callLog2
        viewModel.uiState.test {
            awaitItem() // initial
            val state = awaitItem()
            assertEquals(callLog2, state.ringingCall)
            assertNull(state.contact)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun isDismissedPersistsForSameCallId() = runTest(testDispatcher) {
        callLogRepository.setRingingCall(callLog1)
        viewModel.uiState.test {
            awaitItem() // initial
            awaitItem() // callLog1

            viewModel.dismiss()
            val dismissedState = awaitItem()
            assertTrue(dismissedState.isDismissed)
            assertFalse(dismissedState.showBanner)

            // re-emit same call, should persist
            callLogRepository.setRingingCall(callLog1Copy)
            val state = awaitItem()
            assertTrue(state.isDismissed)
            assertFalse(state.showBanner)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun isSpam_whenRingingCallIsSpam() = runTest(testDispatcher) {
        spamRepository.reportAsSpam("+1234567890")
        callLogRepository.setRingingCall(callLog1)
        viewModel.uiState.test {
            awaitItem() // initial
            val state = awaitItem()
            assertTrue(state.isSpam)
            assertTrue(state.showBanner)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun isSpam_falseWhenNotSpam() = runTest(testDispatcher) {
        callLogRepository.setRingingCall(callLog1)
        viewModel.uiState.test {
            awaitItem() // initial
            val state = awaitItem()
            assertFalse(state.isSpam)
            cancelAndConsumeRemainingEvents()
        }
    }
}
