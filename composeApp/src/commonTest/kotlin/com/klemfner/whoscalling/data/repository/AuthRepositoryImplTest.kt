package com.klemfner.whoscalling.data.repository

import app.cash.turbine.test
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

        assertEquals("alice", localDataSource.getSavedUsername())
        assertEquals("pass123", localDataSource.getSavedPassword())
        assertNotNull(localDataSource.getSavedToken())
        assertEquals(1000L, localDataSource.getSavedLoginTime())
    }

    @Test
    fun loginWithoutRememberMeClearsLocalDataSource() = runTest {
        localDataSource.saveCredentials("old", "old", "old", 0L)

        repository.login("alice", "pass123", rememberMe = false)

        assertNull(localDataSource.getSavedUsername())
        assertNull(localDataSource.getSavedToken())
    }

    @Test
    fun loginFailurePropagatesException() = runTest {
        remoteDataSource.setResult(Result.failure(IllegalArgumentException("Bad credentials")))
        assertFailsWith<IllegalArgumentException> {
            repository.login("alice", "bad", rememberMe = false)
        }
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
        assertNull(localDataSource.getSavedUsername())
    }

    @Test
    fun retryLoginRefreshesToken() = runTest {
        repository.login("alice", "pass123", rememberMe = false)
        val oldToken = repository.getToken()

        remoteDataSource.setResult(Result.success("new-token"))
        repository.retryLogin()

        assertNotNull(repository.getToken())
        assertEquals("new-token", repository.getToken())
    }

    @Test
    fun retryLoginWithoutCredentialsThrows() = runTest {
        assertFailsWith<IllegalStateException> {
            repository.retryLogin()
        }
    }

    @Test
    fun retryLoginUpdatesLocalWhenSaved() = runTest {
        repository.login("alice", "pass123", rememberMe = true)
        val oldToken = localDataSource.getSavedToken()

        remoteDataSource.setResult(Result.success("refreshed-token"))
        repository.retryLogin()

        assertEquals("refreshed-token", localDataSource.getSavedToken())
    }

    @Test
    fun initRestoresFromLocalDataSource() = runTest {
        localDataSource.saveCredentials("bob", "pass456", "saved-token", 2000L)

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
        // Only username saved, missing others
        localDataSource.saveCredentials("bob", "pass456", "token", 2000L)
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
