package com.klemfner.whoscalling.ui.calllogs

import app.cash.turbine.test
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
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
class CallLogsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var spamRepository: FakeSpamRepository
    private lateinit var viewModel: CallLogsViewModel

    private val callLog1 = CallLog("log1", "+1234567890", CallType.INCOMING, false, 1000L, 120L)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        spamRepository = FakeSpamRepository()
        viewModel = CallLogsViewModel(spamRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateHasListPaneAndNoSelection() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(CallLogsPane.LIST, state.currentPane)
            assertNull(state.selectedCallLog)
        }
    }

    @Test
    fun selectCallLogNavigatesToDetails() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.selectCallLog(callLog1)
            val state = awaitItem()
            assertEquals(CallLogsPane.DETAILS, state.currentPane)
            assertEquals(callLog1, state.selectedCallLog)
        }
    }

    @Test
    fun goBackFromDetailsReturnsToList() = runTest {
        viewModel.uiState.test {
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
