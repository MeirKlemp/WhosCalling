package com.klemfner.whoscalling.ui.calllogs

import app.cash.turbine.test
import com.klemfner.whoscalling.data.repository.CallLogRepositoryImpl
import com.klemfner.whoscalling.data.repository.ContactRepositoryImpl
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.model.Contact
import com.klemfner.whoscalling.fake.FakeAuthRepository
import com.klemfner.whoscalling.fake.FakeCallLogLocalDataSource
import com.klemfner.whoscalling.fake.FakeCallLogRemoteDataSource
import com.klemfner.whoscalling.fake.FakeContactLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
    private val testScope = TestScope(testDispatcher)
    private lateinit var contactLocalDataSource: FakeContactLocalDataSource
    private lateinit var callLogLocalDataSource: FakeCallLogLocalDataSource
    private lateinit var callLogRemoteDataSource: FakeCallLogRemoteDataSource
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: CallLogsViewModel

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
        authRepository = FakeAuthRepository()
        authRepository.setLoggedIn("user", "token")

        val contactRepository = ContactRepositoryImpl(
            localDataSource = contactLocalDataSource,
            normalizePhone = { it },
        )
        val callLogRepository = CallLogRepositoryImpl(
            remoteDataSource = callLogRemoteDataSource,
            localDataSource = callLogLocalDataSource,
            authRepository = authRepository,
            scope = testScope,
            normalizePhone = { it },
            refreshIntervalMs = Long.MAX_VALUE,
        )
        viewModel = CallLogsViewModel(callLogRepository, contactRepository, authRepository)
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
        callLogLocalDataSource.saveCallLogs(listOf(callLog1, callLog2, callLog3))

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
        contactLocalDataSource.saveContact(contact1)
        callLogLocalDataSource.saveCallLogs(listOf(callLog1))

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals(contact1, state.contacts["+1234567890"])
        }
    }

    @Test
    fun selectCallLogNavigatesToDetails() = runTest {
        callLogLocalDataSource.saveCallLogs(listOf(callLog1))

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
        callLogLocalDataSource.saveCallLogs(listOf(callLog1, callLog2, callLog3))

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
        callLogLocalDataSource.saveCallLogs(listOf(callLog1))

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
        callLogRemoteDataSource.emit(remoteLogs)

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
        callLogLocalDataSource.saveCallLogs(listOf(callLog3))

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertNull(state.contacts["+0987654321"])
        }
    }

    @Test
    fun savedContactShowsInContactsMap() = runTest {
        contactLocalDataSource.saveContact(contact2)
        callLogLocalDataSource.saveCallLogs(listOf(callLog3))

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
    fun refreshErrorIsSetOnFailure() = runTest {
        val throwingRemote = object : com.klemfner.whoscalling.data.remote.CallLogRemoteDataSource {
            override suspend fun getCallLogs(token: String?): List<CallLog> {
                throw RuntimeException("Network error")
            }
        }

        val callLogRepository = CallLogRepositoryImpl(
            remoteDataSource = throwingRemote,
            localDataSource = callLogLocalDataSource,
            authRepository = authRepository,
            scope = testScope,
            normalizePhone = { it },
            refreshIntervalMs = Long.MAX_VALUE,
        )
        viewModel = CallLogsViewModel(callLogRepository, ContactRepositoryImpl(
            localDataSource = contactLocalDataSource,
            normalizePhone = { it },
        ), authRepository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.refresh()

            var state: CallLogsUiState
            withTimeout(100) {
                do {
                    state = awaitItem()
                } while (state.refreshError == null && state.isRefreshing)
            }

            assertEquals("failed to refresh", state.refreshError)
            assertFalse(state.isRefreshing)
        }
    }
}
