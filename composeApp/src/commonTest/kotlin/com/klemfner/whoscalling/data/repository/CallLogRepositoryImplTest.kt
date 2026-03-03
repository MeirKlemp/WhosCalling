package com.klemfner.whoscalling.data.repository

import app.cash.turbine.test
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.fake.FakeCallLogLocalDataSource
import com.klemfner.whoscalling.fake.FakeCallLogRemoteDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CallLogRepositoryImplTest {

    private lateinit var remoteDataSource: FakeCallLogRemoteDataSource
    private lateinit var localDataSource: FakeCallLogLocalDataSource
    private lateinit var repository: CallLogRepositoryImpl

    private var fakeCurrentTimeMillis = 100_000L

    @BeforeTest
    fun setup() {
        remoteDataSource = FakeCallLogRemoteDataSource()
        localDataSource = FakeCallLogLocalDataSource()
        repository = CallLogRepositoryImpl(
            remoteDataSource,
            localDataSource,
            currentTimeMillis = { fakeCurrentTimeMillis }
        )
    }

    @Test
    fun callLogs_emitsLocalData() = runTest {
        val logs = listOf(
            CallLog("1", "+1234567890", "Alice", CallType.INCOMING, false, 1000L, 60L),
            CallLog("2", "+0987654321", "Bob", CallType.OUTGOING, false, 2000L, 120L)
        )
        localDataSource.saveCallLogs(logs)

        repository.callLogs.test {
            assertEquals(logs, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun callLogs_emptyWhenNoData() = runTest {
        repository.callLogs.test {
            assertEquals(emptyList(), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun refreshCallLogs_fetchesRemoteAndSavesToLocal() = runTest {
        val remoteLogs = listOf(
            CallLog("1", "+1234567890", "Alice", CallType.INCOMING, false, 1000L, 60L),
            CallLog("2", "+0987654321", "Bob", CallType.INCOMING, true, 2000L, 0L)
        )
        remoteDataSource.emit(remoteLogs)

        repository.refreshCallLogs()

        repository.callLogs.test {
            assertEquals(remoteLogs, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun refreshCallLogs_replacesExistingLocalData() = runTest {
        val oldLogs = listOf(
            CallLog("old1", "+1111111111", "Old", CallType.INCOMING, false, 500L, 30L)
        )
        localDataSource.saveCallLogs(oldLogs)

        val newLogs = listOf(
            CallLog("new1", "+2222222222", "New", CallType.OUTGOING, false, 3000L, 90L)
        )
        remoteDataSource.emit(newLogs)

        repository.refreshCallLogs()

        repository.callLogs.test {
            assertEquals(newLogs, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun incomingCallLog_emitsNullWhenNoIncomingCalls() = runTest {
        repository.incomingCallLog.test {
            assertNull(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun incomingCallLog_emitsNullWhenIncomingCallIsOlderThanOneMinute() = runTest {
        fakeCurrentTimeMillis = 200_000L
        val logs = listOf(
            CallLog("1", "+1234567890", "Alice", CallType.INCOMING, false, 100_000L, 0L)
        )
        localDataSource.saveCallLogs(logs)

        repository.incomingCallLog.test {
            assertNull(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun incomingCallLog_emitsEarliestIncomingCallWithinLastMinute() = runTest {
        fakeCurrentTimeMillis = 100_000L
        val earlier = CallLog("1", "+1234567890", "Alice", CallType.INCOMING, false, 50_000L, 0L)
        val later = CallLog("2", "+0987654321", "Bob", CallType.INCOMING, false, 80_000L, 0L)
        localDataSource.saveCallLogs(listOf(later, earlier))

        repository.incomingCallLog.test {
            assertEquals(earlier, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun incomingCallLog_ignoresOutgoingCalls() = runTest {
        fakeCurrentTimeMillis = 100_000L
        val logs = listOf(
            CallLog("1", "+1234567890", "Alice", CallType.OUTGOING, false, 80_000L, 60L)
        )
        localDataSource.saveCallLogs(logs)

        repository.incomingCallLog.test {
            assertNull(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun incomingCallLog_updatesWhenCallLogsChange() = runTest {
        fakeCurrentTimeMillis = 100_000L

        repository.incomingCallLog.test {
            assertNull(awaitItem())

            val log = CallLog("1", "+1234567890", "Alice", CallType.INCOMING, false, 80_000L, 0L)
            localDataSource.saveCallLogs(listOf(log))
            assertEquals(log, awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }
}
