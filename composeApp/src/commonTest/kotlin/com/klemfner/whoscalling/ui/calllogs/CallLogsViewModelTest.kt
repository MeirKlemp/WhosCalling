package com.klemfner.whoscalling.ui.calllogs

import app.cash.turbine.test
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.fake.FakeAuthRepository
import com.klemfner.whoscalling.fake.FakeCallLogRepository
import com.klemfner.whoscalling.fake.FakeContactRepository
import com.klemfner.whoscalling.fake.FakeSettingsRepository
import com.klemfner.whoscalling.fake.FakeSpamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withTimeout
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
class CallLogsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var callLogRepository: FakeCallLogRepository
    private lateinit var contactRepository: FakeContactRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var spamRepository: FakeSpamRepository
    private lateinit var viewModel: CallLogsViewModel

    private val contact1 = Contact("1", "Alice", "+1234567890", "alice@test.com")
    private val contact2 = Contact("2", "Bob", "+0987654321", null)

    private val callLog1 = CallLog("log1", "+1234567890", CallType.INCOMING, false, 1000L, 120L)
    private val callLog2 = CallLog("log2", "+1234567890", CallType.OUTGOING, false, 2000L, 60L)
    private val callLog3 = CallLog("log3", "+0987654321", CallType.INCOMING, true, 3000L, 0L)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        callLogRepository = FakeCallLogRepository()
        contactRepository = FakeContactRepository()
        authRepository = FakeAuthRepository()
        authRepository.setLoggedIn("user", "token")
        spamRepository = FakeSpamRepository()

        viewModel = CallLogsViewModel(callLogRepository, contactRepository, authRepository, FakeSettingsRepository(), spamRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsEmpty() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(emptyList(), state.callLogs)
            assertEquals(CallLogsPane.LIST, state.currentPane)
            assertNull(state.selectedCallLog)
        }
    }

    @Test
    fun callLogsAreSortedByTimestampDescending() = runTest {
        callLogRepository.setCallLogs(listOf(callLog1, callLog2, callLog3))

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals(3, state.callLogs.size)
            assertEquals(callLog3, state.callLogs[0])
            assertEquals(callLog2, state.callLogs[1])
            assertEquals(callLog1, state.callLogs[2])
        }
    }

    @Test
    fun contactsAreResolvedByPhoneNumber() = runTest {
        contactRepository.setContacts(listOf(contact1))
        callLogRepository.setCallLogs(listOf(callLog1))

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals(contact1, state.contacts["+1234567890"])
        }
    }

    @Test
    fun selectCallLogNavigatesToDetails() = runTest {
        callLogRepository.setCallLogs(listOf(callLog1))

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.selectCallLog(callLog1)
            val state = awaitItem()
            assertEquals(CallLogsPane.DETAILS, state.currentPane)
            assertEquals(callLog1, state.selectedCallLog)
        }
    }

    @Test
    fun selectCallLogFiltersNumberCallLogs() = runTest {
        callLogRepository.setCallLogs(listOf(callLog1, callLog2, callLog3))

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.selectCallLog(callLog1)
            awaitItem()
            val state = awaitItem()
            assertEquals(2, state.selectedNumberCallLogs.size)
            assertEquals(callLog2, state.selectedNumberCallLogs[0])
            assertEquals(callLog1, state.selectedNumberCallLogs[1])
        }
    }

    @Test
    fun goBackFromDetailsReturnsToList() = runTest {
        callLogRepository.setCallLogs(listOf(callLog1))

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.selectCallLog(callLog1)
            awaitItem()

            viewModel.goBack()
            val state = awaitItem()
            assertEquals(CallLogsPane.LIST, state.currentPane)
            assertNull(state.selectedCallLog)
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
    fun refreshSetsIsRefreshing() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.refresh()
            val state = awaitItem()
            assertEquals(true, state.isRefreshing)
        }
    }

    @Test
    fun refreshLoadsRemoteCallLogs() = runTest {
        val remoteLogs = listOf(callLog1, callLog2)
        callLogRepository.setRefreshCallLogs(remoteLogs)

        viewModel.uiState.test {
            awaitItem()

            viewModel.refresh()

            // isRefreshing and callLogs update via separate coroutines,
            // so they may arrive in separate emissions
            var state: CallLogsUiState
            withTimeout(100) {
                do {
                    state = awaitItem()
                } while (state.isRefreshing || state.callLogs.isEmpty())
            }

            assertFalse(state.isRefreshing)
            assertEquals(2, state.callLogs.size)
        }
    }

    @Test
    fun unsavedContactShowsPhoneNumber() = runTest {
        callLogRepository.setCallLogs(listOf(callLog3))

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertNull(state.contacts["+0987654321"])
        }
    }

    @Test
    fun savedContactShowsInContactsMap() = runTest {
        contactRepository.setContacts(listOf(contact2))
        callLogRepository.setCallLogs(listOf(callLog3))

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals(contact2, state.contacts["+0987654321"])
        }
    }

    @Test
    fun isLoggedInReflectsAuthState() = runTest {
        viewModel.uiState.test {
            var state = awaitItem()
            // Initially logged in (set in setup)
            assertTrue(state.isLoggedIn)

            authRepository.setLoggedOut()
            state = awaitItem()
            assertFalse(state.isLoggedIn)
        }
    }

    @Test
    fun refreshErrorIsSetOnFailure() = runTest(testDispatcher) {
        callLogRepository.refreshException = RuntimeException("Network error")

        viewModel.uiState.test {
            awaitItem()
            viewModel.refresh()

            var state: CallLogsUiState
            withTimeout(100) {
                do {
                    state = awaitItem()
                } while (!state.refreshError || state.isRefreshing)
            }

            assertTrue(state.refreshError)
            assertFalse(state.isRefreshing)
        }
    }

    @Test
    fun selectCallLogByIdNavigatesToDetails() = runTest {
        callLogRepository.setCallLogs(listOf(callLog1, callLog2))

        viewModel.uiState.test {
            skipItems(1)
            awaitItem() // wait for call logs to load

            viewModel.selectCallLogById(callLog1.id)
            val state = awaitItem()

            assertEquals(CallLogsPane.DETAILS, state.currentPane)
            assertEquals(callLog1, state.selectedCallLog)
        }
    }

    @Test
    fun selectCallLogByIdWithUnknownIdDoesNothing() = runTest {
        callLogRepository.setCallLogs(listOf(callLog1))

        viewModel.uiState.test {
            skipItems(1)
            awaitItem() // wait for call logs to load

            viewModel.selectCallLogById("unknown-id")
            expectNoEvents()
        }
    }

    @Test
    fun spamNumbersAppearInState() = runTest {
        callLogRepository.setCallLogs(listOf(callLog1))
        spamRepository.reportAsSpam("+1234567890")

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(state.spams["+1234567890"]?.isSpam == true)
        }
    }

    @Test
    fun requestReportSpamShowsDialog() = runTest {
        callLogRepository.setCallLogs(listOf(callLog1))

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.selectCallLog(callLog1)
            awaitItem()

            viewModel.requestReportSpam()
            val state = awaitItem()
            assertTrue(state.showReportSpamDialog)
            assertEquals("+1234567890", state.reportDialogPhoneNumber)
        }
    }

    @Test
    fun confirmReportSpamAddsToSpam() = runTest {
        callLogRepository.setCallLogs(listOf(callLog1))

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.selectCallLog(callLog1)
            awaitItem()

            viewModel.requestReportSpam()
            awaitItem()

            viewModel.confirmReportSpam()
            var state = awaitItem()
            assertFalse(state.showReportSpamDialog)

            // Wait for spam list to update
            while (state.spams["+1234567890"]?.isSpam != true) {
                state = awaitItem()
            }
            assertTrue(state.spams["+1234567890"]?.isSpam == true)
        }
    }

    @Test
    fun dismissReportDialogClosesDialog() = runTest {
        callLogRepository.setCallLogs(listOf(callLog1))

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.selectCallLog(callLog1)
            awaitItem()

            viewModel.requestReportSpam()
            awaitItem()

            viewModel.dismissReportDialog()
            val state = awaitItem()
            assertFalse(state.showReportSpamDialog)
            assertFalse(state.showTrustNumberDialog)
        }
    }
}
