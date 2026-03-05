package com.klemfner.whoscalling.fake

import com.klemfner.whoscalling.domain.model.LoggedInUser
import com.klemfner.whoscalling.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository : AuthRepository {
    private val _loggedInUser = MutableStateFlow<LoggedInUser?>(null)
    override val loggedInUser: StateFlow<LoggedInUser?> = _loggedInUser.asStateFlow()

    private var token: String? = null
    var retryLoginResult: Result<Unit> = Result.success(Unit)
    var retryLoginCallCount = 0

    fun setLoggedIn(username: String, tokenValue: String, loginTime: Long = 0L) {
        token = tokenValue
        _loggedInUser.value = LoggedInUser(username, loginTime)
    }

    fun setLoggedOut() {
        token = null
        _loggedInUser.value = null
    }

    override suspend fun login(username: String, password: String, rememberMe: Boolean) {
        token = "fake-token"
        _loggedInUser.value = LoggedInUser(username, 0L)
    }

    override suspend fun retryLogin() {
        retryLoginCallCount++
        retryLoginResult.getOrThrow()
        token = "retried-token"
    }

    override suspend fun logout() {
        token = null
        _loggedInUser.value = null
    }

    override fun getToken(): String? = token
}
