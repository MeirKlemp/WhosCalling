package com.klemfner.whoscalling.ui.calllogs.calllog_details

import app.cash.turbine.test
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.model.SpamReport
import com.klemfner.whoscalling.fake.FakeCallLogRepository
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CallLogDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var callLogRepository: FakeCallLogRepository
    private lateinit var spamRepository: FakeSpamRepository
    private lateinit var viewModel: CallLogDetailsViewModel

    private val callLog1 = CallLog("log1", "+1234567890", CallType.INCOMING, false, 1000L, 120L)
    private val callLog2 = CallLog("log2", "+1234567890", CallType.OUTGOING, false, 2000L, 60L)
    private val callLog3 = CallLog("log3", "+0987654321", CallType.INCOMING, true, 3000L, 0L)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        callLogRepository = FakeCallLogRepository()
        spamRepository = FakeSpamRepository()
        viewModel = CallLogDetailsViewModel(callLogRepository, spamRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsEmpty() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(emptyList(), state.selectedNumberCallLogs)
            assertEquals(emptyMap(), state.spams)
        }
    }

    @Test
    fun setSelectedPhoneFiltersCallLogsByNumber() = runTest {
        callLogRepository.setCallLogs(listOf(callLog1, callLog2, callLog3))

        viewModel.uiState.test {
            awaitItem() // initial empty state

            viewModel.setSelectedPhone("+1234567890")
            val state = awaitItem()
            assertEquals(2, state.selectedNumberCallLogs.size)
            assertTrue(state.selectedNumberCallLogs.all { it.phoneNumber == "+1234567890" })
        }
    }

    @Test
    fun setSelectedPhoneSortsCallLogsByTimestampDescending() = runTest {
        callLogRepository.setCallLogs(listOf(callLog1, callLog2))

        viewModel.uiState.test {
            awaitItem() // initial empty state

            viewModel.setSelectedPhone("+1234567890")
            val state = awaitItem()
            assertEquals(2, state.selectedNumberCallLogs.size)
            assertEquals(callLog2, state.selectedNumberCallLogs[0])
            assertEquals(callLog1, state.selectedNumberCallLogs[1])
        }
    }

    @Test
    fun setNullPhoneReturnsEmptyList() = runTest {
        callLogRepository.setCallLogs(listOf(callLog1, callLog2))

        viewModel.uiState.test {
            awaitItem() // initial empty state

            viewModel.setSelectedPhone("+1234567890")
            awaitItem()

            viewModel.setSelectedPhone(null)
            val state = awaitItem()
            assertEquals(emptyList(), state.selectedNumberCallLogs)
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
}
