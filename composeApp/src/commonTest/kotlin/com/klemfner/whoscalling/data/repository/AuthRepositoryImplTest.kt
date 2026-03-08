package com.klemfner.whoscalling.data.repository

import app.cash.turbine.test
import com.klemfner.whoscalling.domain.model.SavedCredentials
import com.klemfner.whoscalling.fake.FakeAuthLocalDataSource
import com.klemfner.whoscalling.fake.FakeAuthRemoteDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthRepositoryImplTest {

    private lateinit var remoteDataSource: FakeAuthRemoteDataSource
    private lateinit var localDataSource: FakeAuthLocalDataSource
    private lateinit var repository: AuthRepositoryImpl

    private var fakeCurrentTimeMillis = 1000L

    @BeforeTest
    fun setup() {
        remoteDataSource = FakeAuthRemoteDataSource()
        localDataSource = FakeAuthLocalDataSource()
        repository = AuthRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            currentTimeMillis = { fakeCurrentTimeMillis },
        )
    }

    @Test
    fun initialStateIsLoggedOut() = runTest {
        repository.loggedInUser.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun loginSetsLoggedInUser() = runTest {
        repository.loggedInUser.test {
            assertNull(awaitItem())

            repository.login("alice", "pass123", rememberMe = false)
            val user = awaitItem()
            assertNotNull(user)
            assertEquals("alice", user.username)
            assertEquals(1000L, user.loginTime)
        }
    }

    @Test
    fun loginSetsToken() = runTest {
        assertNull(repository.getToken())
        repository.login("alice", "pass123", rememberMe = false)
        assertNotNull(repository.getToken())
    }

    @Test
    fun loginWithRememberMeSavesToLocalDataSource() = runTest {
        repository.login("alice", "pass123", rememberMe = true)

        val saved = localDataSource.savedCredentials.value
        assertNotNull(saved)
        assertEquals("alice", saved.username)
        assertEquals("pass123", saved.password)
        assertNotNull(saved.sessionKey)
        assertEquals(1000L, saved.loginTime)
    }

    @Test
    fun loginWithoutRememberMeClearsLocalDataSource() = runTest {
        localDataSource.saveCredentials(
            SavedCredentials("old", "old", 0L, "old")
        )

        repository.login("alice", "pass123", rememberMe = false)

        assertNull(localDataSource.savedCredentials.value)
    }

    @Test
    fun loginFailurePropagatesException() = runTest {
        remoteDataSource.setResult(Result.failure(IllegalArgumentException("Bad credentials")))
        assertFailsWith<IllegalArgumentException> {
            repository.login("alice", "bad", rememberMe = false)
        }
    }

    @Test
    fun loginIllegalStateExceptionLogsOut() = runTest {
        repository.login("alice", "pass123", rememberMe = true)
        assertNotNull(repository.loggedInUser.value)
        assertNotNull(repository.getToken())

        remoteDataSource.setResult(Result.failure(IllegalStateException("Token expired")))
        assertFailsWith<IllegalStateException> {
            repository.login("alice", "pass123", rememberMe = true)
        }

        assertNull(repository.getToken())
        assertNull(repository.loggedInUser.value)
        assertNull(localDataSource.savedCredentials.value)
    }

    @Test
    fun logoutClearsState() = runTest {
        repository.login("alice", "pass123", rememberMe = true)

        repository.loggedInUser.test {
            assertNotNull(awaitItem())

            repository.logout()
            assertNull(awaitItem())
        }

        assertNull(repository.getToken())
        assertNull(localDataSource.savedCredentials.value)
    }

    @Test
    fun retryLoginRefreshesToken() = runTest {
        repository.login("alice", "pass123", rememberMe = true)

        remoteDataSource.setResult(Result.success("new-token"))
        repository.retryLogin()

        assertNotNull(repository.getToken())
        assertEquals("new-token", repository.getToken())
    }

    @Test
    fun retryLoginWithoutRememberMeThrowsAndLogsOut() = runTest {
        repository.login("alice", "pass123", rememberMe = false)

        assertFailsWith<IllegalStateException> {
            repository.retryLogin()
        }

        assertNull(repository.getToken())
        assertNull(repository.loggedInUser.value)
    }

    @Test
    fun retryLoginWithoutCredentialsThrowsAndLogsOut() = runTest {
        assertFailsWith<IllegalStateException> {
            repository.retryLogin()
        }

        assertNull(repository.getToken())
        assertNull(repository.loggedInUser.value)
    }

    @Test
    fun retryLoginUpdatesLocalWhenSaved() = runTest {
        repository.login("alice", "pass123", rememberMe = true)

        remoteDataSource.setResult(Result.success("refreshed-token"))
        repository.retryLogin()

        assertEquals("refreshed-token", localDataSource.savedCredentials.value?.sessionKey)
    }

    @Test
    fun retryLoginIllegalStateExceptionLogsOut() = runTest {
        repository.login("alice", "pass123", rememberMe = true)
        assertNotNull(repository.loggedInUser.value)
        assertNotNull(repository.getToken())

        remoteDataSource.setResult(Result.failure(IllegalStateException("Token expired")))
        assertFailsWith<IllegalStateException> {
            repository.retryLogin()
        }

        assertNull(repository.getToken())
        assertNull(repository.loggedInUser.value)
        assertNull(localDataSource.savedCredentials.value)
    }

    @Test
    fun initRestoresFromLocalDataSource() = runTest {
        localDataSource.saveCredentials(
            SavedCredentials("bob", "pass456", 2000L, "saved-token")
        )

        val restored = AuthRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            currentTimeMillis = { fakeCurrentTimeMillis },
        )

        restored.loggedInUser.test {
            val user = awaitItem()
            assertNotNull(user)
            assertEquals("bob", user.username)
            assertEquals(2000L, user.loginTime)
        }
        assertEquals("saved-token", restored.getToken())
    }

    @Test
    fun initDoesNotRestoreWithIncompleteLocalData() = runTest {
        localDataSource.saveCredentials(
            SavedCredentials("bob", "pass456", 2000L, "token")
        )
        localDataSource.clearCredentials()

        val restored = AuthRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            currentTimeMillis = { fakeCurrentTimeMillis },
        )

        restored.loggedInUser.test {
            assertNull(awaitItem())
        }
        assertNull(restored.getToken())
    }
}
