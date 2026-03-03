package com.klemfner.whoscalling.data.repository

import app.cash.turbine.test
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.model.IncomingCall
import com.klemfner.whoscalling.fake.FakeCallLogLocalDataSource
import com.klemfner.whoscalling.fake.FakeCallLogRemoteDataSource
import com.klemfner.whoscalling.fake.FakeIncomingCallDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CallLogRepositoryImplTest {

    private lateinit var remoteDataSource: FakeCallLogRemoteDataSource
    private lateinit var localDataSource: FakeCallLogLocalDataSource
    private lateinit var incomingCallDataSource: FakeIncomingCallDataSource
    private lateinit var repository: CallLogRepositoryImpl

    @BeforeTest
    fun setup() {
        remoteDataSource = FakeCallLogRemoteDataSource()
        localDataSource = FakeCallLogLocalDataSource()
        incomingCallDataSource = FakeIncomingCallDataSource()
        repository = CallLogRepositoryImpl(remoteDataSource, localDataSource, incomingCallDataSource)
    }

    @Test
    fun getCallLogs_emitsLocalData() = runTest {
        val logs = listOf(
            CallLog("1", "+1234567890", "Alice", CallType.INCOMING, 1000L, 60L),
            CallLog("2", "+0987654321", "Bob", CallType.OUTGOING, 2000L, 120L)
        )
        localDataSource.saveCallLogs(logs)

        repository.getCallLogs().test {
            assertEquals(logs, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun getCallLogs_emptyWhenNoData() = runTest {
        repository.getCallLogs().test {
            assertEquals(emptyList(), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun refreshCallLogs_fetchesRemoteAndSavesToLocal() = runTest {
        val remoteLogs = listOf(
            CallLog("1", "+1234567890", "Alice", CallType.INCOMING, 1000L, 60L),
            CallLog("2", "+0987654321", "Bob", CallType.MISSED, 2000L, 0L)
        )
        remoteDataSource.emit(remoteLogs)

        repository.refreshCallLogs()

        repository.getCallLogs().test {
            assertEquals(remoteLogs, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun refreshCallLogs_replacesExistingLocalData() = runTest {
        val oldLogs = listOf(
            CallLog("old1", "+1111111111", "Old", CallType.INCOMING, 500L, 30L)
        )
        localDataSource.saveCallLogs(oldLogs)

        val newLogs = listOf(
            CallLog("new1", "+2222222222", "New", CallType.OUTGOING, 3000L, 90L)
        )
        remoteDataSource.emit(newLogs)

        repository.refreshCallLogs()

        repository.getCallLogs().test {
            assertEquals(newLogs, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun observeIncomingCall_emitsNullInitially() = runTest {
        repository.observeIncomingCall().test {
            assertNull(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun observeIncomingCall_emitsIncomingCall() = runTest {
        val call = IncomingCall("+1234567890", "Alice", 5000L)

        repository.observeIncomingCall().test {
            assertNull(awaitItem())

            incomingCallDataSource.emit(call)
            assertEquals(call, awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun observeIncomingCall_emitsNullWhenCallEnds() = runTest {
        val call = IncomingCall("+1234567890", "Alice", 5000L)

        repository.observeIncomingCall().test {
            assertNull(awaitItem())

            incomingCallDataSource.emit(call)
            assertEquals(call, awaitItem())

            incomingCallDataSource.emit(null)
            assertNull(awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }
}
