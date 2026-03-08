package com.klemfner.whoscalling.data.repository

import app.cash.turbine.test
import com.klemfner.whoscalling.domain.model.CallLog
import com.klemfner.whoscalling.domain.model.CallType
import com.klemfner.whoscalling.domain.model.UnauthorizedException
import com.klemfner.whoscalling.domain.model.UserPreferences
import com.klemfner.whoscalling.fake.FakeAuthRepository
import com.klemfner.whoscalling.fake.FakeCallLogLocalDataSource
import com.klemfner.whoscalling.fake.FakeCallLogRemoteDataSource
import com.klemfner.whoscalling.fake.FakeSettingsRepository
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CallLogRepositoryImplTest {

    private lateinit var remoteDataSource: FakeCallLogRemoteDataSource
    private lateinit var localDataSource: FakeCallLogLocalDataSource
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var repository: CallLogRepositoryImpl

    private var fakeCurrentTimeMillis = 100_000L

    @BeforeTest
    fun setup() {
        remoteDataSource = FakeCallLogRemoteDataSource()
        localDataSource = FakeCallLogLocalDataSource()
        authRepository = FakeAuthRepository()
        settingsRepository = FakeSettingsRepository(
            UserPreferences(countryIso = "US", touchMode = true, refreshRateSeconds = 0),
        )
        authRepository.setLoggedIn("user", "token")
        repository = createRepository()
    }

    private fun createRepository(
        normalizePhone: (String) -> String = { it },
        settingsRepo: FakeSettingsRepository = settingsRepository,
    ) = CallLogRepositoryImpl(
        remoteDataSource,
        localDataSource,
        authRepository,
        settingsRepo,
        scope = TestScope(),
        currentTimeMillis = { fakeCurrentTimeMillis },
        normalizePhone = normalizePhone,
    )

    @Test
    fun callLogs_emitsLocalData() = runTest {
        val logs = listOf(
            CallLog("1", "+1234567890", CallType.INCOMING, false, 1000L, 60L),
            CallLog("2", "+0987654321", CallType.OUTGOING, false, 2000L, 120L)
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
            CallLog("1", "+1234567890", CallType.INCOMING, false, 1000L, 60L),
            CallLog("2", "+0987654321", CallType.INCOMING, true, 2000L, 0L)
        )
        remoteDataSource.emit(remoteLogs)

        repository.refreshCallLogs()

        repository.callLogs.test {
            val saved = awaitItem()
            assertEquals(2, saved.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun refreshCallLogs_replacesExistingLocalData() = runTest {
        val oldLogs = listOf(
            CallLog("old1", "+1111111111", CallType.INCOMING, false, 500L, 30L)
        )
        localDataSource.saveCallLogs(oldLogs)

        val newLogs = listOf(
            CallLog("new1", "+2222222222", CallType.OUTGOING, false, 3000L, 90L)
        )
        remoteDataSource.emit(newLogs)

        repository.refreshCallLogs()

        repository.callLogs.test {
            val saved = awaitItem()
            assertEquals(1, saved.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun refreshCallLogs_setsIdToNormalizedNumberDashTimestamp() = runTest {
        repository = createRepository(normalizePhone = { "+1234" })

        val remoteLogs = listOf(
            CallLog("original-id", "5551234567", CallType.INCOMING, false, 9999L, 10L)
        )
        remoteDataSource.emit(remoteLogs)

        repository.refreshCallLogs()

        repository.callLogs.test {
            val saved = awaitItem()
            assertEquals("+1234-9999", saved[0].id)
            assertEquals("+1234", saved[0].phoneNumber)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun refreshCallLogs_normalizesPhoneNumbers() = runTest {
        repository = createRepository(normalizePhone = { "+1${it.filter { c -> c.isDigit() }}" })

        val remoteLogs = listOf(
            CallLog("1", "5551234567", CallType.INCOMING, false, 1000L, 60L)
        )
        remoteDataSource.emit(remoteLogs)

        repository.refreshCallLogs()

        repository.callLogs.test {
            val saved = awaitItem()
            assertEquals("+15551234567", saved[0].phoneNumber)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun refreshCallLogs_usesRawNumberOnNormalizationError() = runTest {
        repository = createRepository(normalizePhone = { throw IllegalArgumentException("Invalid") })

        val remoteLogs = listOf(
            CallLog("1", "invalid-number", CallType.INCOMING, false, 1000L, 60L)
        )
        remoteDataSource.emit(remoteLogs)

        repository.refreshCallLogs()

        repository.callLogs.test {
            val saved = awaitItem()
            assertEquals("invalid-number", saved[0].phoneNumber)
            assertEquals("invalid-number-1000", saved[0].id)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun ringingCall_emitsNullWhenNoCallLogs() = runTest {
        repository.ringingCall.test {
            assertNull(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun ringingCall_emitsNullWhenLastCallIsOlderThanOneMinute() = runTest {
        fakeCurrentTimeMillis = 200_000L
        val logs = listOf(
            CallLog("1", "+1234567890", CallType.INCOMING, true, 100_000L, 0L)
        )
        localDataSource.saveCallLogs(logs)

        repository.ringingCall.test {
            assertNull(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun ringingCall_emitsLastCallWhenIncomingMissedAndRecent() = runTest {
        fakeCurrentTimeMillis = 100_000L
        val earlier = CallLog("1", "+1234567890", CallType.INCOMING, true, 50_000L, 0L)
        val later = CallLog("2", "+0987654321", CallType.INCOMING, true, 80_000L, 0L)
        localDataSource.saveCallLogs(listOf(later, earlier))

        repository.ringingCall.test {
            assertEquals(later, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun ringingCall_emitsNullWhenLastCallIsOutgoing() = runTest {
        fakeCurrentTimeMillis = 100_000L
        val logs = listOf(
            CallLog("1", "+1234567890", CallType.OUTGOING, false, 80_000L, 60L)
        )
        localDataSource.saveCallLogs(logs)

        repository.ringingCall.test {
            assertNull(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun ringingCall_emitsNullWhenLastCallIsNotMissed() = runTest {
        fakeCurrentTimeMillis = 100_000L
        val logs = listOf(
            CallLog("1", "+1234567890", CallType.INCOMING, false, 80_000L, 60L)
        )
        localDataSource.saveCallLogs(logs)

        repository.ringingCall.test {
            assertNull(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun ringingCall_updatesWhenCallLogsChange() = runTest {
        fakeCurrentTimeMillis = 100_000L

        repository.ringingCall.test {
            assertNull(awaitItem())

            val log = CallLog("1", "+1234567890", CallType.INCOMING, true, 80_000L, 0L)
            localDataSource.saveCallLogs(listOf(log))
            assertEquals(log, awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun refreshCallLogs_retriesLoginOnUnauthorizedException() = runTest {
        val throwingRemote = object : com.klemfner.whoscalling.data.remote.CallLogRemoteDataSource {
            var callCount = 0
            override suspend fun getCallLogs(token: String?): List<CallLog> {
                callCount++
                if (callCount == 1) throw UnauthorizedException()
                return listOf(CallLog("1", "+1234567890", CallType.INCOMING, false, 1000L, 60L))
            }
        }

        repository = CallLogRepositoryImpl(
            throwingRemote,
            localDataSource,
            authRepository,
            settingsRepository,
            scope = TestScope(),
            currentTimeMillis = { fakeCurrentTimeMillis },
            normalizePhone = { it },
        )

        repository.refreshCallLogs()

        assertEquals(1, authRepository.retryLoginCallCount)
        repository.callLogs.test {
            assertEquals(1, awaitItem().size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun refreshCallLogs_throwsWhenRetryAlsoFails() = runTest {
        val throwingRemote = object : com.klemfner.whoscalling.data.remote.CallLogRemoteDataSource {
            override suspend fun getCallLogs(token: String?): List<CallLog> {
                throw UnauthorizedException()
            }
        }

        repository = CallLogRepositoryImpl(
            throwingRemote,
            localDataSource,
            authRepository,
            settingsRepository,
            scope = TestScope(),
            currentTimeMillis = { fakeCurrentTimeMillis },
            normalizePhone = { it },
        )

        val oldLogs = listOf(CallLog("old", "+1111111111", CallType.INCOMING, false, 500L, 30L))
        localDataSource.saveCallLogs(oldLogs)

        assertFailsWith<UnauthorizedException> {
            repository.refreshCallLogs()
        }

        // Old call logs should still be there
        repository.callLogs.test {
            assertEquals(oldLogs, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun autoRefresh_stopsWhenRefreshRateSetToZero() = runTest {
        val zeroRateSettings = FakeSettingsRepository(
            UserPreferences(refreshRateSeconds = 0)
        )
        val noRefreshRepo = createRepository(settingsRepo = zeroRateSettings)

        val remoteLogs = listOf(
            CallLog("1", "+1234567890", CallType.INCOMING, false, 1000L, 60L),
        )
        remoteDataSource.emit(remoteLogs)

        // With refresh rate 0 (never), auto-refresh should not run
        // The local data source should remain empty
        noRefreshRepo.callLogs.test {
            assertEquals(emptyList(), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun autoRefresh_stopsWhenUserIsLoggedOut() = runTest {
        authRepository.setLoggedOut()
        repository = createRepository()

        val remoteLogs = listOf(
            CallLog("1", "+1234567890", CallType.INCOMING, false, 1000L, 60L),
        )
        remoteDataSource.emit(remoteLogs)

        // With no logged in user, auto-refresh should not run
        repository.callLogs.test {
            assertEquals(emptyList(), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }
}
