package com.klemfner.whoscalling.ui.user

import app.cash.turbine.test
import com.klemfner.whoscalling.data.repository.AuthRepositoryImpl
import com.klemfner.whoscalling.fake.FakeAuthLocalDataSource
import com.klemfner.whoscalling.fake.FakeAuthRemoteDataSource
import com.klemfner.whoscalling.fake.FakeSettingsRepository
import com.klemfner.whoscalling.ui.user.LoginError
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRemoteDataSource: FakeAuthRemoteDataSource
    private lateinit var authLocalDataSource: FakeAuthLocalDataSource
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var viewModel: UserViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRemoteDataSource = FakeAuthRemoteDataSource()
        authLocalDataSource = FakeAuthLocalDataSource()
        settingsRepository = FakeSettingsRepository()

        val authRepository = AuthRepositoryImpl(
            remoteDataSource = authRemoteDataSource,
            localDataSource = authLocalDataSource,
            currentTimeMillis = { 5000L },
        )
        viewModel = UserViewModel(authRepository, settingsRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsLoggedOut() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.loggedInUser)
            assertFalse(state.isLoading)
            assertNull(state.loginError)
        }
    }

    @Test
    fun updateUsernameUpdatesState() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.updateUsername("alice")
            assertEquals("alice", awaitItem().username)
        }
    }

    @Test
    fun updatePasswordUpdatesState() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.updatePassword("secret")
            assertEquals("secret", awaitItem().password)
        }
    }

    @Test
    fun updateRememberMeUpdatesState() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.updateRememberMe(true)
            assertTrue(awaitItem().rememberMe)
        }
    }

    @Test
    fun loginSuccessSetsLoggedInUser() = runTest {
        viewModel.updateUsername("alice")
        viewModel.updatePassword("pass123")

        viewModel.uiState.test {
            awaitItem()
            viewModel.login()

            // Wait for isLoading=true then loggedInUser set
            var state: UserUiState
            do {
                state = awaitItem()
            } while (state.loggedInUser == null)

            assertNotNull(state.loggedInUser)
            assertEquals("alice", state.loggedInUser.username)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun loginSuccessClearsPassword() = runTest {
        viewModel.updateUsername("alice")
        viewModel.updatePassword("pass123")

        viewModel.uiState.test {
            awaitItem()
            viewModel.login()

            var state: UserUiState
            do {
                state = awaitItem()
            } while (state.loggedInUser == null)

            assertEquals("", state.password)
        }
    }

    @Test
    fun loginFailureWithBlankCredentialsSetsBlankCredentialsError() = runTest {
        authRemoteDataSource.setResult(Result.failure(IllegalArgumentException("Username and password must not be blank")))
        viewModel.updateUsername("")
        viewModel.updatePassword("")

        viewModel.uiState.test {
            awaitItem()
            viewModel.login()

            var state: UserUiState
            do {
                state = awaitItem()
            } while (state.loginError == null && state.isLoading)

            assertNotNull(state.loginError)
            assertTrue(state.loginError is LoginError.BlankCredentials)
            assertFalse(state.isLoading)
            assertNull(state.loggedInUser)
        }
    }

    @Test
    fun loginFailureWithGenericErrorSetsGenericError() = runTest {
        authRemoteDataSource.setResult(Result.failure(RuntimeException("Network error")))
        viewModel.updateUsername("alice")
        viewModel.updatePassword("pass123")

        viewModel.uiState.test {
            awaitItem()
            viewModel.login()

            var state: UserUiState
            do {
                state = awaitItem()
            } while (state.loginError == null && state.isLoading)

            assertNotNull(state.loginError)
            assertTrue(state.loginError is LoginError.Generic)
            assertFalse(state.isLoading)
            assertNull(state.loggedInUser)
        }
    }

    @Test
    fun logoutClearsLoggedInUser() = runTest {
        viewModel.updateUsername("alice")
        viewModel.updatePassword("pass123")

        viewModel.uiState.test {
            awaitItem()
            viewModel.login()

            var state: UserUiState
            do {
                state = awaitItem()
            } while (state.loggedInUser == null)
            assertNotNull(state.loggedInUser)

            viewModel.logout()
            do {
                state = awaitItem()
            } while (state.loggedInUser != null)
            assertNull(state.loggedInUser)
        }
    }

    @Test
    fun clearErrorResetsLoginError() = runTest {
        authRemoteDataSource.setResult(Result.failure(IllegalArgumentException("Fail")))
        viewModel.updateUsername("alice")
        viewModel.updatePassword("wrong")

        viewModel.uiState.test {
            awaitItem()
            viewModel.login()

            var state: UserUiState
            do {
                state = awaitItem()
            } while (state.loginError == null && state.isLoading)
            assertNotNull(state.loginError)

            viewModel.clearError()
            state = awaitItem()
            assertNull(state.loginError)
        }
    }

    @Test
    fun routerIp_initialValueFromSettings() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.routerIp)
        }
    }

    @Test
    fun routerIp_updatesWhenSettingsChange() = runTest {
        viewModel.uiState.test {
            awaitItem()

            settingsRepository.setRouterIp("192.168.1.1")
            var state = awaitItem()
            while (state.routerIp != "192.168.1.1") {
                state = awaitItem()
            }
            assertEquals("192.168.1.1", state.routerIp)
        }
    }

    @Test
    fun routerIp_initializedWithConfiguredValue() = runTest {
        val configuredSettingsRepository = FakeSettingsRepository(
            initialPreferences = com.klemfner.whoscalling.domain.model.UserPreferences(
                countryIso = "US",
                touchMode = true,
                routerIp = "10.0.0.1",
            ),
        )
        val authRepository = AuthRepositoryImpl(
            remoteDataSource = authRemoteDataSource,
            localDataSource = authLocalDataSource,
            currentTimeMillis = { 5000L },
        )
        val vm = UserViewModel(authRepository, configuredSettingsRepository)

        vm.uiState.test {
            val state = awaitItem()
            assertEquals("10.0.0.1", state.routerIp)
        }
    }
}
